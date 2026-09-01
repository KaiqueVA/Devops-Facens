package br.facens.horascomplementares.repository;

import br.facens.horascomplementares.domain.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    Optional<Categoria> findByNome(String nome);
}
