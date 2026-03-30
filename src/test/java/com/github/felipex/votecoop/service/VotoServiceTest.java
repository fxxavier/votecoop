package com.github.felipex.votecoop.service;

import com.github.felipex.votecoop.client.UserCheckClient;
import com.github.felipex.votecoop.domain.OpcaoVoto;
import com.github.felipex.votecoop.domain.Pauta;
import com.github.felipex.votecoop.domain.SessaoVotacao;
import com.github.felipex.votecoop.domain.Voto;
import com.github.felipex.votecoop.dto.request.RegistrarVotoRequest;
import com.github.felipex.votecoop.exception.AppException;
import com.github.felipex.votecoop.repository.VotoRepository;
import com.github.felipex.votecoop.util.CpfUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VotoServiceTest {

    @Mock
    private VotoRepository votoRepository;

    @Mock
    private SessaoVotacaoService sessaoService;

    @Mock
    private UserCheckClient userCheckClient;

    @InjectMocks
    private VotoService votoService;

    private SessaoVotacao sessaoAtiva;
    private String cpf;

    @BeforeEach
    void setUp() {
        var pauta = new Pauta("Titulo", "Descricao");
        sessaoAtiva = new SessaoVotacao(pauta, 60);
        cpf = CpfUtils.gerarAleatorio();
    }

    @Test
    void registrarVoto_deveRegistrarVotoComSucesso() {
        var request = new RegistrarVotoRequest(cpf, OpcaoVoto.SIM);
        when(sessaoService.buscarSessaoAtiva(1L)).thenReturn(sessaoAtiva);
        when(votoRepository.existsByPautaIdAndAssociadoId(1L, cpf)).thenReturn(false);
        when(votoRepository.save(any(Voto.class))).thenAnswer(i -> i.getArgument(0));

        var voto = votoService.registrarVoto(1L, request);

        assertThat(voto.getOpcao()).isEqualTo(OpcaoVoto.SIM);
        assertThat(voto.getAssociadoId()).isEqualTo(cpf);
        verify(userCheckClient).verificarAssociado(cpf);
        verify(votoRepository).save(any(Voto.class));
    }

    @Test
    void registrarVoto_deveLancarExcecaoQuandoCpfInvalido() {
        var request = new RegistrarVotoRequest("cpf-invalido", OpcaoVoto.SIM);

        assertThatThrownBy(() -> votoService.registrarVoto(1L, request))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).tipo)
                        .isEqualTo(AppException.Tipo.CPF_INVALIDO));

        verifyNoInteractions(userCheckClient, sessaoService, votoRepository);
    }

    @Test
    void registrarVoto_deveLancarExcecaoQuandoVotoDuplicado() {
        var request = new RegistrarVotoRequest(cpf, OpcaoVoto.SIM);
        when(sessaoService.buscarSessaoAtiva(1L)).thenReturn(sessaoAtiva);
        when(votoRepository.existsByPautaIdAndAssociadoId(1L, cpf)).thenReturn(true);

        assertThatThrownBy(() -> votoService.registrarVoto(1L, request))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).tipo)
                        .isEqualTo(AppException.Tipo.VOTO_DUPLICADO));
    }

    @Test
    void registrarVoto_deveLancarExcecaoQuandoSessaoEncerrada() {
        var pauta = new Pauta("Titulo", "Descricao");
        var sessaoEncerrada = new SessaoVotacao(pauta, 0);
        var request = new RegistrarVotoRequest(cpf, OpcaoVoto.SIM);
        when(sessaoService.buscarSessaoAtiva(1L)).thenReturn(sessaoEncerrada);

        assertThatThrownBy(() -> votoService.registrarVoto(1L, request))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).tipo)
                        .isEqualTo(AppException.Tipo.SESSAO_ENCERRADA));
    }

    @Test
    void contabilizarVotos_deveRetornarAprovadaQuandoSimMaior() {
        when(votoRepository.countByPautaId(1L)).thenReturn(3L);
        when(votoRepository.countByPautaIdAndOpcao(1L, OpcaoVoto.SIM)).thenReturn(2L);
        when(votoRepository.countByPautaIdAndOpcao(1L, OpcaoVoto.NAO)).thenReturn(1L);

        var resultado = votoService.contabilizarVotos(1L, "Titulo");

        assertThat(resultado.resultado()).isEqualTo("APROVADA");
        assertThat(resultado.totalVotos()).isEqualTo(3L);
    }

    @Test
    void contabilizarVotos_deveRetornarReprovadaQuandoNaoMaior() {
        when(votoRepository.countByPautaId(1L)).thenReturn(3L);
        when(votoRepository.countByPautaIdAndOpcao(1L, OpcaoVoto.SIM)).thenReturn(1L);
        when(votoRepository.countByPautaIdAndOpcao(1L, OpcaoVoto.NAO)).thenReturn(2L);

        var resultado = votoService.contabilizarVotos(1L, "Titulo");

        assertThat(resultado.resultado()).isEqualTo("REPROVADA");
    }

    @Test
    void contabilizarVotos_deveRetornarEmpateQuandoIgual() {
        when(votoRepository.countByPautaId(1L)).thenReturn(2L);
        when(votoRepository.countByPautaIdAndOpcao(1L, OpcaoVoto.SIM)).thenReturn(1L);
        when(votoRepository.countByPautaIdAndOpcao(1L, OpcaoVoto.NAO)).thenReturn(1L);

        var resultado = votoService.contabilizarVotos(1L, "Titulo");

        assertThat(resultado.resultado()).isEqualTo("EMPATE");
    }

    @Test
    void contabilizarVotos_deveRetornarSemVotosQuandoNaoHaVotos() {
        when(votoRepository.countByPautaId(1L)).thenReturn(0L);
        when(votoRepository.countByPautaIdAndOpcao(1L, OpcaoVoto.SIM)).thenReturn(0L);
        when(votoRepository.countByPautaIdAndOpcao(1L, OpcaoVoto.NAO)).thenReturn(0L);

        var resultado = votoService.contabilizarVotos(1L, "Titulo");

        assertThat(resultado.resultado()).isEqualTo("Sem votos registrados");
        assertThat(resultado.totalVotos()).isZero();
    }
}
