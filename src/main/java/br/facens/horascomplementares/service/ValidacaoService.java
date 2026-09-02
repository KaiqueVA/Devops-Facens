package br.facens.horascomplementares.service;

import br.facens.horascomplementares.domain.Certificado;
import br.facens.horascomplementares.domain.HistoricoValidacao;
import br.facens.horascomplementares.domain.exception.CertificadoInexistenteException;
import br.facens.horascomplementares.repository.CertificadoRepository;
import br.facens.horascomplementares.repository.HistoricoValidacaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

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

    @Transactional
    public HistoricoValidacao aprovar(Long certificadoId) {
        Certificado certificado = buscar(certificadoId);
        certificado.aprovar();
        certificadoRepository.save(certificado);
        return historicoRepository.save(
                HistoricoValidacao.aprovacao(certificado, LocalDateTime.now(clock)));
    }

    @Transactional
    public HistoricoValidacao reprovar(Long certificadoId, String justificativa) {
        Certificado certificado = buscar(certificadoId);
        certificado.reprovar(justificativa);
        certificadoRepository.save(certificado);
        return historicoRepository.save(
                HistoricoValidacao.reprovacao(certificado, justificativa, LocalDateTime.now(clock)));
    }

    private Certificado buscar(Long certificadoId) {
        return certificadoRepository.findById(certificadoId)
                .orElseThrow(() -> new CertificadoInexistenteException(certificadoId));
    }
}
