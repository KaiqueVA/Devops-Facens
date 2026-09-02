package br.facens.horascomplementares.service;

import br.facens.horascomplementares.domain.Categoria;
import br.facens.horascomplementares.repository.CategoriaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoriaServiceTest {

    @Mock
    private CategoriaRepository categoriaRepository;

    @Test
    void listarCatalogoRetornaAsCategoriasDoRepositorio() {
        List<Categoria> catalogo = List.of(new Categoria("ENSINO", 80), new Categoria("EVENTO", 60));
        when(categoriaRepository.findAll()).thenReturn(catalogo);

        CategoriaService service = new CategoriaService(categoriaRepository);

        assertThat(service.listarCatalogo()).isEqualTo(catalogo);
    }
}
