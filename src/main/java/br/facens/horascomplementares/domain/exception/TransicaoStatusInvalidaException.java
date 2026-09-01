package br.facens.horascomplementares.domain.exception;

import br.facens.horascomplementares.domain.StatusCertificado;

public class TransicaoStatusInvalidaException extends NegocioException {

    public TransicaoStatusInvalidaException(StatusCertificado statusAtual) {
        super("Operacao invalida: apenas certificados EM_ANALISE podem ser validados, "
                + "mas o certificado esta com status " + statusAtual + ".");
    }
}
