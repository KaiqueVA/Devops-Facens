package br.facens.horascomplementares.support;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class ConfiguracaoTeste {

    /**
     * Relógio de teste, marcado como {@code @Primary} para substituir o
     * {@code Clock} de produção nas injeções dos serviços durante os cenários BDD.
     */
    @Bean
    @Primary
    public RelogioAjustavel relogioAjustavel() {
        return new RelogioAjustavel();
    }
}
