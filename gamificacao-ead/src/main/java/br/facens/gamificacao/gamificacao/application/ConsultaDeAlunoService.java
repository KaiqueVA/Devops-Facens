package br.facens.gamificacao.gamificacao.application;

import br.facens.gamificacao.gamificacao.api.ConsultaDeAluno;
import br.facens.gamificacao.gamificacao.api.ResumoDoAluno;
import br.facens.gamificacao.gamificacao.application.port.AlunoRepositorio;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ConsultaDeAlunoService implements ConsultaDeAluno {

    private final AlunoRepositorio repositorio;

    public ConsultaDeAlunoService(AlunoRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    public Optional<ResumoDoAluno> buscarResumo(Long alunoId) {
        return repositorio.buscarPorId(alunoId)
                .map(a -> new ResumoDoAluno(a.getId(), a.getPlano().name(), a.getCursosConcluidos(),
                        a.getCursosConquistados(), a.getMoedas()));
    }
}
