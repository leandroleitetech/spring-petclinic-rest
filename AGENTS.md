# AGENTS.md — Spring PetClinic REST

Guia para agentes de IA (Devin e similares) que trabalham neste repositório.

## Descrição do projeto

Backend REST do Spring PetClinic (Spring Boot 4.1.0). **Não possui UI** — expõe apenas uma API REST,
consumida pelo frontend [spring-petclinic-angular](https://github.com/leandroleitetech/spring-petclinic-angular).

- Porta padrão: `9966`
- Base URL: `http://localhost:9966/petclinic/`
- Health check: `http://localhost:9966/petclinic/actuator/health`
- Swagger UI: `http://localhost:9966/petclinic/swagger-ui.html`
- OpenAPI (OAS 3.1): `http://localhost:9966/petclinic/v3/api-docs`

## Build tool

Maven, através do wrapper incluso no repositório: `./mvnw` (não é necessário instalar o Maven).

## Comandos

Comandos oficiais, extraídos de `.devin/blueprint.yaml` e `.devin/skills/run-petclinic-rest/SKILL.md`:

| Ação | Comando |
|--|--|
| Setup / Build | `./mvnw -q -DskipTests package` |
| Testes | `./mvnw test` |
| Lint (Checkstyle) | `./mvnw checkstyle:check` |
| Iniciar a aplicação | `./mvnw spring-boot:run` |

## Banco de dados

Por padrão a aplicação usa **H2 em memória**, populado automaticamente com dados de exemplo na inicialização.
Perfis alternativos são definidos pela propriedade `spring.profiles.active` em
`src/main/resources/application.properties`:

- `spring.profiles.active=h2,spring-data-jpa` (padrão)
- `spring.profiles.active=hsqldb,spring-data-jpa`
- `spring.profiles.active=mysql,spring-data-jpa` (requer MySQL em execução)
- `spring.profiles.active=postgres,spring-data-jpa` (requer PostgreSQL em execução)

O `docker-compose.yml` inclui perfis para MySQL e PostgreSQL:

```bash
docker compose --profile mysql up
docker compose --profile postgres up
```

Console H2 (com a aplicação rodando): `http://localhost:9966/petclinic/h2-console`
(JDBC URL `jdbc:h2:mem:petclinic`, usuário `sa`, senha em branco).

## Estrutura de código

Conforme a seção "Looking for something in particular?" do `readme.md`:

| Camada | Local |
|--|--|
| Controllers REST | `src/main/java/org/springframework/samples/petclinic/rest` |
| Service | `src/main/java/org/springframework/samples/petclinic/service/ClinicServiceImpl.java` |
| Repositórios JDBC | `src/main/java/org/springframework/samples/petclinic/repository/jdbc` |
| Repositórios JPA | `src/main/java/org/springframework/samples/petclinic/repository/jpa` |
| Repositórios Spring Data JPA | `src/main/java/org/springframework/samples/petclinic/repository/springdatajpa` |
| Testes | `src/test/java/org/springframework/samples/petclinic/service/clinicService/AbstractClinicServiceTests.java` |

Parte do código (DTOs e interfaces de API a partir de `src/main/resources/openapi.yml`, além dos mappers
MapStruct) é **gerada durante o build** em `target/generated-sources` — não edite esses arquivos manualmente.

## CONVENÇÕES E REGRAS

Regras de trabalho obrigatórias para agentes neste repositório.

### Skills

- As skills ficam em `.devin/skills/<nome>/SKILL.md`.
- Cada skill possui frontmatter YAML com os campos `name` e `description`.
- Exemplo existente neste repositório: `.devin/skills/run-petclinic-rest/SKILL.md`.
- **Antes de executar qualquer tarefa de setup, build, run, test ou lint, consulte a skill relevante**
  e siga os comandos documentados nela.

### Rules

- Por convenção, regras específicas do repositório devem ficar em `.devin/rules/`.
- Esse diretório ainda não existe neste repositório; caso venha a existir, todas as rules ali
  presentes devem ser lidas e seguidas.

### Blueprint do Devin Cloud

- O blueprint fica em `.devin/blueprint.yaml` e contém os passos de `initialize`, `maintenance` e o
  bloco `knowledge` com os comandos de setup, build, test, lint e startup.
- O agente deve seguir o blueprint como fonte oficial de configuração do ambiente.

### Testar antes de concluir

- Tudo que o agente fizer deve ser testado antes de ser considerado concluído.
- Rode os testes (`./mvnw test`) e o lint (`./mvnw checkstyle:check`) e garanta que **nada quebrou**
  (nenhuma regressão) antes de finalizar a tarefa ou abrir um PR.

### Não fazer push sem solicitação

- Não execute `git push` nem abra Pull Request sem solicitação explícita do usuário.

### Não alterar o que não foi pedido

- Limite as mudanças estritamente ao escopo solicitado.
- Não refatore, não reformate e não altere arquivos fora do pedido.

### Convenções gerais

- Mensagens de commit descritivas e objetivas.
- Respeite o estilo de código existente; o projeto usa **Checkstyle** (`./mvnw checkstyle:check`).
- Documente no PR o resultado da validação de ambiente (build, testes e lint).
