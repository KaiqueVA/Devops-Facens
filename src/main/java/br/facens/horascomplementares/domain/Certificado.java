package br.facens.horascomplementares.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

import br.facens.horascomplementares.domain.exception.CertificadoImutavelException;
import br.facens.horascomplementares.domain.exception.JustificativaInsuficienteException;
import br.facens.horascomplementares.domain.exception.TransicaoStatusInvalidaException;

import java.time.LocalDate;

@Entity
public class Certificado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Aluno aluno;

    @ManyToOne(optional = false)
    private Categoria categoria;

    private String titulo;

    private int cargaHoraria;

    private LocalDate data;

    @Enumerated(EnumType.STRING)
    private StatusCertificado status;

    private String justificativaReprovacao;

    protected Certificado() {
    }

    public Certificado(Aluno aluno, Categoria categoria, String titulo, int cargaHoraria, LocalDate data) {
        this.aluno = aluno;
        this.categoria = categoria;
        this.titulo = titulo;
        this.cargaHoraria = cargaHoraria;
        this.data = data;
        this.status = StatusCertificado.EM_ANALISE;
    }

    public Long getId() {
        return id;
    }

    public Aluno getAluno() {
        return aluno;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public String getTitulo() {
        return titulo;
    }

    public int getCargaHoraria() {
        return cargaHoraria;
    }

    public LocalDate getData() {
        return data;
    }

    public StatusCertificado getStatus() {
        return status;
    }

    public String getJustificativaReprovacao() {
        return justificativaReprovacao;
    }

    private static final int TAMANHO_MINIMO_JUSTIFICATIVA = 10;

    public void aprovar() {
        garantirQuePodeSerValidado();
        this.status = StatusCertificado.APROVADO;
    }

    public void reprovar(String justificativa) {
        garantirQuePodeSerValidado();
        if (justificativa == null || justificativa.trim().length() < TAMANHO_MINIMO_JUSTIFICATIVA) {
            throw new JustificativaInsuficienteException(TAMANHO_MINIMO_JUSTIFICATIVA);
        }
        this.status = StatusCertificado.REPROVADO;
        this.justificativaReprovacao = justificativa;
    }

    private void garantirQuePodeSerValidado() {
        if (this.status == StatusCertificado.APROVADO) {
            throw new CertificadoImutavelException();
        }
        if (this.status != StatusCertificado.EM_ANALISE) {
            throw new TransicaoStatusInvalidaException(this.status);
        }
    }
}
