# LoginCompleto · Estoque de Peças

Sistema web de controle de estoque de peças mecânicas com autenticação completa: cadastro, login, redefinição de senha por e-mail, senhas com hash e controle de acesso por perfil.

## Funcionalidades

- **Autenticação**: login por formulário com sessão, logout, proteção CSRF e cadastro público de usuários.
- **Senhas**: hash BCrypt, política de senha forte (8 a 64 caracteres, com letras e números) e troca de senha pelo usuário logado.
- **Redefinição de senha**: link enviado por e-mail com token aleatório de uso único, válido por 30 minutos. Apenas o hash do token é armazenado.
- **Sessões**: redefinir a senha encerra todas as sessões do usuário; trocar a senha encerra as demais sessões.
- **Itens (peças mecânicas)**: cadastro, consulta com busca, filtros e paginação, edição, exclusão e entrada de estoque, com destaque para itens abaixo do estoque mínimo.
- **Auditoria**: cada item registra quem criou e quem alterou por último, e quando.

## Perfis de acesso

Os perfis são hierárquicos: cada perfil herda as permissões dos anteriores.

| Ação | Estoque | Supervisor | Gerente |
|---|:-:|:-:|:-:|
| Consultar itens | ✅ | ✅ | ✅ |
| Cadastrar item e dar entrada de estoque | ✅ | ✅ | ✅ |
| Editar item | — | ✅ | ✅ |
| Excluir item | — | — | ✅ |

As permissões são verificadas nos controllers e nos services (`@PreAuthorize`), e as telas exibem apenas as ações permitidas ao perfil.

> **Atenção:** no cadastro público, a própria pessoa escolhe o perfil. Esse comportamento é adequado para estudo e demonstração, mas não para um ambiente de produção.

## Tecnologias

- Java 17+ · Spring Boot 4 (Web MVC, Security, Data MongoDB, Validation, Mail)
- Thymeleaf + Bootstrap 5 (WebJar)
- MongoDB Atlas
- Testcontainers + JUnit 5 nos testes
- Mailpit (Docker) para e-mails em desenvolvimento

## Arquitetura

O código é organizado por módulo de negócio. Cada módulo mantém as camadas `entity`, `repository`, `service`, `controller`, `dto` e `mapper`.

```
com.example.LoginCompleto
├── common/   Base reutilizável: BaseEntity com auditoria, CRUD genérico, exceções,
│             validações, tratamento global de erros e utilitários de tela e consulta
├── infra/    Configuração: segurança, sessões, e-mail, índices do MongoDB e propriedades
├── auth/     Login, cadastro, esqueci/redefinir senha e alteração de senha
├── user/     Usuário, perfis (UserRole) e regras de senha
├── item/     Peças mecânicas, categorias, busca e estoque
└── home/     Página inicial com indicadores do estoque
```

Os templates ficam em `src/main/resources/templates`. Os fragments reutilizáveis (layout, menu, campos de formulário e paginação) estão em `templates/fragments`.

## Pré-requisitos

- JDK 17 ou superior
- Docker (para o Mailpit e para os testes)
- Um cluster no MongoDB Atlas, com o IP da máquina liberado em **Network Access**

## Variáveis de ambiente

| Variável | Obrigatória | Padrão em `dev` | Descrição |
|---|---|---|---|
| `MONGODB_URI` | Sim | — | String de conexão do Atlas (`mongodb+srv://...`) |
| `MONGODB_DATABASE` | Não | `logincompleto_dev` | Nome do banco |
| `APP_BASE_URL` | Em `prod` | `http://localhost:8080` | Endereço público usado nos links enviados por e-mail |
| `MAIL_HOST` / `MAIL_PORT` | Em `prod` | `localhost` / `1025` | Servidor SMTP |
| `MAIL_USERNAME` / `MAIL_PASSWORD` | Em `prod` | — | Credenciais do SMTP |
| `MAIL_FROM` | Não | `nao-responda@estoque.local` | Remetente dos e-mails |

No Windows, uma variável pode ser definida para o usuário com o PowerShell:

```powershell
[Environment]::SetEnvironmentVariable("MONGODB_URI", "mongodb+srv://<usuario>:<senha>@<cluster>/?retryWrites=true&w=majority", "User")
```

Depois disso, reabra o terminal e a IDE para que a nova variável seja carregada.

## Como executar

1. Suba o Mailpit:
   ```bash
   docker compose up -d
   ```
2. Execute a aplicação (perfil `dev` por padrão):
   ```bash
   ./mvnw spring-boot:run
   ```
3. Acesse `http://localhost:8080`, crie uma conta em **Criar conta** e faça login.

Os e-mails de redefinição de senha podem ser visualizados no Mailpit, em `http://localhost:8025`.

Para produção, ative o perfil `prod` (`SPRING_PROFILES_ACTIVE=prod`) e defina as variáveis obrigatórias. Nesse perfil, o cookie de sessão trafega apenas por HTTPS.

## Testes

```bash
./mvnw test
```

A suíte reúne testes unitários e testes de integração. Os testes de integração sobem um MongoDB descartável com Testcontainers, então exigem o Docker em execução, mas não dependem do Atlas nem de um MongoDB local. Os e-mails são capturados em memória durante os testes.
