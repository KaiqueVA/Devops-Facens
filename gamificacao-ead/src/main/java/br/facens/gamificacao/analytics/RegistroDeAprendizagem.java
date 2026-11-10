package br.facens.gamificacao.analytics;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "registro_aprendizagem",
        uniqueConstraints = @UniqueConstraint(name = "uk_registro_evento", columnNames = "evento_id"))
public class RegistroDeAprendizagem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "evento_id", nullable = false, length = 36)
    private String eventoId;

    private Long alunoId;
    private String curso;
    private BigDecimal media;
    private boolean conquistado;
    private boolean promovidoAPremium;
    private Instant ocorridoEm;

    protected RegistroDeAprendizagem() {
        // JPA
    }

    RegistroDeAprendizagem(String eventoId, Long alunoId, String curso, BigDecimal media, boolean conquistado,
                           boolean promovidoAPremium, Instant ocorridoEm) {
        this.eventoId = eventoId;
        this.alunoId = alunoId;
        this.curso = curso;
        this.media = media;
        this.conquistado = conquistado;
        this.promovidoAPremium = promovidoAPremium;
        this.ocorridoEm = ocorridoEm;
    }
}
