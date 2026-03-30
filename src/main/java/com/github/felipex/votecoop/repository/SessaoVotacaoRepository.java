package com.github.felipex.votecoop.repository;

import com.github.felipex.votecoop.domain.SessaoVotacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SessaoVotacaoRepository extends JpaRepository<SessaoVotacao, Long> {

    Optional<SessaoVotacao> findFirstByPautaIdAndDataFechamentoAfterOrderByDataAberturaDesc(
            Long pautaId, LocalDateTime agora);

    List<SessaoVotacao> findByPautaId(Long pautaId);
}
