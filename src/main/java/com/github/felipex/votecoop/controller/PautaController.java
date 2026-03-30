package com.github.felipex.votecoop.controller;

import com.github.felipex.votecoop.domain.Pauta;
import com.github.felipex.votecoop.domain.SessaoVotacao;
import com.github.felipex.votecoop.dto.request.AbrirSessaoRequest;
import com.github.felipex.votecoop.dto.request.CadastrarPautaRequest;
import com.github.felipex.votecoop.dto.request.RegistrarVotoRequest;
import com.github.felipex.votecoop.dto.response.*;
import com.github.felipex.votecoop.service.PautaService;
import com.github.felipex.votecoop.service.SessaoVotacaoService;
import com.github.felipex.votecoop.service.VotoService;
import com.github.felipex.votecoop.util.CpfUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class PautaController {

    private final PautaService pautaService;
    private final SessaoVotacaoService sessaoService;
    private final VotoService votoService;

    @Value("${app.base-url}")
    private String baseUrl;

    // -------------------------------------------------------------------------
    // Tela inicial
    // -------------------------------------------------------------------------

    @GetMapping("/")
    public TelaSelecao telaInicial() {
        return new TelaSelecao("Votecoop", List.of(
                new ItemSelecao("Listar Pautas", baseUrl + "/pautas"),
                new ItemSelecao("Cadastrar Nova Pauta", baseUrl + "/pautas/formulario")
        ));
    }

    // -------------------------------------------------------------------------
    // Pautas
    // -------------------------------------------------------------------------

    /** Retorna a lista de pautas (pode ser chamado via GET ou POST pelo app). */
    @GetMapping("/pautas")
    public TelaSelecao listarPautas() {
        List<Pauta> pautas = pautaService.listarTodas();

        if (pautas.isEmpty()) {
            return new TelaSelecao("Pautas", List.of(
                    new ItemSelecao("Nenhuma pauta cadastrada ainda.", ""),
                    new ItemSelecao("Cadastrar Nova Pauta", baseUrl + "/pautas/formulario")
            ));
        }

        List<ItemSelecao> itens = pautas.stream()
                .map(p -> new ItemSelecao(p.getTitulo(), baseUrl + "/pautas/" + p.getId()))
                .toList();

        return new TelaSelecao("Pautas", itens);
    }

    /** Retorna o formulário de cadastro de uma nova pauta. */
    @GetMapping("/pautas/formulario")
    public TelaFormulario formularioCadastrar() {
        return new TelaFormulario(
                "Cadastrar Pauta",
                List.of(
                        new ItemInputTexto("titulo", "Título da pauta"),
                        new ItemInputTexto("descricao", "Descrição")
                ),
                new BotaoAcao("Cadastrar", baseUrl + "/pautas",
                        Map.of("titulo", "", "descricao", "")),
                new BotaoAcao("Cancelar", baseUrl + "/pautas")
        );
    }

    /** Cadastra uma nova pauta e retorna a tela de detalhes dela. */
    @PostMapping("/pautas")
    public Object cadastrarPauta(@Valid @RequestBody CadastrarPautaRequest request) {
        Pauta pauta = pautaService.cadastrar(request);
        return telaPauta(pauta.getId());
    }

    /** Retorna a tela de detalhes e ações de uma pauta específica. */
    @GetMapping("/pautas/{id}")
    public TelaSelecao telaPauta(@PathVariable Long id) {
        Pauta pauta = pautaService.buscarPorId(id);
        Optional<SessaoVotacao> sessaoAtiva = sessaoService.buscarSessaoAtivaOpcional(id);

        List<ItemSelecao> itens = new ArrayList<>();

        if (pauta.getDescricao() != null && !pauta.getDescricao().isBlank()) {
            itens.add(new ItemSelecao(pauta.getDescricao(), ""));
        }

        if (sessaoAtiva.isPresent()) {
            String dataFechamento = sessaoAtiva.get().getDataFechamento()
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
            itens.add(new ItemSelecao("Sessão ativa até: " + dataFechamento, ""));
            itens.add(new ItemSelecao("Votar", baseUrl + "/pautas/" + id + "/votos/formulario"));
        } else {
            itens.add(new ItemSelecao("Abrir Sessão de Votação",
                    baseUrl + "/pautas/" + id + "/sessao/formulario"));
        }

        itens.add(new ItemSelecao("Ver Resultado", baseUrl + "/pautas/" + id + "/resultado"));
        itens.add(new ItemSelecao("Voltar às Pautas", baseUrl + "/pautas"));

        return new TelaSelecao(pauta.getTitulo(), itens);
    }

    // -------------------------------------------------------------------------
    // Sessão de votação
    // -------------------------------------------------------------------------

    /** Retorna o formulário para abrir uma sessão de votação. */
    @GetMapping("/pautas/{id}/sessao/formulario")
    public TelaFormulario formularioAbrirSessao(@PathVariable Long id) {
        Pauta pauta = pautaService.buscarPorId(id);
        return new TelaFormulario(
                "Abrir Sessão — " + pauta.getTitulo(),
                List.of(
                        new ItemTexto("Configure a duração da sessão de votação."),
                        new ItemInputNumero("duracaoMinutos", "Duração (minutos)", 1)
                ),
                new BotaoAcao("Abrir Sessão", baseUrl + "/pautas/" + id + "/sessao",
                        Map.of("duracaoMinutos", 1)),
                new BotaoAcao("Cancelar", baseUrl + "/pautas/" + id)
        );
    }

    /** Abre uma sessão de votação para a pauta. */
    @PostMapping("/pautas/{id}/sessao")
    public TelaSelecao abrirSessao(
            @PathVariable Long id,
            @Valid @RequestBody(required = false) AbrirSessaoRequest request) {

        SessaoVotacao sessao = sessaoService.abrirSessao(id, request);
        Pauta pauta = sessao.getPauta();

        String dataFechamento = sessao.getDataFechamento()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));

        return new TelaSelecao("Sessão Aberta!", List.of(
                new ItemSelecao("Pauta: " + pauta.getTitulo(), ""),
                new ItemSelecao("Sessão encerra em: " + dataFechamento, ""),
                new ItemSelecao("Votar agora", baseUrl + "/pautas/" + id + "/votos/formulario"),
                new ItemSelecao("Voltar à Pauta", baseUrl + "/pautas/" + id)
        ));
    }

    // -------------------------------------------------------------------------
    // Votação
    // -------------------------------------------------------------------------

    /** Retorna o formulário de votação (exige sessão ativa). */
    @GetMapping("/pautas/{id}/votos/formulario")
    public TelaFormulario formularioVotar(@PathVariable Long id) {
        Pauta pauta = pautaService.buscarPorId(id);
        SessaoVotacao sessao = sessaoService.buscarSessaoAtiva(id);

        String dataFechamento = sessao.getDataFechamento()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));

        String cpf = CpfUtils.gerarAleatorio();

        return new TelaFormulario(
                "Votar — " + pauta.getTitulo(),
                List.of(
                        new ItemTexto("Sessão encerra em: " + dataFechamento),
                        new ItemInputTexto("associadoId", "CPF do Associado", cpf)
                ),
                new BotaoAcao("Votar SIM", baseUrl + "/pautas/" + id + "/votos",
                        Map.of("associadoId", cpf, "opcao", "SIM")),
                new BotaoAcao("Votar NÃO", baseUrl + "/pautas/" + id + "/votos",
                        Map.of("associadoId", cpf, "opcao", "NAO"))
        );
    }

    /** Registra o voto de um associado. */
    @PostMapping("/pautas/{id}/votos")
    public TelaSelecao registrarVoto(
            @PathVariable Long id,
            @Valid @RequestBody RegistrarVotoRequest request) {

        votoService.registrarVoto(id, request);

        return new TelaSelecao("Voto Registrado!", List.of(
                new ItemSelecao("Seu voto foi computado com sucesso.", ""),
                new ItemSelecao("Ver Resultado Parcial", baseUrl + "/pautas/" + id + "/resultado"),
                new ItemSelecao("Voltar às Pautas", baseUrl + "/pautas")
        ));
    }

    // -------------------------------------------------------------------------
    // Resultado
    // -------------------------------------------------------------------------

    /** Retorna o resultado da votação de uma pauta. */
    @GetMapping("/pautas/{id}/resultado")
    public TelaSelecao resultado(@PathVariable Long id) {
        Pauta pauta = pautaService.buscarPorId(id);

        boolean sessaoJaFoiAberta = !sessaoService.buscarSessoesPorPauta(id).isEmpty();
        if (!sessaoJaFoiAberta) {
            return new TelaSelecao("Resultado — " + pauta.getTitulo(), List.of(
                    new ItemSelecao("Nenhuma sessão de votação foi aberta para esta pauta.", ""),
                    new ItemSelecao("Voltar à Pauta", baseUrl + "/pautas/" + id)
            ));
        }

        VotoService.ResultadoVotacao resultado = votoService.contabilizarVotos(id, pauta.getTitulo());

        return new TelaSelecao("Resultado — " + pauta.getTitulo(), List.of(
                new ItemSelecao("Votos SIM: " + resultado.votosSim(), ""),
                new ItemSelecao("Votos NÃO: " + resultado.votosNao(), ""),
                new ItemSelecao("Total de votos: " + resultado.totalVotos(), ""),
                new ItemSelecao("Resultado: " + resultado.resultado(), ""),
                new ItemSelecao("Voltar à Pauta", baseUrl + "/pautas/" + id)
        ));
    }
}
