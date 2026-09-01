package br.facens.horascomplementares.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

import java.time.LocalDateTime;

@Entity
public class HistoricoValidacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Certificado certificado;

    @Enumerated(EnumType.STRING)
    private StatusCertificado resultado;

    private String justificativa;

    private LocalDateTime dataHora;

    protected HistoricoValidacao() {
    }

    private HistoricoValidacao(Certificado certificado, StatusCertificado resultado,
                               String justificativa, LocalDateTime dataHora) {
        this.certificado = certificado;
        this.resultado = resultado;
        this.justificativa = justificativa;
        this.dataHora = dataHora;
    }

    public static HistoricoValidacao aprovacao(Certificado certificado, LocalDateTime dataHora) {
        return new HistoricoValidacao(certificado, StatusCertificado.APROVADO, null, dataHora);
    }

    public static HistoricoValidacao reprovacao(Certificado certificado, String justificativa,
                                                LocalDateTime dataHora) {
        return new HistoricoValidacao(certificado, StatusCertificado.REPROVADO, justificativa, dataHora);
    }

    public Long getId() {
        return id;
    }

    public Certificado getCertificado() {
        return certificado;
    }

    public StatusCertificado getResultado() {
        return resultado;
    }

    public String getJustificativa() {
        return justificativa;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }
}
