FROM python:3.12-slim
WORKDIR /srv
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt
COPY transformacao.py encaminhador.py gateway.py simulador.py ./
EXPOSE 9101
CMD ["python", "-u", "gateway.py"]
