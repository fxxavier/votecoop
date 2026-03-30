package com.github.felipex.votecoop.repository;

import com.github.felipex.votecoop.domain.OpcaoVoto;
import com.github.felipex.votecoop.domain.Voto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface VotoRepository extends JpaRepository<Voto, Long> {

    // Verifica se associado já votou em qualquer sessão da pauta
    @Query("SELECT COUNT(v) > 0 FROM Voto v WHERE v.sessao.pauta.id = :pautaId AND v.associadoId = :associadoId")
    boolean existsByPautaIdAndAssociadoId(Long pautaId, String associadoId);

    @Query("SELECT COUNT(v) FROM Voto v WHERE v.sessao.pauta.id = :pautaId AND v.opcao = :opcao")
    long countByPautaIdAndOpcao(Long pautaId, OpcaoVoto opcao);

    @Query("SELECT COUNT(v) FROM Voto v WHERE v.sessao.pauta.id = :pautaId")
    long countByPautaId(Long pautaId);
}
