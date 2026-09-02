package br.facens.horascomplementares.controller;

import br.facens.horascomplementares.dto.RespostasDTO.CategoriaResponse;
import br.facens.horascomplementares.service.CategoriaService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @GetMapping
    public List<CategoriaResponse> listar() {
        return categoriaService.listarCatalogo().stream().map(CategoriaResponse::de).toList();
    }
}
