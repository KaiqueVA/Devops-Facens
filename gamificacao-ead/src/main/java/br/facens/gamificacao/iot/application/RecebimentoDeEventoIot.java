package br.facens.gamificacao.iot.application;

import br.facens.gamificacao.gamificacao.api.ConsultaDeAluno;
import br.facens.gamificacao.iot.api.EventoDeLaboratorioRecebido;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * RF06: valida, deduplica, guarda e encaminha o evento de laboratorio.
 * Idempotente por eventoId: o gateway pode reenviar quantas vezes precisar.
 */
@Service
public class RecebimentoDeEventoIot {

    /** Eventos guardados no dispositivo por muito tempo ainda sao aceitos, mas nao do futuro. */
    static final Duration TOLERANCIA_FUTURO = Duration.ofMinutes(5);

    private final EventoIotRepository repositorio;
    private final ConsultaDeAluno alunos;
    private final ApplicationEventPublisher eventos;
    private final Clock relogio = Clock.systemUTC();
    private final AtomicLong novos = new AtomicLong();
    private final AtomicLong duplicados = new AtomicLong();
    private final AtomicLong invalidos = new AtomicLong();

    public RecebimentoDeEventoIot(EventoIotRepository repositorio, ConsultaDeAluno alunos,
                                  ApplicationEventPublisher eventos) {
        this.repositorio = repositorio;
        this.alunos = alunos;
        this.eventos = eventos;
    }

    @Transactional
    public ResultadoRecebimento receber(ComandoEventoIot c) {
        validar(c);
        if (repositorio.existsByEventoId(c.eventoId())) {
            duplicados.incrementAndGet();
            return ResultadoRecebimento.DUPLICADO;
        }
        // Corrida entre duas reentregas simultaneas: a unique (uk_evento_iot) barra a segunda,
        // que volta como erro; o gateway reenvia e, na proxima, cai no existsByEventoId -> DUPLICADO.
        repositorio.saveAndFlush(new EventoIot(c, Instant.now(relogio)));
        eventos.publishEvent(new EventoDeLaboratorioRecebido(c.eventoId(), c.laboratorio(), c.dispositivo(),
                c.tipo(), c.alunoId(), c.atividade(), c.ocorridoEm()));
        novos.incrementAndGet();
        return ResultadoRecebimento.NOVO;
    }

    private void validar(ComandoEventoIot c) {
        String erro = null;
        if (c.eventoId() == null || c.eventoId().isBlank()) {
            erro = "eventoId obrigatorio";
        } else if (c.tipo() == null || c.alunoId() == null || c.ocorridoEm() == null
                || c.dispositivo() == null || c.laboratorio() == null) {
            erro = "tipo, alunoId, ocorridoEm, dispositivo e laboratorio sao obrigatorios";
        } else if (c.ocorridoEm().isAfter(Instant.now(relogio).plus(TOLERANCIA_FUTURO))) {
            erro = "ocorridoEm no futuro";
        } else if (alunos.buscarResumo(c.alunoId()).isEmpty()) {
            erro = "aluno inexistente: " + c.alunoId();
        }
        if (erro != null) {
            invalidos.incrementAndGet();
            throw new EventoIotInvalidoException(erro);
        }
    }

    public long getNovos() {
        return novos.get();
    }

    public long getDuplicados() {
        return duplicados.get();
    }

    public long getInvalidos() {
        return invalidos.get();
    }
}
