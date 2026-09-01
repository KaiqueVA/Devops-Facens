package br.facens.horascomplementares.domain;

import br.facens.horascomplementares.domain.exception.CargaHorariaInvalidaException;
import br.facens.horascomplementares.domain.exception.DataCertificadoInvalidaException;

import java.time.LocalDate;

/**
 * Regras de validação de um certificado no momento da submissão.
 * Classe de domínio pura (sem dependência de Spring ou de persistência),
 * o que permite testá-la 100% em teste de unidade.
 */
public class CertificadoValidator {

    private static final int CARGA_HORARIA_MINIMA = 1;
    private static final int CARGA_HORARIA_MAXIMA = 200;

    public void validar(int cargaHoraria, LocalDate data, LocalDate dataIngresso, LocalDate hoje) {
        validarCargaHoraria(cargaHoraria);
        validarData(data, dataIngresso, hoje);
    }

    private void validarCargaHoraria(int cargaHoraria) {
        if (cargaHoraria < CARGA_HORARIA_MINIMA || cargaHoraria > CARGA_HORARIA_MAXIMA) {
            throw new CargaHorariaInvalidaException(cargaHoraria);
        }
    }

    private void validarData(LocalDate data, LocalDate dataIngresso, LocalDate hoje) {
        if (data.isAfter(hoje)) {
            throw new DataCertificadoInvalidaException("nao pode estar no futuro");
        }
        if (data.isBefore(dataIngresso)) {
            throw new DataCertificadoInvalidaException("nao pode ser anterior ao ingresso do aluno");
        }
    }
}
