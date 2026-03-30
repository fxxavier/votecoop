package com.github.felipex.votecoop.exception;

public class AppException extends RuntimeException {

    public enum Tipo {
        RECURSO_NAO_ENCONTRADO(404, "Não Encontrado",      "Recurso não encontrado com id: %s."),
        SESSAO_NAO_ATIVA      (422, "Sessão Indisponível",  "Não há sessão de votação ativa para a pauta %s."),
        SESSAO_ENCERRADA      (422, "Sessão Encerrada",    "A sessão de votação para a pauta %s foi encerrada."),
        SESSAO_JA_ABERTA      (409, "Sessão Já Aberta",    "A pauta %s já possui uma sessão de votação e não pode ter outra."),
        VOTO_DUPLICADO        (409, "Voto Já Registrado",  "O associado '%s' já votou nesta pauta."),
        CPF_INVALIDO          (400, "CPF Inválido",        "O CPF '%s' informado é inválido."),
        ASSOCIADO_NAO_ENCONTRADO(404, "Associado Não Encontrado", "Nenhum associado encontrado com o CPF '%s'."),
        ASSOCIADO_INAPTO        (422, "Associado Inapto",        "O associado com CPF '%s' não está habilitado para votar."),
        SERVICO_INDISPONIVEL    (503, "Serviço Indisponível",    "O serviço de verificação de associados está indisponível. Tente novamente mais tarde.");

        public final int status;
        public final String titulo;
        private final String template;

        Tipo(int status, String titulo, String template) {
            this.status   = status;
            this.titulo   = titulo;
            this.template = template;
        }

        public String formatar(Object... args) {
            return args.length == 0 ? template : String.format(template, args);
        }
    }

    public final Tipo tipo;

    public AppException(Tipo tipo, Object... args) {
        super(tipo.formatar(args));
        this.tipo = tipo;
    }
}
