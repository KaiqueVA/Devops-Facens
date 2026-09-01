package br.facens.horascomplementares.domain.exception;

/**
 * Exceção base para todas as violações de regra de negócio do domínio de
 * horas complementares. Tratada de forma centralizada no RestControllerAdvice.
 */
public abstract class NegocioException extends RuntimeException {

    protected NegocioException(String mensagem) {
        super(mensagem);
    }
}
