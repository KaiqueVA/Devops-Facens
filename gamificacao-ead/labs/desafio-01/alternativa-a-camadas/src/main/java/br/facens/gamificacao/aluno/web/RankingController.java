package br.facens.gamificacao.aluno.web;

import br.facens.gamificacao.aluno.domain.Aluno;
import br.facens.gamificacao.aluno.dto.RankingResponse;
import br.facens.gamificacao.aluno.service.RankingService;
import java.util.ArrayList;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RankingController {

    private final RankingService service;

    public RankingController(RankingService service) {
        this.service = service;
    }

    @GetMapping("/ranking")
    public List<RankingResponse> consultar(@RequestParam(defaultValue = "0") int pagina,
                                           @RequestParam(defaultValue = "10") int tamanho) {
        List<Aluno> alunos = service.consultar(pagina, tamanho);
        List<RankingResponse> resposta = new ArrayList<>();
        for (int i = 0; i < alunos.size(); i++) {
            resposta.add(RankingResponse.from(pagina * tamanho + i + 1, alunos.get(i)));
        }
        return resposta;
    }
}
