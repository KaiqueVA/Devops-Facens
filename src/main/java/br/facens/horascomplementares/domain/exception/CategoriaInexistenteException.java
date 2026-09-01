package br.facens.horascomplementares.domain.exception;

public class CategoriaInexistenteException extends NegocioException {

    public CategoriaInexistenteException(String nome) {
        super("Categoria \"" + nome + "\" nao existe no catalogo da faculdade.");
    }
}
