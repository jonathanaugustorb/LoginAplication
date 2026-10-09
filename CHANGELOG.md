# Changelog

Todas as mudanças relevantes do projeto são registradas neste arquivo.

## [0.1.0] - 2026-10-08

Primeira versão funcional do sistema: autenticação completa, redefinição de senha, controle de acesso por perfil e CRUD de peças mecânicas, com frontend em Thymeleaf e banco de dados MongoDB Atlas.

### Adicionado

**Autenticação e usuários**
- Login por formulário com sessão, logout e proteção CSRF.
- Cadastro público de usuários com escolha do perfil de acesso.
- Hash de senha com BCrypt e política de senha forte (8 a 64 caracteres, com letras e números).
- Redefinição de senha por e-mail, com token aleatório de uso único válido por 30 minutos; apenas o hash do token é armazenado.
- Alteração de senha pelo usuário logado.
- Encerramento das sessões abertas após a redefinição ou a alteração de senha.
- Botão para mostrar e ocultar a senha em todos os campos de senha.

**Perfis de acesso**
- Perfis hierárquicos `ESTOQUE`, `SUPERVISOR` e `GERENTE`, verificados nos controllers, nos serviços (`@PreAuthorize`) e nas telas.

| Ação | Estoque | Supervisor | Gerente |
|---|:-:|:-:|:-:|
| Consultar itens | ✅ | ✅ | ✅ |
| Cadastrar item e dar entrada de estoque | ✅ | ✅ | ✅ |
| Editar item | — | ✅ | ✅ |
| Excluir item | — | — | ✅ |

**Itens (peças mecânicas)**
- Cadastro, consulta, edição e exclusão de itens com código, nome, descrição, categoria, marca, aplicação, quantidade, estoque mínimo, preço e localização.
- Entrada de estoque com incremento atômico no banco de dados.
- Busca por texto, filtros por categoria e por estoque baixo, ordenação e paginação.
- Detecção de edição simultânea do mesmo item.
- Página inicial com indicadores de itens cadastrados e de itens com estoque baixo.

**Frontend**
- Páginas em Thymeleaf com Bootstrap 5, layout responsivo e fragmentos reutilizáveis (layout, menu, mensagens, campos de formulário e paginação).
- Página de erro padronizada para acesso negado, página não encontrada e erros inesperados.

**Base genérica e infraestrutura**
- Módulo `common` com entidade base auditada, CRUD genérico, exceções, validações e utilitários de tela e de consulta.
- Auditoria automática de criação e alteração (data e usuário).
- Criação automática dos índices do MongoDB a partir das anotações das entidades, incluindo índice TTL para os tokens de redefinição.
- Envio de e-mails assíncrono com templates Thymeleaf; Mailpit via `docker-compose.yml` para desenvolvimento.
- Configuração por variáveis de ambiente e perfis `dev`, `prod` e `test`.

**Testes e documentação**
- Suíte com 65 testes unitários e de integração; os testes de integração usam MongoDB descartável com Testcontainers.
- `README.md` com instruções de configuração e execução.
- Documentação técnica em `docs/`.

### Alterado
- Código reorganizado por módulo de negócio (`auth`, `common`, `home`, `infra`, `item`, `user`) dentro do pacote `com.example.LoginCompleto`.
- Perfis de acesso substituídos por `ESTOQUE`, `SUPERVISOR` e `GERENTE`.
- Preço dos itens passa a usar `BigDecimal`, gravado como `Decimal128` no MongoDB.
- Dependências atualizadas no `pom.xml`: entraram validação, e-mail, Thymeleaf Security, WebJars e Testcontainers.

### Removido
- Autenticação por JWT (`SecurityFilter` e `TokenService`) e a dependência `java-jwt`, substituídas pela autenticação baseada em sessão.
- Dependência duplicada do Spring Data MongoDB no `pom.xml`.

### Segurança
- Nenhuma credencial é armazenada no código; a conexão com o MongoDB Atlas é lida da variável de ambiente `MONGODB_URI`.
- Removido o segredo fixo do JWT que existia em `application.properties`.
- Cabeçalho `Referrer-Policy: same-origin` e cookie de sessão restrito a HTTPS no perfil `prod`.
