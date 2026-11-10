"""Encaminha mensagens MQTT ao Core com retentativa, e so confirma (ack) depois do aceite.

Regras (ADR-005):
  Core 201/200          -> ack (200 = duplicado, ja processado: QoS 1 e "pelo menos uma vez")
  Core 422/400 ou payload invalido -> publica na DLQ (lab/dlq) e ack: reenviar nao adianta
  Core fora / 5xx / timeout        -> NAO ack; tenta de novo com backoff ate o Core voltar.
                                      Se o gateway morrer nesse meio tempo, o broker reentrega
                                      (sessao persistente + mensagem sem PUBACK).
"""
import json
import logging
import time
import urllib.error
import urllib.request

from prometheus_client import Counter, Gauge

import transformacao

LOG = logging.getLogger("iot-gateway")
RECEBIDAS = Counter("iot_mensagens_recebidas_total", "Mensagens MQTT recebidas pelo gateway")
ENCAMINHADAS = Counter("iot_encaminhadas_total", "Resultado do encaminhamento ao Core", ["resultado"])
RETENTATIVAS = Counter("iot_retentativas_total", "Tentativas repetidas por Core indisponivel")
FILA = Gauge("iot_fila_interna", "Mensagens recebidas e ainda nao confirmadas ao broker")


class CoreIndisponivel(Exception):
    pass


class Encaminhador:
    def __init__(self, core_url, confirmar, publicar_dlq, dormir=time.sleep, timeout=3.0,
                 backoff_inicial=1.0, backoff_teto=30.0):
        self.core_url = core_url.rstrip("/")
        self.confirmar = confirmar
        self.publicar_dlq = publicar_dlq
        self.dormir = dormir
        self.timeout = timeout
        self.backoff_inicial = backoff_inicial
        self.backoff_teto = backoff_teto

    def _post(self, corpo: dict) -> int:
        req = urllib.request.Request(self.core_url + "/iot/eventos", data=json.dumps(corpo).encode(),
                                     headers={"Content-Type": "application/json"}, method="POST")
        try:
            with urllib.request.urlopen(req, timeout=self.timeout) as r:
                return r.status
        except urllib.error.HTTPError as e:
            if e.code >= 500:
                raise CoreIndisponivel(f"HTTP {e.code}") from e
            return e.code
        except (urllib.error.URLError, TimeoutError, ConnectionError, OSError) as e:
            raise CoreIndisponivel(str(e)) from e

    def processar(self, mid, topico: str, payload: bytes) -> str:
        """Processa UMA mensagem ate um desfecho definitivo. Devolve o resultado."""
        RECEBIDAS.inc()
        FILA.inc()
        try:
            try:
                corpo = transformacao.traduzir(topico, payload)
            except transformacao.EventoInvalido as e:
                return self._dlq(mid, topico, payload, str(e))
            espera = self.backoff_inicial
            while True:
                try:
                    status = self._post(corpo)
                    break
                except CoreIndisponivel as e:
                    RETENTATIVAS.inc()
                    LOG.warning("Core indisponivel (%s); evento %s retido, nova tentativa em %.0fs",
                                e, corpo["eventoId"], espera)
                    self.dormir(espera)
                    espera = min(espera * 2, self.backoff_teto)
            if status in (200, 201):
                resultado = "novo" if status == 201 else "duplicado"
                LOG.info("evento %s -> Core %s (%s)", corpo["eventoId"], status, resultado)
                ENCAMINHADAS.labels(resultado).inc()
                self.confirmar(mid)
                return resultado
            return self._dlq(mid, topico, payload, f"Core recusou com HTTP {status}")
        finally:
            FILA.dec()

    def _dlq(self, mid, topico, payload, motivo):
        LOG.error("evento invalido enviado para a DLQ: %s", motivo)
        ENCAMINHADAS.labels("invalido").inc()
        self.publicar_dlq(json.dumps({"topico": topico, "payload": payload.decode(errors="replace"),
                                      "motivo": motivo}))
        self.confirmar(mid)
        return "invalido"
