package br.facens.horascomplementares.service;

import br.facens.horascomplementares.domain.HistoricoValidacao;
import br.facens.horascomplementares.repository.CertificadoRepository;
import br.facens.horascomplementares.repository.HistoricoValidacaoRepository;
import org.springframework.stereotype.Service;

import java.time.Clock;

@Service
public class ValidacaoService {

    private final CertificadoRepository certificadoRepository;
    private final HistoricoValidacaoRepository historicoRepository;
    private final Clock clock;

    public ValidacaoService(CertificadoRepository certificadoRepository,
                            HistoricoValidacaoRepository historicoRepository,
                            Clock clock) {
        this.certificadoRepository = certificadoRepository;
        this.historicoRepository = historicoRepository;
        this.clock = clock;
    }

    public HistoricoValidacao aprovar(Long certificadoId) {
        throw new UnsupportedOperationException("aprovar ainda nao implementado");
    }

    public HistoricoValidacao reprovar(Long certificadoId, String justificativa) {
        throw new UnsupportedOperationException("reprovar ainda nao implementado");
    }
}
