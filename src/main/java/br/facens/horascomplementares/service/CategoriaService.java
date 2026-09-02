package br.facens.horascomplementares.service;

import br.facens.horascomplementares.domain.Categoria;
import br.facens.horascomplementares.repository.CategoriaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    public CategoriaService(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    @Transactional(readOnly = true)
    public List<Categoria> listarCatalogo() {
        return categoriaRepository.findAll();
    }
}
