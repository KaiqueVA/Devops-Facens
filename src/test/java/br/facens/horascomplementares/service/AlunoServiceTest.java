package br.facens.horascomplementares.service;

import br.facens.horascomplementares.domain.Aluno;
import br.facens.horascomplementares.domain.exception.AlunoInexistenteException;
import br.facens.horascomplementares.repository.AlunoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlunoServiceTest {

    @Mock
    private AlunoRepository alunoRepository;

    private AlunoService service;

    @BeforeEach
    void setUp() {
        service = new AlunoService(alunoRepository);
    }

    @Test
    void cadastrarPersisteOAluno() {
        when(alunoRepository.save(any(Aluno.class))).thenAnswer(i -> i.getArgument(0));

        Aluno aluno = service.cadastrar("Ana", LocalDate.of(2023, 1, 1));

        assertThat(aluno.getNome()).isEqualTo("Ana");
        assertThat(aluno.getDataIngresso()).isEqualTo(LocalDate.of(2023, 1, 1));
    }

    @Test
    void listarRetornaTodosOsAlunos() {
        Aluno aluno = new Aluno("Ana", LocalDate.of(2023, 1, 1));
        when(alunoRepository.findAll()).thenReturn(List.of(aluno));

        assertThat(service.listar()).containsExactly(aluno);
    }

    @Test
    void buscarRetornaOAlunoQuandoExiste() {
        Aluno aluno = new Aluno("Ana", LocalDate.of(2023, 1, 1));
        when(alunoRepository.findById(1L)).thenReturn(Optional.of(aluno));

        assertThat(service.buscar(1L)).isSameAs(aluno);
    }

    @Test
    void buscarLancaExcecaoQuandoNaoExiste() {
        when(alunoRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscar(9L))
                .isInstanceOf(AlunoInexistenteException.class);
    }
}
