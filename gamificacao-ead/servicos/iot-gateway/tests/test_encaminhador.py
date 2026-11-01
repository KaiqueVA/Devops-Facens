import json
import threading
from http.server import BaseHTTPRequestHandler, HTTPServer

from encaminhador import Encaminhador

TOPICO = "lab/LAB-ARQ/bancada-03/eventos"
PAYLOAD = json.dumps({"id": "e1", "d": "bancada-03", "t": "P", "a": 1, "ts": 1793480000}).encode()


def core_falso(respostas):
    """Sobe um 'Core' que responde a sequencia de status informada."""
    recebidos = []

    class H(BaseHTTPRequestHandler):
        def do_POST(self):
            recebidos.append(json.loads(self.rfile.read(int(self.headers["Content-Length"]))))
            self.send_response(respostas.pop(0) if respostas else 201)
            self.end_headers()

        def log_message(self, *a):
            pass

    srv = HTTPServer(("localhost", 0), H)
    threading.Thread(target=srv.serve_forever, daemon=True).start()
    return srv, f"http://localhost:{srv.server_address[1]}", recebidos


def montar(url):
    acks, dlq, esperas = [], [], []
    e = Encaminhador(url, confirmar=acks.append, publicar_dlq=dlq.append, dormir=esperas.append, timeout=1)
    return e, acks, dlq, esperas


def test_novo_e_duplicado_sao_confirmados():
    srv, url, recebidos = core_falso([201, 200])
    e, acks, dlq, _ = montar(url)
    assert e.processar(1, TOPICO, PAYLOAD) == "novo"
    assert e.processar(2, TOPICO, PAYLOAD) == "duplicado"
    assert acks == [1, 2] and dlq == []
    assert recebidos[0]["eventoId"] == "e1"
    srv.shutdown()


def test_core_com_5xx_retem_e_reenvia_com_backoff_sem_confirmar_antes():
    srv, url, recebidos = core_falso([503, 503, 503, 201])
    e, acks, dlq, esperas = montar(url)
    assert e.processar(7, TOPICO, PAYLOAD) == "novo"
    assert esperas == [1.0, 2.0, 4.0]       # backoff exponencial
    assert acks == [7] and len(recebidos) == 4
    srv.shutdown()


def test_core_desligado_retem_ate_voltar():
    srv, url, _ = core_falso([])
    srv.shutdown()
    srv.server_close()
    e, acks, _, esperas = montar(url)
    tentativas = {"n": 0}

    def dormir(s):
        esperas.append(s)
        tentativas["n"] += 1
        if tentativas["n"] == 3:                # o Core "volta" na 3a espera
            novo, novo_url, _ = core_falso([201])
            e.core_url = novo_url

    e.dormir = dormir
    assert e.processar(9, TOPICO, PAYLOAD) == "novo"
    assert acks == [9] and esperas == [1.0, 2.0, 4.0]


def test_invalido_vai_para_dlq_e_e_confirmado():
    srv, url, recebidos = core_falso([422])
    e, acks, dlq, _ = montar(url)
    assert e.processar(3, TOPICO, b"{lixo") == "invalido"     # nem chega ao Core
    assert e.processar(4, TOPICO, PAYLOAD) == "invalido"      # Core recusou (ex.: aluno inexistente)
    assert acks == [3, 4] and len(dlq) == 2 and len(recebidos) == 1
    assert "HTTP 422" in json.loads(dlq[1])["motivo"]
    srv.shutdown()
