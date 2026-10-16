# aws-developer-labs

Base para uma trilha prática de AWS com Java 21, Spring Boot 3.5 e Maven Wrapper.
Inclui Web, Validation, Actuator e somente o módulo de serviço STS do AWS SDK
for Java v2, com versões gerenciadas pelo BOM oficial.

## Estrutura e execução

- `AwsDeveloperLabsApplication`: inicialização do Spring Boot.
- `aws/AwsIdentityService`: ponto de implementação manual do próximo lab.
- `controller/AwsIdentityController`: expõe `GET /api/aws/identity` como texto.
- `src/main/resources/application.yml`: nome da aplicação e exposição de health/info.
- `src/test/java`: teste básico de inicialização do contexto Spring.

Requer JDK 21. O Wrapper baixa o Maven na primeira execução:

```bash
./mvnw test
./mvnw package
```

O método `getCurrentIdentityArn()` está intencionalmente pendente, com TODO e
`UnsupportedOperationException`. Até sua implementação, o endpoint de identidade
retorna HTTP 500. Nenhum cliente STS é criado e nenhuma chamada AWS é feita.
O contexto e os testes podem iniciar sem credenciais AWS.

## Contexto AWS local

Contexto informado para os labs, mantido fora do repositório:

1. AWS CLI v2 instalada.
2. IAM user local: `aws-labs-dev`.
3. Autenticação feita com `aws login`.
4. Existe uma role `s3-reader-role`.
5. O profile local `s3-reader-role` assume essa role usando `aws-labs-dev`
   como source profile.
6. Região padrão dos labs: `us-east-1`.

Executar a aplicação posteriormente com:

```bash
AWS_PROFILE=s3-reader-role AWS_REGION=us-east-1 ./mvnw spring-boot:run
```

O próximo lab implementará a consulta ao STS usando a Default Credentials
Provider Chain do SDK e o profile local. As configurações em `~/.aws` permanecem
locais; não copie esses arquivos para o projeto.

## Cost and Security Guardrails

- Nunca versionar credenciais AWS, tokens ou arquivos de `~/.aws`.
- Usar IAM roles e credenciais temporárias sempre que possível.
- Aplicar least privilege às permissões de cada lab.
- Nenhum recurso AWS faturável deve ser criado nesta etapa.
- Futuros labs devem considerar limites de custo, throttling, concurrency,
  exposição pública e teardown de recursos.

Esta base não inclui banco de dados, Docker, Spring Security ou infraestrutura
como código. Os labs futuros serão implementados incrementalmente.
