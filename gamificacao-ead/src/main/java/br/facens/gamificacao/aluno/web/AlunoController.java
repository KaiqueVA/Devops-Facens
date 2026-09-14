package br.facens.gamificacao.aluno.web;

import br.facens.gamificacao.aluno.domain.Aluno;
import br.facens.gamificacao.aluno.domain.DestinoMoeda;
import br.facens.gamificacao.aluno.domain.GamificacaoException;
import br.facens.gamificacao.aluno.service.GamificacaoService;
import java.math.BigDecimal;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/alunos")
public class AlunoController {

    private final GamificacaoService service;

    public AlunoController(GamificacaoService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Aluno> matricular(@RequestParam String email) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.matricular(email));
    }

    @PostMapping("/{id}/conclusoes")
    public ResponseEntity<Aluno> concluirCurso(@PathVariable Long id,
                                               @RequestParam String curso,
                                               @RequestParam BigDecimal media) {
        return ResponseEntity.ok(service.concluirCurso(id, curso, media));
    }

    @PostMapping("/{id}/forum")
    public ResponseEntity<Aluno> participarDoForum(@PathVariable Long id,
                                                   @RequestParam int topicos,
                                                   @RequestParam int comentarios) {
        return ResponseEntity.ok(service.participarDoForum(id, topicos, comentarios));
    }

    @PostMapping("/{id}/moedas")
    public ResponseEntity<Aluno> converterMoedas(@PathVariable Long id,
                                                 @RequestParam int quantidade,
                                                 @RequestParam DestinoMoeda destino) {
        return ResponseEntity.ok(service.converterMoedas(id, quantidade, destino));
    }

    @PostMapping("/forum/premiacao")
    public ResponseEntity<Aluno> premiarDestaqueDoForum() {
        return ResponseEntity.ok(service.premiarDestaqueDoForum());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Aluno> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscar(id));
    }

    @ExceptionHandler(GamificacaoException.class)
    public ResponseEntity<String> tratarRegraViolada(GamificacaoException erro) {
        return ResponseEntity.badRequest().body(erro.getMessage());
    }
}
