package br.facens.horascomplementares.domain.exception;

public class CertificadoImutavelException extends NegocioException {

    public CertificadoImutavelException() {
        super("Certificado APROVADO e imutavel e nao pode ter seu status alterado.");
    }
}
