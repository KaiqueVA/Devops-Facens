package br.facens.horascomplementares.support;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;

/**
 * Relógio de teste cuja "data de hoje" pode ser fixada pelos cenários BDD.
 */
public class RelogioAjustavel extends Clock {

    private static final ZoneId ZONA = ZoneOffset.UTC;

    private Instant instante = LocalDate.of(2024, 1, 1).atStartOfDay(ZONA).toInstant();

    public void definirData(LocalDate data) {
        this.instante = data.atStartOfDay(ZONA).toInstant();
    }

    @Override
    public ZoneId getZone() {
        return ZONA;
    }

    @Override
    public Clock withZone(ZoneId zone) {
        return this;
    }

    @Override
    public Instant instant() {
        return instante;
    }
}
