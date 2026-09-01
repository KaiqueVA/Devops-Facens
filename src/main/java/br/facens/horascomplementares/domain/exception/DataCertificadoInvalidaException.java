package br.facens.horascomplementares.domain.exception;

public class DataCertificadoInvalidaException extends NegocioException {

    public DataCertificadoInvalidaException(String motivo) {
        super("Data do certificado invalida: " + motivo + ".");
    }
}
