package br.facens.horascomplementares.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Teste de integração da camada REST (controllers + RestControllerAdvice).
 * Usa um banco H2 isolado para não compartilhar estado com os cenários Cucumber.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "spring.datasource.url=jdbc:h2:mem:api-it;DB_CLOSE_DELAY=-1")
class ApiHorasComplementaresTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    private long cadastrarAluno(String nome) throws Exception {
        String json = mockMvc.perform(post("/api/alunos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"" + nome + "\",\"dataIngresso\":\"2023-01-01\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(json).get("id").asLong();
    }

    private long submeter(long alunoId, String categoria, String titulo, int horas) throws Exception {
        String json = mockMvc.perform(post("/api/certificados")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"alunoId\":" + alunoId + ",\"categoria\":\"" + categoria + "\",\"titulo\":\""
                                + titulo + "\",\"cargaHoraria\":" + horas + ",\"data\":\"2024-01-10\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("EM_ANALISE"))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(json).get("id").asLong();
    }

    @Test
    void fluxoCompleto_submeteAprovaEConsultaHoras() throws Exception {
        long alunoId = cadastrarAluno("Ana");
        long certId = submeter(alunoId, "EVENTO", "Palestra", 90);

        mockMvc.perform(post("/api/certificados/" + certId + "/aprovacao"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultado").value("APROVADO"));

        JsonNode horas = objectMapper.readTree(mockMvc.perform(get("/api/alunos/" + alunoId + "/horas"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString());
        assertThat(horas.get("totalContabilizado").asInt()).isEqualTo(60);
        assertThat(horas.get("totalExcedente").asInt()).isEqualTo(30);
    }

    @Test
    void reprovarComJustificativaCurta_retorna422DoRestControllerAdvice() throws Exception {
        long alunoId = cadastrarAluno("Bruno");
        long certId = submeter(alunoId, "ENSINO", "Curso", 20);

        mockMvc.perform(post("/api/certificados/" + certId + "/reprovacao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"justificativa\":\"curta\"}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.mensagem").value(Matchers.containsString("justificativa")));
    }

    @Test
    void aprovarCertificadoInexistente_retorna404() throws Exception {
        mockMvc.perform(post("/api/certificados/99999/aprovacao"))
                .andExpect(status().isNotFound());
    }

    @Test
    void listarCategorias_retornaCatalogoComTetos() throws Exception {
        mockMvc.perform(get("/api/categorias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.nome=='EVENTO')].tetoHoras").value(Matchers.hasItem(60)));
    }
}
