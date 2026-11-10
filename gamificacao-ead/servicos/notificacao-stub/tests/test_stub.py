import json
import sys
import threading
import urllib.error
import urllib.request
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
import stub  # noqa: E402


def _post(url, corpo, chave=None):
    req = urllib.request.Request(url, data=json.dumps(corpo).encode(), method="POST",
                                 headers={"Content-Type": "application/json", **({"Idempotency-Key": chave} if chave else {})})
    try:
        with urllib.request.urlopen(req) as r:
            return r.status
    except urllib.error.HTTPError as e:
        return e.code


def test_idempotencia_e_listagem():
    servidor = stub.criar_servidor(0)
    threading.Thread(target=servidor.serve_forever, daemon=True).start()
    url = f"http://localhost:{servidor.server_address[1]}"
    try:
        assert _post(url + "/notificacoes", {"titulo": "oi"}, "k1") == 202
        assert _post(url + "/notificacoes", {"titulo": "oi"}, "k1") == 200
        assert _post(url + "/notificacoes", {"titulo": "oi"}) == 400
        with urllib.request.urlopen(url + "/notificacoes") as r:
            recebidas = json.loads(r.read())
        assert len(recebidas) == 1
        assert recebidas[0]["duplicadas"] == 1
    finally:
        servidor.shutdown()


def test_corpo_chunked_como_o_cliente_do_spring_envia():
    import http.client
    servidor = stub.criar_servidor(0)
    threading.Thread(target=servidor.serve_forever, daemon=True).start()
    try:
        con = http.client.HTTPConnection("localhost", servidor.server_address[1])
        corpo = json.dumps({"titulo": "Curso conquistado", "destinatario": "ana@facens.br"}).encode()
        con.request("POST", "/notificacoes", body=iter([corpo[:10], corpo[10:]]),
                    headers={"Content-Type": "application/json", "Idempotency-Key": "k-chunked",
                             "Transfer-Encoding": "chunked"}, encode_chunked=True)
        assert con.getresponse().status == 202
        assert stub.RECEBIDAS["k-chunked"]["titulo"] == "Curso conquistado"
    finally:
        servidor.shutdown()
