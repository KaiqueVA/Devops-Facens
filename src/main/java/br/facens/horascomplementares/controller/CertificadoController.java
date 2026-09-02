package br.facens.horascomplementares.controller;

import br.facens.horascomplementares.dto.ReprovacaoDTO;
import br.facens.horascomplementares.dto.RespostasDTO.CertificadoResponse;
import br.facens.horascomplementares.dto.RespostasDTO.HistoricoValidacaoResponse;
import br.facens.horascomplementares.dto.SubmissaoCertificadoDTO;
import br.facens.horascomplementares.service.CertificadoService;
import br.facens.horascomplementares.service.ValidacaoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/certificados")
public class CertificadoController {

    private final CertificadoService certificadoService;
    private final ValidacaoService validacaoService;

    public CertificadoController(CertificadoService certificadoService, ValidacaoService validacaoService) {
        this.certificadoService = certificadoService;
        this.validacaoService = validacaoService;
    }

    @PostMapping
    public ResponseEntity<CertificadoResponse> submeter(@RequestBody SubmissaoCertificadoDTO dados) {
        CertificadoResponse resposta = CertificadoResponse.de(certificadoService.submeter(dados));
        return ResponseEntity.status(HttpStatus.CREATED).body(resposta);
    }

    @PostMapping("/{id}/aprovacao")
    public HistoricoValidacaoResponse aprovar(@PathVariable Long id) {
        return HistoricoValidacaoResponse.de(validacaoService.aprovar(id));
    }

    @PostMapping("/{id}/reprovacao")
    public HistoricoValidacaoResponse reprovar(@PathVariable Long id, @RequestBody ReprovacaoDTO dados) {
        return HistoricoValidacaoResponse.de(validacaoService.reprovar(id, dados.justificativa()));
    }
}
