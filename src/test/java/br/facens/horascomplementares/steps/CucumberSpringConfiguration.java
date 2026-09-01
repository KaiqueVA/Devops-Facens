package br.facens.horascomplementares.steps;

import br.facens.horascomplementares.support.ConfiguracaoTeste;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@CucumberContextConfiguration
@SpringBootTest
@Import(ConfiguracaoTeste.class)
public class CucumberSpringConfiguration {
}
