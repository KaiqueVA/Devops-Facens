package br.facens.horascomplementares.domain.exception;

public class CertificadoInexistenteException extends NegocioException {

    public CertificadoInexistenteException(Long id) {
        super("Certificado com id " + id + " nao encontrado.");
    }
}
