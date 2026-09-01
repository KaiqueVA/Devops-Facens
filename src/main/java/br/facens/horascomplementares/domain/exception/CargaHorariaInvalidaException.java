package br.facens.horascomplementares.domain.exception;

public class CargaHorariaInvalidaException extends NegocioException {

    public CargaHorariaInvalidaException(int cargaHoraria) {
        super("Carga horaria invalida: " + cargaHoraria
                + ". Deve ser um numero inteiro maior que 0 e menor ou igual a 200.");
    }
}
