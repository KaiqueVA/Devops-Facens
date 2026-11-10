package br.facens.gamificacao.ranking.web;

import br.facens.gamificacao.ranking.application.PosicaoRanking;
import br.facens.gamificacao.ranking.application.RankingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ranking")
@Tag(name = "Ranking", description = "RF01 - ranking de alunos (top N paginado)")
public class RankingController {

    private final RankingService service;

    public RankingController(RankingService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Top N por cursos conquistados e moedas (pagina comeca em 0, tamanho max. 100)")
    public List<PosicaoRanking> consultar(@RequestParam(defaultValue = "0") int pagina,
                                          @RequestParam(defaultValue = "10") int tamanho) {
        return service.consultar(pagina, tamanho);
    }
}
