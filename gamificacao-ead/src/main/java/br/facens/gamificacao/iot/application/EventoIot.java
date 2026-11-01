package br.facens.gamificacao.iot.application;

import br.facens.gamificacao.iot.api.TipoEventoLaboratorio;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;

/** Evento de laboratorio aceito. A unique em evento_id e a ultima barreira contra duplicidade. */
@Entity
@Table(name = "evento_iot", uniqueConstraints = @UniqueConstraint(name = "uk_evento_iot", columnNames = "evento_id"))
public class EventoIot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "evento_id", nullable = false, length = 64)
    private String eventoId;

    @Column(nullable = false, length = 60)
    private String laboratorio;

    @Column(nullable = false, length = 60)
    private String dispositivo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoEventoLaboratorio tipo;

    @Column(nullable = false)
    private Long alunoId;

    @Column(length = 60)
    private String atividade;

    @Column(nullable = false)
    private Instant ocorridoEm;

    @Column(nullable = false)
    private Instant recebidoEm;

    protected EventoIot() {
        // JPA
    }

    EventoIot(ComandoEventoIot c, Instant recebidoEm) {
        this.eventoId = c.eventoId();
        this.laboratorio = c.laboratorio();
        this.dispositivo = c.dispositivo();
        this.tipo = c.tipo();
        this.alunoId = c.alunoId();
        this.atividade = c.atividade();
        this.ocorridoEm = c.ocorridoEm();
        this.recebidoEm = recebidoEm;
    }
}
