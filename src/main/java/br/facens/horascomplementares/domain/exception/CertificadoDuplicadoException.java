package br.facens.horascomplementares.domain.exception;

import java.time.LocalDate;

public class CertificadoDuplicadoException extends NegocioException {

    public CertificadoDuplicadoException(String titulo, LocalDate data) {
        super("Certificado duplicado: o aluno ja possui um certificado \"" + titulo
                + "\" na data " + data + ".");
    }
}
