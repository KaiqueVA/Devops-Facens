"""Provedor externo de notificacoes SIMULADO (e-mail/push) para o Desafio 4.

Representa um servico de terceiros que o Core nao controla e que pode cair.
So usa a biblioteca padrao do Python.

  POST /notificacoes   recebe a notificacao (cabecalho Idempotency-Key obrigatorio)
                       202 = aceita | 200 = duplicada (ja recebida) | 400 = sem chave
  GET  /notificacoes   lista o que foi recebido
  GET  /health
"""
import json
import os
import threading
from datetime import datetime, timezone
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer

RECEBIDAS = {}
TRAVA = threading.Lock()


class Handler(BaseHTTPRequestHandler):
    def _responder(self, status, corpo=None):
        dados = json.dumps(corpo if corpo is not None else {}, ensure_ascii=False).encode()
        self.send_response(status)
        self.send_header("Content-Type", "application/json")
        self.send_header("Content-Length", str(len(dados)))
        self.end_headers()
        self.wfile.write(dados)

    def do_GET(self):
        if self.path == "/health":
            return self._responder(200, {"status": "UP"})
        if self.path == "/notificacoes":
            with TRAVA:
                return self._responder(200, list(RECEBIDAS.values()))
        return self._responder(404, {"erro": "nao encontrado"})

    def _ler_corpo(self):
        if "chunked" in (self.headers.get("Transfer-Encoding") or "").lower():
            partes = []
            while True:
                tamanho = int(self.rfile.readline().strip().split(b";")[0], 16)
                if tamanho == 0:
                    self.rfile.readline()
                    break
                partes.append(self.rfile.read(tamanho))
                self.rfile.readline()
            return b"".join(partes)
        return self.rfile.read(int(self.headers.get("Content-Length") or 0))

    def do_POST(self):
        if self.path != "/notificacoes":
            return self._responder(404, {"erro": "nao encontrado"})
        chave = self.headers.get("Idempotency-Key")
        corpo = json.loads(self._ler_corpo() or b"{}")
        if not chave:
            return self._responder(400, {"erro": "Idempotency-Key obrigatorio"})
        with TRAVA:
            if chave in RECEBIDAS:
                RECEBIDAS[chave]["duplicadas"] += 1
                print(f"[stub] DUPLICADA ignorada {chave}", flush=True)
                return self._responder(200, {"status": "duplicada"})
            RECEBIDAS[chave] = {"chave": chave, "recebidaEm": datetime.now(timezone.utc).isoformat(),
                                "duplicadas": 0, **corpo}
        print(f"[stub] ENVIADA para {corpo.get('destinatario')}: {corpo.get('titulo')} (chave {chave})", flush=True)
        return self._responder(202, {"status": "aceita"})

    def log_message(self, *args):
        pass


def criar_servidor(porta):
    return ThreadingHTTPServer(("0.0.0.0", porta), Handler)


if __name__ == "__main__":
    porta = int(os.getenv("PORTA", "8090"))
    print(f"[stub] provedor de notificacoes ouvindo na porta {porta}", flush=True)
    criar_servidor(porta).serve_forever()
