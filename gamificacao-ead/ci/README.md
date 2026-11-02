# Esteira Jenkins (ADR-006)

Três pipelines independentes, um por componente com deploy próprio:

| Job | Script Path | Gates que bloqueiam o deploy |
|---|---|---|
| `core-gamificacao` | `gamificacao-ead/Jenkinsfile` | `mvn verify` (unidade + Cucumber + `ArquiteturaModularTest` + JaCoCo) · `promtool test rules` · smoke (health, `/ranking`, métricas dos ADRs) |
| `ia-service` | `gamificacao-ead/servicos/ia-service/Jenkinsfile` | `pytest` · smoke (`/health`, `/metrics`) |
| `iot-gateway` | `gamificacao-ead/servicos/iot-gateway/Jenkinsfile` | `pytest` · smoke (`/metrics`) |

Cada pipeline só roda as etapas se **o próprio componente mudou** desde o último build bem-sucedido (ou com `FORCAR=true`). O deploy é `docker compose up -d --no-deps <serviço>`: só o componente é recriado. Smoke test OK → a imagem vira `:estavel`; smoke falhou → rollback para `:estavel`.

## Subir o Jenkins do lab

```bash
cd gamificacao-ead
docker compose up -d                        # Core, IA, eventos, IoT, Prometheus, Grafana
docker compose --profile ci up -d jenkins   # Jenkins em http://localhost:8081
docker compose exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword
```

1. Abra http://localhost:8081, cole a senha e escolha **Install suggested plugins** (os plugins do `plugins.txt` já vêm instalados).
2. **New Item → Multibranch Pipeline** (um para cada linha da tabela).
   - Branch Source: Git → `https://github.com/KaiqueVA/Devops-Facens.git`
   - Build Configuration → Script Path: o da tabela.
3. **Scan Multibranch Pipeline Now**. Os três jobs passam a rodar sozinhos a cada push.

> O Jenkins roda como `root` e usa o Docker do host (`/var/run/docker.sock`) só porque é um laboratório.
