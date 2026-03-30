# Votecoop

API para gerenciamento de pautas e votações de cooperativas.
Desenvolvida com Spring Boot 4, retorna respostas no formato de telas navegáveis para consumo por aplicativos mobile.

---

## Tecnologias

| Dependência | Descrição |
|---|---|
| Spring Boot 4.0.5 | Framework principal |
| Spring Data JPA | Persistência de dados |
| H2 Database | Banco de dados em arquivo |
| Lombok | Redução de boilerplate |
| Caelum Stella | Validação e geração de CPF |
| SpringDoc OpenAPI | Documentação Swagger |

---

## Pré-requisitos

- Java 17+
- Maven 3.9+
- Docker
- API usercheck `https://github.com/fxxavier/usercheck`

---

## Como executar

```bash
mvn spring-boot:run
```

A aplicação sobe em `http://localhost:8080`.

**URLs úteis:**
- Home: `http://localhost:8080/`
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- Console H2: `http://localhost:8080/h2-console`

---

## Como executar com Docker

```bash
docker build -t votecoop .
docker run -p 8080:8080 --name votecoop votecoop
```

Após container criado
```bash
docker start votecoop
```

---

## Como executar os testes

```bash
mvn test
```

---

## Como gerar o JAR

```bash
mvn clean package -DskipTests
java -jar target/votecoop-0.1.0-SNAPSHOT.jar
```

---

## Configuração

As propriedades configuráveis estão em `src/main/resources/application.properties`:

| Propriedade | Padrão | Descrição |
|---|---|---|
| `app.base-url` | `http://localhost:8080` | URL base da aplicação |
| `app.sessao.duracao-padrao-minutos` | `1` | Duração padrão de sessões de votação |
| `app.usercheck.base-url` | `http://localhost:8090` | URL da API de verificação de associados |

Todas as propriedades podem ser sobrescritas na execução:

```bash
java -jar target/votecoop-0.1.0-SNAPSHOT.jar \
  --app.sessao.duracao-padrao-minutos=5 \
  --app.usercheck.base-url=http://meu-servico:8090
```

---

## Modelo de domínio

```
Pauta (1) ──── (N) SessaoVotacao (1) ──── (N) Voto
```

- **Pauta** — proposta a ser votada
- **SessaoVotacao** — janela de tempo em que os votos são aceitos (uma por pauta)
- **Voto** — registro do voto de um associado (CPF único por sessão)

---

## Endpoints

### Tela inicial

| Método | URL | Descrição |
|---|---|---|
| GET | `/` | Retorna a tela inicial |

### Pautas

| Método | URL | Descrição |
|---|---|---|
| GET | `/pautas` | Lista todas as pautas |
| GET | `/pautas/formulario` | Retorna o formulário de cadastro |
| POST | `/pautas` | Cadastra uma nova pauta |
| GET | `/pautas/{id}` | Retorna os detalhes de uma pauta |

**POST /pautas — corpo da requisição:**
```json
{
  "titulo": "Aprovação do orçamento",
  "descricao": "Votação sobre o orçamento anual"
}
```

### Sessão de votação

| Método | URL | Descrição |
|---|---|---|
| GET | `/pautas/{id}/sessao/formulario` | Retorna o formulário para abrir sessão |
| POST | `/pautas/{id}/sessao` | Abre a sessão de votação |

**POST /pautas/{id}/sessao — corpo da requisição:**
```json
{
  "duracaoMinutos": 5
}
```
> `duracaoMinutos` é opcional. Se omitido, usa o valor padrão configurado em `app.sessao.duracao-padrao-minutos`.

### Votos

| Método | URL | Descrição |
|---|---|---|
| GET | `/pautas/{id}/votos/formulario` | Retorna o formulário de votação com CPF gerado |
| POST | `/pautas/{id}/votos` | Registra o voto de um associado |

**POST /pautas/{id}/votos — corpo da requisição:**
```json
{
  "associadoId": "12345678901",
  "opcao": "SIM"
}
```

> `opcao` aceita apenas `SIM` ou `NAO`.
> `associadoId` deve ser um CPF válido.
> O formulário (`GET .../votos/formulario`) já pré-preenche o campo com um CPF gerado automaticamente.

### Resultado

| Método | URL | Descrição |
|---|---|---|
| GET | `/pautas/{id}/resultado` | Retorna o resultado da votação |

---

## Erros

Todas as respostas de erro seguem o mesmo formato de tela:

```json
{
  "tipo": "SELECAO",
  "titulo": "Título do erro",
  "itens": [
    { "texto": "Mensagem descritiva.", "url": "", "body": {} }
  ]
}
```

| Código | HTTP | Título | Situação |
|---|---|---|---|
| `RECURSO_NAO_ENCONTRADO` | 404 | Não Encontrado | ID inexistente |
| `SESSAO_NAO_ATIVA` | 422 | Sessão Indisponível | Nenhuma sessão ativa para a pauta |
| `SESSAO_ENCERRADA` | 422 | Sessão Encerrada | Sessão expirada |
| `SESSAO_JA_ABERTA` | 409 | Sessão Já Aberta | Pauta já possui sessão |
| `VOTO_DUPLICADO` | 409 | Voto Já Registrado | Associado já votou |
| `CPF_INVALIDO` | 400 | CPF Inválido | CPF com formato inválido |
| `ASSOCIADO_NAO_ENCONTRADO` | 404 | Associado Não Encontrado | CPF não encontrado no serviço externo |
| `ASSOCIADO_INAPTO` | 422 | Associado Inapto | Associado não habilitado para votar |
| `SERVICO_INDISPONIVEL` | 503 | Serviço Indisponível | API de verificação fora do ar |

---

## Integração externa: verificação de associados

Antes de registrar um voto, a API consulta o serviço externo configurado em `app.usercheck.base-url`:

```
GET http://localhost:8090/users/{cpf}
```

**Resposta esperada:**
```json
{ "status": "ABLE_TO_VOTE" }
```

| Status retornado | Comportamento |
|---|---|
| `ABLE_TO_VOTE` | Voto liberado |
| `UNABLE_TO_VOTE` | Retorna `ASSOCIADO_INAPTO` (422) |
| HTTP 4xx | Retorna `ASSOCIADO_NAO_ENCONTRADO` (404) |
| Falha de conexão | Retorna `SERVICO_INDISPONIVEL` (503) |

---

## Estrutura do projeto

```
src/main/java/com/github/felipex/votecoop/
├── client/         # Cliente HTTP para serviço externo
├── controller/     # Endpoints REST e tratamento de exceções
├── domain/         # Entidades JPA
├── dto/
│   ├── request/    # Objetos de entrada
│   └── response/   # Objetos de saída (telas)
├── exception/      # Exceções tipadas com código e mensagem
├── repository/     # Repositórios Spring Data JPA
├── service/        # Regras de negócio
└── util/           # Utilitários (CPF)
```
