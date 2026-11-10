package br.facens.gamificacao.analytics;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "registro_laboratorio")
public class RegistroDeLaboratorio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "evento_id", nullable = false, unique = true, length = 64)
    private String eventoId;

    private Long alunoId;
    private String laboratorio;
    private String tipo;
    private Instant ocorridoEm;

    protected RegistroDeLaboratorio() {
        // JPA
    }

    RegistroDeLaboratorio(String eventoId, Long alunoId, String laboratorio, String tipo, Instant ocorridoEm) {
        this.eventoId = eventoId;
        this.alunoId = alunoId;
        this.laboratorio = laboratorio;
        this.tipo = tipo;
        this.ocorridoEm = ocorridoEm;
    }
}
