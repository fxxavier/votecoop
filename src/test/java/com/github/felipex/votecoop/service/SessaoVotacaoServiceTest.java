package com.github.felipex.votecoop.service;

import com.github.felipex.votecoop.domain.Pauta;
import com.github.felipex.votecoop.domain.SessaoVotacao;
import com.github.felipex.votecoop.dto.request.AbrirSessaoRequest;
import com.github.felipex.votecoop.exception.AppException;
import com.github.felipex.votecoop.repository.PautaRepository;
import com.github.felipex.votecoop.repository.SessaoVotacaoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SessaoVotacaoServiceTest {

    @Mock
    private SessaoVotacaoRepository sessaoRepository;

    @Mock
    private PautaRepository pautaRepository;

    @InjectMocks
    private SessaoVotacaoService sessaoVotacaoService;

    private final Pauta pauta = new Pauta("Titulo", "Descricao");

    void setUp() {
        ReflectionTestUtils.setField(sessaoVotacaoService, "duracaoPadraoMinutos", 1);
    }

    @Test
    void abrirSessao_deveCriarSessaoComDuracaoPadrao() {
        ReflectionTestUtils.setField(sessaoVotacaoService, "duracaoPadraoMinutos", 1);
        when(pautaRepository.findById(1L)).thenReturn(Optional.of(pauta));
        when(sessaoRepository.findByPautaId(1L)).thenReturn(List.of());
        when(sessaoRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        var sessao = sessaoVotacaoService.abrirSessao(1L, null);

        assertThat(sessao).isNotNull();
        assertThat(sessao.getDataFechamento()).isAfter(sessao.getDataAbertura());
    }

    @Test
    void abrirSessao_deveCriarSessaoComDuracaoInformada() {
        ReflectionTestUtils.setField(sessaoVotacaoService, "duracaoPadraoMinutos", 1);
        when(pautaRepository.findById(1L)).thenReturn(Optional.of(pauta));
        when(sessaoRepository.findByPautaId(1L)).thenReturn(List.of());
        when(sessaoRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        var sessao = sessaoVotacaoService.abrirSessao(1L, new AbrirSessaoRequest(10));

        assertThat(sessao.getDataFechamento())
                .isAfterOrEqualTo(sessao.getDataAbertura().plusMinutes(9));
    }

    @Test
    void abrirSessao_deveLancarExcecaoQuandoPautaNaoEncontrada() {
        when(pautaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sessaoVotacaoService.abrirSessao(99L, null))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).tipo)
                        .isEqualTo(AppException.Tipo.RECURSO_NAO_ENCONTRADO));
    }

    @Test
    void abrirSessao_deveLancarExcecaoQuandoSessaoJaExiste() {
        when(pautaRepository.findById(1L)).thenReturn(Optional.of(pauta));
        when(sessaoRepository.findByPautaId(1L)).thenReturn(List.of(new SessaoVotacao(pauta, 1)));

        assertThatThrownBy(() -> sessaoVotacaoService.abrirSessao(1L, null))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).tipo)
                        .isEqualTo(AppException.Tipo.SESSAO_JA_ABERTA));
    }

    @Test
    void buscarSessaoAtiva_deveRetornarSessaoAtiva() {
        var sessao = new SessaoVotacao(pauta, 60);
        when(sessaoRepository.findFirstByPautaIdAndDataFechamentoAfterOrderByDataAberturaDesc(
                eq(1L), any(LocalDateTime.class))).thenReturn(Optional.of(sessao));

        var resultado = sessaoVotacaoService.buscarSessaoAtiva(1L);

        assertThat(resultado).isNotNull();
        assertThat(resultado.isAtiva()).isTrue();
    }

    @Test
    void buscarSessaoAtiva_deveLancarExcecaoQuandoNaoHaSessaoAtiva() {
        when(sessaoRepository.findFirstByPautaIdAndDataFechamentoAfterOrderByDataAberturaDesc(
                eq(1L), any(LocalDateTime.class))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sessaoVotacaoService.buscarSessaoAtiva(1L))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).tipo)
                        .isEqualTo(AppException.Tipo.SESSAO_NAO_ATIVA));
    }
}
