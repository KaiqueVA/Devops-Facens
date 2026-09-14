package br.facens.gamificacao.aluno.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import br.facens.gamificacao.aluno.domain.Aluno;
import br.facens.gamificacao.aluno.domain.DestinoMoeda;
import br.facens.gamificacao.aluno.domain.GamificacaoException;
import br.facens.gamificacao.aluno.service.GamificacaoService;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class AlunoControllerTest {

    @Mock
    private GamificacaoService service;

    private AlunoController controller;
    private Aluno aluno;

    @BeforeEach
    void setUp() {
        controller = new AlunoController(service);
        aluno = new Aluno("maria@teste.com");
    }

    @Test
    @DisplayName("CT-API-01 - POST /alunos retorna 201")
    void matricularRetorna201() {
        when(service.matricular(anyString())).thenReturn(aluno);

        assertEquals(HttpStatus.CREATED, controller.matricular("maria@teste.com").getStatusCode());
    }

    @Test
    @DisplayName("CT-API-02 - POST /alunos/{id}/conclusoes retorna 200")
    void concluirCursoRetorna200() {
        when(service.concluirCurso(anyLong(), anyString(), any(BigDecimal.class))).thenReturn(aluno);

        assertEquals(HttpStatus.OK, controller
                .concluirCurso(1L, "Java Basico", new BigDecimal("8.5"))
                .getStatusCode());
    }

    @Test
    @DisplayName("CT-API-03 - forum, moedas, premiacao e busca retornam 200")
    void demaisEndpointsRetornam200() {
        when(service.participarDoForum(anyLong(), anyInt(), anyInt())).thenReturn(aluno);
        when(service.converterMoedas(anyLong(), anyInt(), any(DestinoMoeda.class))).thenReturn(aluno);
        when(service.premiarDestaqueDoForum()).thenReturn(aluno);
        when(service.buscar(1L)).thenReturn(aluno);

        assertEquals(HttpStatus.OK, controller.participarDoForum(1L, 3, 4).getStatusCode());
        assertEquals(HttpStatus.OK,
                controller.converterMoedas(1L, 1, DestinoMoeda.CONHECIMENTO).getStatusCode());
        assertEquals(HttpStatus.OK, controller.premiarDestaqueDoForum().getStatusCode());
        assertEquals(HttpStatus.OK, controller.buscar(1L).getStatusCode());
    }

    @Test
    @DisplayName("CT-API-04 - regra de negocio violada retorna 400 com a mensagem")
    void erroDeNegocioRetorna400() {
        ResponseEntity<String> resposta = controller.tratarRegraViolada(
                new GamificacaoException("Media deve estar entre 0,0 e 10,0"));

        assertEquals(HttpStatus.BAD_REQUEST, resposta.getStatusCode());
        assertEquals("Media deve estar entre 0,0 e 10,0", resposta.getBody());
    }
}
