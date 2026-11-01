package br.facens.gamificacao.eventos.outbox;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Duration;
import java.time.Instant;

/** Uma linha do outbox = um evento a ser entregue a UM assinante. */
@Entity
@Table(name = "outbox_entrega",
        uniqueConstraints = @UniqueConstraint(name = "uk_outbox_evento_assinante", columnNames = {"evento_id", "assinante"}),
        indexes = @Index(name = "idx_outbox_fila", columnList = "status, proxima_tentativa_em"))
public class EntregaPendente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "evento_id", nullable = false, length = 36)
    private String eventoId;

    @Column(nullable = false, length = 80)
    private String assinante;

    @Column(name = "tipo_evento", nullable = false, length = 200)
    private String tipoEvento;

    @Column(nullable = false, length = 4000)
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusEntrega status;

    private int tentativas;

    @Column(name = "proxima_tentativa_em", nullable = false)
    private Instant proximaTentativaEm;

    @Column(name = "ultimo_erro", length = 500)
    private String ultimoErro;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm;

    @Column(name = "entregue_em")
    private Instant entregueEm;

    protected EntregaPendente() {
        // JPA
    }

    EntregaPendente(String eventoId, String assinante, String tipoEvento, String payload, Instant agora) {
        this.eventoId = eventoId;
        this.assinante = assinante;
        this.tipoEvento = tipoEvento;
        this.payload = payload;
        this.status = StatusEntrega.PENDENTE;
        this.criadoEm = agora;
        this.proximaTentativaEm = agora;
    }

    void marcarEntregue(Instant agora) {
        this.tentativas++;
        this.status = StatusEntrega.ENTREGUE;
        this.entregueEm = agora;
        this.ultimoErro = null;
    }

    /** Backoff exponencial: base * 2^(tentativas-1), limitado ao teto. */
    void registrarFalha(String erro, Instant agora, int maxTentativas, Duration base, Duration teto) {
        this.tentativas++;
        this.ultimoErro = erro == null ? "erro sem mensagem" : erro.substring(0, Math.min(erro.length(), 500));
        if (tentativas >= maxTentativas) {
            this.status = StatusEntrega.FALHOU_DEFINITIVO;
            return;
        }
        long fator = 1L << Math.min(tentativas - 1, 20);
        Duration espera = base.multipliedBy(fator);
        this.proximaTentativaEm = agora.plus(espera.compareTo(teto) > 0 ? teto : espera);
    }

    void reabrir(Instant agora) {
        this.status = StatusEntrega.PENDENTE;
        this.tentativas = 0;
        this.proximaTentativaEm = agora;
    }

    public Long getId() {
        return id;
    }

    public String getEventoId() {
        return eventoId;
    }

    public String getAssinante() {
        return assinante;
    }

    public String getTipoEvento() {
        return tipoEvento;
    }

    public String getPayload() {
        return payload;
    }

    public StatusEntrega getStatus() {
        return status;
    }

    public int getTentativas() {
        return tentativas;
    }

    public Instant getProximaTentativaEm() {
        return proximaTentativaEm;
    }

    public String getUltimoErro() {
        return ultimoErro;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }

    public Instant getEntregueEm() {
        return entregueEm;
    }
}
