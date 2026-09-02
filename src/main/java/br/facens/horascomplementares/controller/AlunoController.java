package br.facens.horascomplementares.controller;

import br.facens.horascomplementares.dto.NovoAlunoDTO;
import br.facens.horascomplementares.dto.RespostasDTO.AlunoResponse;
import br.facens.horascomplementares.dto.RespostasDTO.CertificadoResponse;
import br.facens.horascomplementares.dto.RespostasDTO.ResumoHorasResponse;
import br.facens.horascomplementares.service.AlunoService;
import br.facens.horascomplementares.service.CertificadoService;
import br.facens.horascomplementares.service.HorasService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/alunos")
public class AlunoController {

    private final AlunoService alunoService;
    private final CertificadoService certificadoService;
    private final HorasService horasService;

    public AlunoController(AlunoService alunoService, CertificadoService certificadoService,
                           HorasService horasService) {
        this.alunoService = alunoService;
        this.certificadoService = certificadoService;
        this.horasService = horasService;
    }

    @PostMapping
    public ResponseEntity<AlunoResponse> cadastrar(@Valid @RequestBody NovoAlunoDTO dados) {
        AlunoResponse resposta = AlunoResponse.de(alunoService.cadastrar(dados.nome(), dados.dataIngresso()));
        return ResponseEntity.status(HttpStatus.CREATED).body(resposta);
    }

    @GetMapping
    public List<AlunoResponse> listar() {
        return alunoService.listar().stream().map(AlunoResponse::de).toList();
    }

    @GetMapping("/{id}")
    public AlunoResponse buscar(@PathVariable Long id) {
        return AlunoResponse.de(alunoService.buscar(id));
    }

    @GetMapping("/{id}/certificados")
    public List<CertificadoResponse> certificados(@PathVariable Long id) {
        return certificadoService.listarPorAluno(id).stream().map(CertificadoResponse::de).toList();
    }

    @GetMapping("/{id}/horas")
    public ResumoHorasResponse horas(@PathVariable Long id) {
        return ResumoHorasResponse.de(horasService.resumoDoAluno(id));
    }
}
