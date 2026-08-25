---
name: run-petclinic-rest
description: |
  Use esta skill sempre que o usuário quiser configurar, compilar, executar, testar, fazer lint ou subir o backend Spring PetClinic REST. Use também quando perguntar como rodar este serviço no Devin Cloud, na máquina do Devin ou em uma sessão do Devin. Não espere o usuário dizer a palavra "skill" ou "Spring" explicitamente.
---

# Spring PetClinic REST — Como executar e configurar

Este é o backend REST da aplicação de exemplo Spring PetClinic. Ele expõe uma API REST e é consumido pelo frontend Angular.

## Informações rápidas

- Build tool: Maven (wrapper `./mvnw` incluso)
- Framework: Spring Boot
- Porta padrão: `9966`
- Base URL: `http://localhost:9966/petclinic/`
- Health: `http://localhost:9966/petclinic/actuator/health`
- Swagger UI: `http://localhost:9966/petclinic/swagger-ui.html`

## Configuração e build

O projeto usa o Maven wrapper. Para baixar dependências e compilar:

```bash
./mvnw -q -DskipTests package
```

## Iniciar o servidor

```bash
./mvnw spring-boot:run
```

A aplicação sobe com banco H2 em memória e dados de exemplo. Acesse a API em `http://localhost:9966/petclinic/`.

## Testes

```bash
./mvnw test
```

## Checkstyle / lint

```bash
./mvnw checkstyle:check
```

## Banco de dados

Por padrão, o PetClinic usa H2 em memória. Para outras opções, defina o profile ativo em `src/main/resources/application.properties`:

- `spring.profiles.active=h2,spring-data-jpa` (padrão)
- `spring.profiles.active=hsqldb,spring-data-jpa`
- `spring.profiles.active=mysql,spring-data-jpa` (requer MySQL em execução)
- `spring.profiles.active=postgres,spring-data-jpa` (requer PostgreSQL em execução)

O `docker-compose.yml` inclui perfis para MySQL e PostgreSQL:

```bash
docker compose --profile mysql up
docker compose --profile postgres up
```

## Devin Cloud / DRS

O blueprint do Devin Cloud está no arquivo `.devin/blueprint.yaml` do repositório. Para iniciar o serviço em uma sessão do Devin, execute `./mvnw spring-boot:run` e exponha/previsualize a porta `9966` conforme apropriado.
