package com.github.felipex.votecoop.service;

import com.github.felipex.votecoop.domain.Pauta;
import com.github.felipex.votecoop.domain.SessaoVotacao;
import com.github.felipex.votecoop.dto.request.AbrirSessaoRequest;
import com.github.felipex.votecoop.exception.AppException;
import com.github.felipex.votecoop.repository.PautaRepository;
import com.github.felipex.votecoop.repository.SessaoVotacaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SessaoVotacaoService {

    private final SessaoVotacaoRepository sessaoRepository;
    private final PautaRepository pautaRepository;

    @Value("${app.sessao.duracao-padrao-minutos}")
    private int duracaoPadraoMinutos;

    @Transactional
    public SessaoVotacao abrirSessao(Long pautaId, AbrirSessaoRequest request) {
        Pauta pauta = pautaRepository.findById(pautaId)
                .orElseThrow(() -> new AppException(AppException.Tipo.RECURSO_NAO_ENCONTRADO, pautaId));

        boolean sessaoExistente = !sessaoRepository.findByPautaId(pautaId).isEmpty();

        if (sessaoExistente) {
            throw new AppException(AppException.Tipo.SESSAO_JA_ABERTA, pautaId);
        }

        int duracao = (request == null || request.duracaoMinutos() == null) ? duracaoPadraoMinutos : request.duracaoMinutos();
        SessaoVotacao sessao = new SessaoVotacao(pauta, duracao);
        return sessaoRepository.save(sessao);
    }

    @Transactional(readOnly = true)
    public SessaoVotacao buscarSessaoAtiva(Long pautaId) {
        return sessaoRepository
                .findFirstByPautaIdAndDataFechamentoAfterOrderByDataAberturaDesc(pautaId, LocalDateTime.now())
                .orElseThrow(() -> new AppException(AppException.Tipo.SESSAO_NAO_ATIVA, pautaId));
    }

    @Transactional(readOnly = true)
    public Optional<SessaoVotacao> buscarSessaoAtivaOpcional(Long pautaId) {
        return sessaoRepository
                .findFirstByPautaIdAndDataFechamentoAfterOrderByDataAberturaDesc(pautaId, LocalDateTime.now());
    }

    @Transactional(readOnly = true)
    public List<SessaoVotacao> buscarSessoesPorPauta(Long pautaId) {
        return sessaoRepository.findByPautaId(pautaId);
    }
}
