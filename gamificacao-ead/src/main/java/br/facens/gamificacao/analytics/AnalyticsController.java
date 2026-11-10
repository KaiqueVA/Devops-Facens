package br.facens.gamificacao.analytics;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Analytics", description = "Registro de aprendizagem (consumidor interno do evento CursoConcluido)")
public class AnalyticsController {

    private final RegistroDeAprendizagemRepository repositorio;
    private final RegistroDeLaboratorioRepository laboratorio;

    public AnalyticsController(RegistroDeAprendizagemRepository repositorio,
                               RegistroDeLaboratorioRepository laboratorio) {
        this.repositorio = repositorio;
        this.laboratorio = laboratorio;
    }

    @GetMapping("/analytics/resumo")
    @Operation(summary = "Totais de conclusoes registradas (consistencia eventual)")
    public Map<String, Object> resumo() {
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("conclusoes", repositorio.count());
        r.put("conquistas", repositorio.countByConquistadoTrue());
        r.put("promocoesPremium", repositorio.countByPromovidoAPremiumTrue());
        Double media = repositorio.mediaGeral();
        r.put("mediaGeral", media == null ? 0.0 : Math.round(media * 100) / 100.0);
        r.put("eventosDeLaboratorio", laboratorio.count());
        return r;
    }
}
