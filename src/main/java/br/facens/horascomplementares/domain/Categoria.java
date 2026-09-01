package br.facens.horascomplementares.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String nome;

    private int tetoHoras;

    protected Categoria() {
    }

    public Categoria(String nome, int tetoHoras) {
        this.nome = nome;
        this.tetoHoras = tetoHoras;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public int getTetoHoras() {
        return tetoHoras;
    }
}
