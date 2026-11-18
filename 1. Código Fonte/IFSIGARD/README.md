# IFSIGARD — Protótipo Android

Sistema de Gestão de Ausências e Reposições Docentes (Projeto PRIS · SLTPRIS 2026-2).
Kotlin + Jetpack Compose + Room.

## Como abrir no Android Studio

1. Descompacte o `.zip`.
2. Android Studio → **File › Open** → selecione a pasta `IFSIGARD`.
3. Aguarde o *Gradle Sync* (na primeira vez ele baixa as dependências).
4. Rode em um emulador ou celular (Android 8.0 / API 26 ou superior).

Requisitos: Android Studio Ladybug (2024.2) ou mais recente, JDK 17 (já vem com o Android Studio).

## Usuários de teste (senha `123`)

| Login | Perfil | O que pode fazer |
| --- | --- | --- |
| `cae` | CAE | Faltas, usuários, avisos |
| `coord` | Coordenador | Faltas, reposições, avisos |
| `prof` | Professor | Faltas, avisos |
| `aluno` | Aluno (curso ADS) | Só visualizar avisos do seu curso |

## Funcionalidades (casos de uso)

- **Manter Falta** — CAE, Professor e Coordenador registram, editam e excluem faltas
  (faltante, data, disciplina, curso, motivo). Cada registro gera um aviso.
- **Manter Reposição** — só o Coordenador. Agenda data, horário e sala; a falta passa a
  "Reposta" e um aviso é gerado. Excluir a reposição devolve a falta para "pendente".
- **Manter Usuários** — só a CAE cadastra alunos, professores e coordenadores.
- **Visualizar Avisos** — dashboard; o aluno vê apenas os avisos do seu curso.

## Requisitos técnicos atendidos

- **Room**: entidades `Usuario`, `Falta`, `Reposicao`, `Aviso` (`data/`).
- **Validação de formulários**: campos obrigatórios, datas `dd/mm/aaaa`, horário `hh:mm`,
  reposição não pode ser antes da falta, login único.
- **Tratamento de exceções**: `RegraNegocioException` no `Repositorio`, tratada no
  `AppViewModel` e exibida em Toast; `DateTimeParseException` em `util/Formatos.kt`.
- **Orientação a objetos, coleções e funções**: data classes, enums, `filter`,
  `associateBy`, `count`, funções de extensão.

## Estrutura

```
app/src/main/java/br/edu/ifsp/ifsigard/
├── MainActivity.kt          navegação entre as telas
├── data/                    Room: entidades, DAOs, banco e regras (Repositorio)
├── ui/AppViewModel.kt       estado, sessão e tratamento de erros
├── ui/telas/                Login, Início, Faltas, Reposições, Usuários, Avisos
├── ui/componentes/          campos, listas suspensas, diálogos
├── ui/tema/                 cores
└── util/Formatos.kt         leitura e validação de datas/horários
```

> Protótipo acadêmico: as senhas ficam em texto puro no banco local.
