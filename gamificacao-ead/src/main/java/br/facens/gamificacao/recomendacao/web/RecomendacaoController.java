package br.facens.gamificacao.recomendacao.web;

import br.facens.gamificacao.recomendacao.application.AlunoSemCadastroException;
import br.facens.gamificacao.recomendacao.application.AssistenteIndisponivelException;
import br.facens.gamificacao.recomendacao.application.RecomendacaoService;
import br.facens.gamificacao.recomendacao.application.Recomendacoes;
import br.facens.gamificacao.recomendacao.application.port.AssistenteEducacional;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "IA", description = "RF03 recomendacao e RF04 assistente (via ia-service, com degradacao)")
public class RecomendacaoController {

    private final RecomendacaoService service;

    public RecomendacaoController(RecomendacaoService service) {
        this.service = service;
    }

    @GetMapping("/alunos/{id}/recomendacoes")
    @Operation(summary = "Cursos recomendados; se a IA cair, devolve o catalogo padrao com degradado=true")
    public Recomendacoes recomendar(@PathVariable Long id) {
        return service.recomendar(id);
    }

    @PostMapping("/assistente/perguntas")
    @Operation(summary = "Pergunta ao assistente educacional (RAG); 503 se a IA estiver indisponivel")
    public AssistenteEducacional.Resposta perguntar(@RequestBody PerguntaRequest request) {
        return service.perguntar(request.pergunta());
    }

    @ExceptionHandler(AlunoSemCadastroException.class)
    public ResponseEntity<Map<String, String>> alunoInexistente(AlunoSemCadastroException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("erro", e.getMessage()));
    }

    @ExceptionHandler(AssistenteIndisponivelException.class)
    public ResponseEntity<Map<String, String>> assistenteIndisponivel(AssistenteIndisponivelException e) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .header("Retry-After", "30")
                .body(Map.of("erro", e.getMessage()));
    }

    public record PerguntaRequest(String pergunta) {
    }
}
