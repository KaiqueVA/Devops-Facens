package br.facens.horascomplementares.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.LocalDate;

@Entity
public class Aluno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    private LocalDate dataIngresso;

    protected Aluno() {
    }

    public Aluno(String nome, LocalDate dataIngresso) {
        this.nome = nome;
        this.dataIngresso = dataIngresso;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public LocalDate getDataIngresso() {
        return dataIngresso;
    }
}
