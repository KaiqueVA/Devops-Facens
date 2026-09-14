package br.facens.gamificacao.aluno.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EnumsTest {

    @Test
    @DisplayName("CT-ENUM-01 - PlanoAssinatura tem BASICO e PREMIUM")
    void planoAssinatura() {
        assertEquals(2, PlanoAssinatura.values().length);
        assertEquals(PlanoAssinatura.BASICO, PlanoAssinatura.valueOf("BASICO"));
        assertEquals(PlanoAssinatura.PREMIUM, PlanoAssinatura.valueOf("PREMIUM"));
    }

    @Test
    @DisplayName("CT-ENUM-02 - DestinoMoeda tem os tres destinos do case")
    void destinoMoeda() {
        assertEquals(3, DestinoMoeda.values().length);
        assertEquals(DestinoMoeda.CONHECIMENTO, DestinoMoeda.valueOf("CONHECIMENTO"));
        assertEquals(DestinoMoeda.ACUMULO, DestinoMoeda.valueOf("ACUMULO"));
        assertEquals(DestinoMoeda.CRIPTOMOEDA, DestinoMoeda.valueOf("CRIPTOMOEDA"));
    }
}
