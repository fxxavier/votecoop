package com.github.felipex.votecoop.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "votos",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_sessao_associado",
        columnNames = {"sessao_id", "associado_id"}
    )
)
@Getter
@Setter
@NoArgsConstructor
public class Voto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sessao_id", nullable = false)
    private SessaoVotacao sessao;

    @Column(name = "associado_id", nullable = false)
    private String associadoId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OpcaoVoto opcao;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime registradoEm;

    public Voto(SessaoVotacao sessao, String associadoId, OpcaoVoto opcao) {
        this.sessao = sessao;
        this.associadoId = associadoId;
        this.opcao = opcao;
    }
}
