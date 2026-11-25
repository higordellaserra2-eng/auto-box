# AutoBox

Aplicativo Android para clientes de centros automotivos brasileiros. Permite pagar serviços por Pix ou cartão, consultar o histórico, acumular benefícios, cadastrar veículos e receber promoções e lembretes.

![Versão](https://img.shields.io/badge/vers%C3%A3o-0.1.0-blue)
![Android](https://img.shields.io/badge/Android-8.0%2B-3DDC84?logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-Jetpack%20Compose-7F52FF?logo=kotlin&logoColor=white)

> **Status:** em desenvolvimento (v0.1.0 - Fundação). Esta versão contém a base técnica e visual. As funcionalidades de negócio são entregues nas próximas etapas.

## Funcionalidades

| Funcionalidade | Status |
|---|---|
| Onboarding e Home (dados mockados) | Pronto |
| Cadastro de veículos | Planejado (v0.2.0) |
| Histórico de serviços | Planejado (v0.3.0) |
| Pagamento por Pix ou cartão | Planejado (v0.4.0) |
| Acúmulo de benefícios | Planejado (v0.5.0) |
| Promoções e lembretes | Planejado (v0.6.0) |

## Tecnologias

- **Linguagem:** Kotlin, Coroutines e Flow
- **UI:** Jetpack Compose e Material Design 3 (tema claro, escuro e dinâmico)
- **Arquitetura:** multimódulo, camadas UI / Domain / Data, MVVM com `StateFlow`
- **Injeção de dependência:** Hilt (com KSP)
- **Navegação:** Navigation Compose tipada (`@Serializable`) com deep links
- **Rede:** Retrofit, OkHttp e Moshi
- **Persistência:** Jetpack DataStore (Room previsto para a v0.2.0)
- **Testes:** JUnit, MockK, Turbine, Compose UI Test e Robolectric

## Requisitos

- Android Studio (versão estável mais recente)
- JDK 17 ou superior
- Android SDK com API 36 instalada
- Dispositivo ou emulador com Android 8.0 (API 26) ou superior

## Como executar

```bash
# 1. Clone o repositório
git clone <URL_DO_REPOSITORIO>
cd autobox

# 2. Compile e instale a versão de debug
./gradlew :app:installDebug
```

Ou abra o projeto no Android Studio, aguarde a sincronização do Gradle e execute o módulo `app`.

## Testes

```bash
# Todos os testes unitários
./gradlew test

# Um módulo específico
./gradlew :feature:home:test
```

## Estrutura do projeto

```text
autobox/
├── app/                    # Aplicação, MainActivity e navegação principal
├── core/
│   ├── common/             # AppResult, AppError, UiText, formatadores pt-BR
│   ├── designsystem/       # Tema, tokens e componentes reutilizáveis
│   ├── network/            # Retrofit, OkHttp, interceptors e mapeamento de erros
│   └── data/               # Modelos de domínio, repositórios e DataStore
└── feature/
    ├── onboarding/         # Introdução de 3 telas
    └── home/               # Tela inicial
```

## Design system

O módulo `:core:designsystem` centraliza o visual do app:

- **Tokens:** espaçamento em grade de 4/8dp, raios, elevações e alvo mínimo de toque de 48dp
- **Cores:** azul cobalto (primária), âmbar (secundária) e esmeralda (terciária), com cores semânticas de sucesso, alerta e informação
- **Tipografia:** Plus Jakarta Sans (textos) e JetBrains Mono (placas e valores)
- **Componentes:** botões, campos de texto, cards, chips, skeleton, estados de carregamento, vazio, erro e offline, barra superior, scaffold adaptativo e snackbar

Todos os componentes possuem `@Preview` nos modos Claro, Escuro e Fonte 200%.

## Acessibilidade

- Compatível com TalkBack (semântica, títulos e `liveRegion`)
- Contraste mínimo conforme WCAG 2.2 AA
- Suporte a fonte ampliada até 200%
- Alvos de toque de no mínimo 48dp
- Respeito à preferência de redução de animações
- Nenhuma informação transmitida apenas por cor

## Segurança e privacidade

- O log de rede nunca registra o corpo das requisições e oculta cabeçalhos sensíveis (`Authorization`, `Cookie`)
- Mensagens de erro em linguagem humana, sem detalhes técnicos
- R8 ativado no build de release
- Projeto preparado para a LGPD: coleta mínima de dados e consentimento explícito nas próximas etapas

## Deep links

| Link | Destino |
|---|---|
| `autobox://app/...` | Rotas internas do app (ampliadas a cada etapa) |

## Roadmap

- [x] **v0.1.0** - Fundação
- [ ] **v0.2.0** - Veículos
- [ ] **v0.3.0** - Histórico de serviços
- [ ] **v0.4.0** - Pagamento (Pix e cartão)
- [ ] **v0.5.0** - Benefícios
- [ ] **v0.6.0** - Notificações e lembretes
- [ ] **v1.0.0** - Primeira versão pública

## Contribuindo

1. Crie uma branch a partir de `main`: `git checkout -b feature/nome-da-feature`
2. Faça commits no padrão [Conventional Commits](https://www.conventionalcommits.org/pt-br/) (`feat:`, `fix:`, `docs:`, `test:`, `refactor:`)
3. Garanta que o projeto compila e que todos os testes passam
4. Abra um Pull Request descrevendo a mudança

## Licença

[Defina a licença do projeto]
