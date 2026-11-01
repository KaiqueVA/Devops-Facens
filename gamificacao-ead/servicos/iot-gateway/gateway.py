"""iot-gateway: assina o broker MQTT e entrega ao Core por HTTP (ADR-005).

Variaveis de ambiente:
  MQTT_HOST (localhost)  MQTT_PORT (1883)  MQTT_USUARIO / MQTT_SENHA (opcionais)
  CORE_URL (http://localhost:8080)  METRICAS_PORTA (9101)  CLIENT_ID (iot-gateway)
"""
import logging
import os
import queue
import threading

import paho.mqtt.client as mqtt
from prometheus_client import start_http_server

from encaminhador import Encaminhador

TOPICO = "lab/+/+/eventos"
TOPICO_DLQ = "lab/dlq"

logging.basicConfig(level=logging.INFO, format="%(asctime)s %(levelname)s %(message)s")
LOG = logging.getLogger("iot-gateway")


def main():
    host = os.getenv("MQTT_HOST", "localhost")
    porta = int(os.getenv("MQTT_PORT", "1883"))
    core = os.getenv("CORE_URL", "http://localhost:8080")
    start_http_server(int(os.getenv("METRICAS_PORTA", "9101")))

    # clean_session=False + client_id fixo: o broker guarda as mensagens QoS 1 enquanto o gateway estiver fora.
    # manual_ack=True: o PUBACK so sai depois que o Core aceitou (ou o evento foi para a DLQ).
    cliente = mqtt.Client(mqtt.CallbackAPIVersion.VERSION2, client_id=os.getenv("CLIENT_ID", "iot-gateway"),
                          clean_session=False, manual_ack=True)
    if os.getenv("MQTT_USUARIO"):
        cliente.username_pw_set(os.getenv("MQTT_USUARIO"), os.getenv("MQTT_SENHA"))
    cliente.reconnect_delay_set(min_delay=1, max_delay=30)

    fila = queue.Queue()
    encaminhador = Encaminhador(core, confirmar=lambda mid: cliente.ack(mid, 1),
                                publicar_dlq=lambda texto: cliente.publish(TOPICO_DLQ, texto, qos=1))

    def ao_conectar(c, userdata, flags, reason_code, properties):
        LOG.info("conectado ao broker %s:%s (sessao retomada=%s)", host, porta, flags.session_present)
        c.subscribe(TOPICO, qos=1)

    def ao_receber(c, userdata, msg):
        fila.put((msg.mid, msg.topic, msg.payload))  # nao bloqueia a thread de rede do MQTT

    def trabalhador():
        while True:
            mid, topico, payload = fila.get()
            encaminhador.processar(mid, topico, payload)

    cliente.on_connect = ao_conectar
    cliente.on_message = ao_receber
    threading.Thread(target=trabalhador, daemon=True).start()
    LOG.info("iot-gateway: %s -> %s/iot/eventos", TOPICO, core)
    cliente.connect(host, porta, keepalive=30)
    cliente.loop_forever(retry_first_connection=True)


if __name__ == "__main__":
    main()
