package br.facens.gamificacao.aluno.web;

import br.facens.gamificacao.aluno.domain.Aluno;
import br.facens.gamificacao.aluno.domain.GamificacaoException;
import br.facens.gamificacao.aluno.dto.AlunoResponse;
import br.facens.gamificacao.aluno.dto.ConcluirCursoRequest;
import br.facens.gamificacao.aluno.dto.ConverterMoedasRequest;
import br.facens.gamificacao.aluno.dto.ForumRequest;
import br.facens.gamificacao.aluno.dto.MatricularRequest;
import br.facens.gamificacao.aluno.service.GamificacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/alunos")
@Tag(name = "Alunos", description = "Gamificacao para engajamento de educacao continuada")
public class AlunoController {

    private final GamificacaoService service;

    public AlunoController(GamificacaoService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Matricula um aluno na assinatura basica")
    public ResponseEntity<AlunoResponse> matricular(@RequestBody MatricularRequest request) {
        Aluno aluno = service.matricular(request.email());
        return ResponseEntity.status(HttpStatus.CREATED).body(AlunoResponse.from(aluno));
    }

    @PostMapping("/{id}/conclusoes")
    @Operation(summary = "Conclui um curso (RN2: media acima de 7,0 libera 3 cursos)")
    public ResponseEntity<AlunoResponse> concluirCurso(@PathVariable Long id,
                                                        @RequestBody ConcluirCursoRequest request) {
        Aluno aluno = service.concluirCurso(id, request.curso(), request.media());
        return ResponseEntity.ok(AlunoResponse.from(aluno));
    }

    @PostMapping("/{id}/forum")
    @Operation(summary = "Registra participacao no forum (topicos e comentarios do mes)")
    public ResponseEntity<AlunoResponse> participarDoForum(@PathVariable Long id,
                                                            @RequestBody ForumRequest request) {
        Aluno aluno = service.participarDoForum(id, request.topicos(), request.comentarios());
        return ResponseEntity.ok(AlunoResponse.from(aluno));
    }

    @PostMapping("/{id}/moedas")
    @Operation(summary = "Converte moedas em conhecimento, acumulo ou criptomoeda (RN5)")
    public ResponseEntity<AlunoResponse> converterMoedas(@PathVariable Long id,
                                                          @RequestBody ConverterMoedasRequest request) {
        Aluno aluno = service.converterMoedas(id, request.quantidade(), request.destino());
        return ResponseEntity.ok(AlunoResponse.from(aluno));
    }

    @PostMapping("/forum/premiacao")
    @Operation(summary = "Premia o destaque do forum no fechamento do mes (RN3)")
    public ResponseEntity<AlunoResponse> premiarDestaqueDoForum() {
        Aluno aluno = service.premiarDestaqueDoForum();
        return ResponseEntity.ok(AlunoResponse.from(aluno));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um aluno pelo id")
    public ResponseEntity<AlunoResponse> buscar(@PathVariable Long id) {
        Aluno aluno = service.buscar(id);
        return ResponseEntity.ok(AlunoResponse.from(aluno));
    }

    @ExceptionHandler(GamificacaoException.class)
    public ResponseEntity<String> tratarRegraViolada(GamificacaoException erro) {
        return ResponseEntity.badRequest().body(erro.getMessage());
    }
}
