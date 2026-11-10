"""Simulador de dispositivo de bancada (recursos limitados, rede instavel).

  python simulador.py --aluno 1 --eventos 3
  python simulador.py --aluno 1 --eventos 4 --queda 2:5   # cai a rede apos 2 eventos, por 5 s
  python simulador.py --aluno 1 --duplicar ID              # reenvia um id ja publicado (sem PUBACK recebido)
  python simulador.py --aluno 1 --invalido                 # publica payload com tipo desconhecido

O dispositivo guarda os eventos num buffer LOCAL limitado (500) enquanto esta sem rede
e descarrega quando volta. Publica com QoS 1 e Last Will (status offline retido).
"""
import argparse
import json
import os
import time
import uuid
from collections import deque

import paho.mqtt.client as mqtt

TIPOS = ["P", "I", "F"]


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--host", default=os.getenv("MQTT_HOST", "localhost"))
    ap.add_argument("--porta", type=int, default=int(os.getenv("MQTT_PORT", "1883")))
    ap.add_argument("--laboratorio", default="LAB-ARQ")
    ap.add_argument("--dispositivo", default="bancada-03")
    ap.add_argument("--aluno", type=int, required=True)
    ap.add_argument("--eventos", type=int, default=3)
    ap.add_argument("--intervalo", type=float, default=0.3)
    ap.add_argument("--queda", help="N:S = apos N eventos a rede cai por S segundos")
    ap.add_argument("--duplicar", help="reenvia este id de evento")
    ap.add_argument("--invalido", action="store_true")
    a = ap.parse_args()

    base = f"lab/{a.laboratorio}/{a.dispositivo}"
    cliente = mqtt.Client(mqtt.CallbackAPIVersion.VERSION2, client_id=f"disp-{a.dispositivo}", clean_session=False)
    cliente.will_set(f"{base}/status", "offline", qos=1, retain=True)
    cliente.connect(a.host, a.porta, keepalive=20)
    cliente.loop_start()
    cliente.publish(f"{base}/status", "online", qos=1, retain=True)

    buffer = deque(maxlen=500)
    conectado = True
    queda_apos, queda_seg = (int(x) for x in a.queda.split(":")) if a.queda else (None, 0)

    def enviar(payload):
        info = cliente.publish(f"{base}/eventos", payload, qos=1)
        info.wait_for_publish(timeout=5)
        return info

    if a.invalido:
        p = json.dumps({"id": uuid.uuid4().hex[:16], "d": a.dispositivo, "t": "X", "a": a.aluno, "ts": int(time.time())})
        enviar(p)
        print(f"[{a.dispositivo}] publicou payload INVALIDO: {p}")
    elif a.duplicar:
        p = json.dumps({"id": a.duplicar, "d": a.dispositivo, "t": "P", "a": a.aluno, "ts": int(time.time())},
                       separators=(",", ":"))
        enviar(p)
        print(f"[{a.dispositivo}] REENVIOU o evento {a.duplicar} ({len(p)} bytes)")
    else:
        for i in range(a.eventos):
            if queda_apos is not None and i == queda_apos and conectado:
                cliente.loop_stop()
                cliente.disconnect()
                conectado = False
                fim_da_queda = time.time() + queda_seg
                print(f"[{a.dispositivo}] REDE CAIU por {queda_seg}s; eventos vao para o buffer local")
            p = json.dumps({"id": uuid.uuid4().hex[:16], "d": a.dispositivo, "t": TIPOS[i % 3], "a": a.aluno,
                            "ts": int(time.time()), "at": "ARQ-LAB-1"}, separators=(",", ":"))
            if conectado:
                enviar(p)
                print(f"[{a.dispositivo}] publicou {p} ({len(p)} bytes, QoS 1)")
            else:
                buffer.append(p)
                print(f"[{a.dispositivo}] SEM REDE: guardou no buffer ({len(buffer)}) {p}")
            time.sleep(a.intervalo)
        if not conectado:
            time.sleep(max(0.0, fim_da_queda - time.time()))
            cliente.reconnect()
            cliente.loop_start()
            print(f"[{a.dispositivo}] REDE VOLTOU; descarregando {len(buffer)} eventos do buffer")
            while buffer:
                p = buffer.popleft()
                enviar(p)
                print(f"[{a.dispositivo}] publicou (do buffer) {p}")
    time.sleep(0.5)
    cliente.loop_stop()
    cliente.disconnect()


if __name__ == "__main__":
    main()
