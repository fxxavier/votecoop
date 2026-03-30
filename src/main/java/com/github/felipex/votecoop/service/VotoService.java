package com.github.felipex.votecoop.service;

import com.github.felipex.votecoop.domain.OpcaoVoto;
import com.github.felipex.votecoop.domain.SessaoVotacao;
import com.github.felipex.votecoop.domain.Voto;
import com.github.felipex.votecoop.dto.request.RegistrarVotoRequest;
import com.github.felipex.votecoop.client.UserCheckClient;
import com.github.felipex.votecoop.exception.AppException;
import com.github.felipex.votecoop.repository.VotoRepository;
import com.github.felipex.votecoop.util.CpfUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VotoService {

    private final VotoRepository votoRepository;
    private final SessaoVotacaoService sessaoService;
    private final UserCheckClient userCheckClient;

    @Transactional
    public Voto registrarVoto(Long pautaId, RegistrarVotoRequest request) {
        if (!CpfUtils.isValido(request.associadoId())) {
            throw new AppException(AppException.Tipo.CPF_INVALIDO, request.associadoId());
        }

        userCheckClient.verificarAssociado(request.associadoId());

        SessaoVotacao sessao = sessaoService.buscarSessaoAtiva(pautaId);

        if (!sessao.isAtiva()) {
            throw new AppException(AppException.Tipo.SESSAO_ENCERRADA, pautaId);
        }

        boolean jaVotou = votoRepository.existsByPautaIdAndAssociadoId(pautaId, request.associadoId());
        if (jaVotou) {
            throw new AppException(AppException.Tipo.VOTO_DUPLICADO, request.associadoId());
        }

        Voto voto = new Voto(sessao, request.associadoId(), request.opcao());
        return votoRepository.save(voto);
    }

    public record ResultadoVotacao(
            String tituloPauta,
            long totalVotos,
            long votosSim,
            long votosNao,
            String resultado
    ) {}

    @Transactional(readOnly = true)
    public ResultadoVotacao contabilizarVotos(Long pautaId, String tituloPauta) {
        long totalVotos = votoRepository.countByPautaId(pautaId);
        long votosSim = votoRepository.countByPautaIdAndOpcao(pautaId, OpcaoVoto.SIM);
        long votosNao = votoRepository.countByPautaIdAndOpcao(pautaId, OpcaoVoto.NAO);

        String resultado;
        if (totalVotos == 0) {
            resultado = "Sem votos registrados";
        } else if (votosSim > votosNao) {
            resultado = "APROVADA";
        } else if (votosNao > votosSim) {
            resultado = "REPROVADA";
        } else {
            resultado = "EMPATE";
        }

        return new ResultadoVotacao(tituloPauta, totalVotos, votosSim, votosNao, resultado);
    }
}
