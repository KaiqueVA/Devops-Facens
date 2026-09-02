package br.facens.horascomplementares.dto;

import br.facens.horascomplementares.domain.Aluno;
import br.facens.horascomplementares.domain.Categoria;
import br.facens.horascomplementares.domain.Certificado;
import br.facens.horascomplementares.domain.HistoricoValidacao;
import br.facens.horascomplementares.domain.ResumoHoras;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Agrupa os records de resposta da API (camada de borda, fora da cobertura de negócio).
 */
public final class RespostasDTO {

    private RespostasDTO() {
    }

    public record AlunoResponse(Long id, String nome, LocalDate dataIngresso) {
        public static AlunoResponse de(Aluno aluno) {
            return new AlunoResponse(aluno.getId(), aluno.getNome(), aluno.getDataIngresso());
        }
    }

    public record CategoriaResponse(String nome, int tetoHoras) {
        public static CategoriaResponse de(Categoria categoria) {
            return new CategoriaResponse(categoria.getNome(), categoria.getTetoHoras());
        }
    }

    public record CertificadoResponse(Long id, String titulo, String categoria, int cargaHoraria,
                                      LocalDate data, String status, String justificativaReprovacao) {
        public static CertificadoResponse de(Certificado c) {
            return new CertificadoResponse(c.getId(), c.getTitulo(), c.getCategoria().getNome(),
                    c.getCargaHoraria(), c.getData(), c.getStatus().name(), c.getJustificativaReprovacao());
        }
    }

    public record HistoricoValidacaoResponse(Long id, String resultado, String justificativa,
                                             LocalDateTime dataHora) {
        public static HistoricoValidacaoResponse de(HistoricoValidacao h) {
            return new HistoricoValidacaoResponse(h.getId(), h.getResultado().name(),
                    h.getJustificativa(), h.getDataHora());
        }
    }

    public record ContribuicaoResponse(String categoria, int horasEnviadas, int horasContabilizadas,
                                       int horasExcedentes) {
    }

    public record ResumoHorasResponse(int totalContabilizado, int totalExcedente, int totalExigido,
                                      int percentualProgresso, List<ContribuicaoResponse> categorias) {
        public static ResumoHorasResponse de(ResumoHoras resumo) {
            int exigido = br.facens.horascomplementares.domain.CalculadoraDeHoras.TOTAL_MAXIMO;
            int percentual = resumo.totalContabilizado() * 100 / exigido;
            List<ContribuicaoResponse> categorias = resumo.contribuicoes().stream()
                    .map(c -> new ContribuicaoResponse(c.categoria(), c.horasEnviadas(),
                            c.horasContabilizadas(), c.horasExcedentes()))
                    .toList();
            return new ResumoHorasResponse(resumo.totalContabilizado(), resumo.totalExcedente(),
                    exigido, percentual, categorias);
        }
    }
}
