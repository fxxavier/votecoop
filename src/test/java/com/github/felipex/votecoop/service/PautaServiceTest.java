package com.github.felipex.votecoop.service;

import com.github.felipex.votecoop.domain.Pauta;
import com.github.felipex.votecoop.dto.request.CadastrarPautaRequest;
import com.github.felipex.votecoop.exception.AppException;
import com.github.felipex.votecoop.repository.PautaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PautaServiceTest {

    @Mock
    private PautaRepository pautaRepository;

    @InjectMocks
    private PautaService pautaService;

    @Test
    void cadastrar_deveSalvarERetornarPauta() {
        var request = new CadastrarPautaRequest("Titulo", "Descricao");
        var pauta = new Pauta("Titulo", "Descricao");
        when(pautaRepository.save(any(Pauta.class))).thenReturn(pauta);

        var resultado = pautaService.cadastrar(request);

        assertThat(resultado.getTitulo()).isEqualTo("Titulo");
        verify(pautaRepository).save(any(Pauta.class));
    }

    @Test
    void listarTodas_deveRetornarListaDePautas() {
        var pautas = List.of(new Pauta("A", null), new Pauta("B", null));
        when(pautaRepository.findAll()).thenReturn(pautas);

        var resultado = pautaService.listarTodas();

        assertThat(resultado).hasSize(2);
    }

    @Test
    void buscarPorId_deveRetornarPautaQuandoEncontrada() {
        var pauta = new Pauta("Titulo", "Descricao");
        when(pautaRepository.findById(1L)).thenReturn(Optional.of(pauta));

        var resultado = pautaService.buscarPorId(1L);

        assertThat(resultado.getTitulo()).isEqualTo("Titulo");
    }

    @Test
    void buscarPorId_deveLancarExcecaoQuandoNaoEncontrada() {
        when(pautaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pautaService.buscarPorId(99L))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).tipo)
                        .isEqualTo(AppException.Tipo.RECURSO_NAO_ENCONTRADO));
    }
}
