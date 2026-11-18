package br.edu.ifsp.ifsigard.data

import androidx.room.withTransaction
import br.edu.ifsp.ifsigard.util.Formatos

/** Erro de regra de negócio, exibido ao usuário como mensagem. */
class RegraNegocioException(mensagem: String) : Exception(mensagem)

/**
 * Regras do sistema. Registrar ou repor uma falta também gera um aviso
 * no dashboard, como descrito nos casos de uso.
 */
class Repositorio(private val db: AppDatabase) {

    val usuarios = db.usuarioDao().listar()
    val docentes = db.usuarioDao().listarDocentes()
    val faltas = db.faltaDao().listar()
    val reposicoes = db.reposicaoDao().listar()
    val avisos = db.avisoDao().listar()

    private fun exigir(condicao: Boolean, mensagem: String) {
        if (!condicao) throw RegraNegocioException(mensagem)
    }

    suspend fun autenticar(login: String, senha: String): Usuario =
        db.usuarioDao().autenticar(login.trim(), senha)
            ?: throw RegraNegocioException("Usuário ou senha inválidos.")

    // ---------- Faltas (CAE, Professor e Coordenador) ----------

    suspend fun buscarFalta(id: Long) = db.faltaDao().buscar(id)

    suspend fun salvarFalta(falta: Falta, autor: Usuario) {
        exigir(autor.perfil != Perfil.ALUNO, "Alunos não podem registrar faltas.")
        db.withTransaction {
            if (falta.id == 0L) {
                db.faltaDao().inserir(falta.copy(registradoPor = autor.nome))
                db.avisoDao().inserir(
                    Aviso(
                        tipo = TipoAviso.FALTA,
                        titulo = "Aula sem professor: ${falta.disciplina}",
                        mensagem = "${falta.faltanteNome} estará ausente em " +
                            "${Formatos.isoParaBr(falta.data)}. Motivo: ${falta.descricao}",
                        curso = falta.curso
                    )
                )
            } else {
                db.faltaDao().atualizar(falta)
                db.avisoDao().inserir(
                    Aviso(
                        tipo = TipoAviso.FALTA,
                        titulo = "Falta atualizada: ${falta.disciplina}",
                        mensagem = "A ausência de ${falta.faltanteNome} agora está registrada para " +
                            "${Formatos.isoParaBr(falta.data)}. Motivo: ${falta.descricao}",
                        curso = falta.curso
                    )
                )
            }
        }
    }

    suspend fun excluirFalta(falta: Falta, autor: Usuario) {
        exigir(autor.perfil != Perfil.ALUNO, "Alunos não podem excluir faltas.")
        db.faltaDao().excluir(falta) // a reposição vinculada é excluída em cascata
    }

    // ---------- Reposições (somente Coordenador) ----------

    suspend fun buscarReposicao(id: Long) = db.reposicaoDao().buscar(id)

    suspend fun salvarReposicao(reposicao: Reposicao, autor: Usuario) {
        exigir(
            autor.perfil == Perfil.COORDENADOR,
            "Apenas coordenadores de curso podem gerir reposições."
        )
        val falta = db.faltaDao().buscar(reposicao.faltaId)
            ?: throw RegraNegocioException("A falta selecionada não existe mais.")
        exigir(
            reposicao.data >= falta.data,
            "A reposição não pode ser antes da falta (${Formatos.isoParaBr(falta.data)})."
        )
        val existente = db.reposicaoDao().buscarPorFalta(falta.id)
        exigir(
            existente == null || existente.id == reposicao.id,
            "Essa falta já possui uma reposição agendada."
        )

        db.withTransaction {
            if (reposicao.id == 0L) {
                db.reposicaoDao().inserir(reposicao.copy(coordenadorNome = autor.nome))
            } else {
                db.reposicaoDao().atualizar(reposicao)
            }
            db.faltaDao().atualizarStatus(falta.id, StatusFalta.REPOSTA.name)
            db.avisoDao().inserir(
                Aviso(
                    tipo = TipoAviso.REPOSICAO,
                    titulo = "Reposição agendada: ${falta.disciplina}",
                    mensagem = "A aula de ${Formatos.isoParaBr(falta.data)} será reposta em " +
                        "${Formatos.isoParaBr(reposicao.data)} às ${reposicao.horario}, " +
                        "sala ${reposicao.sala}.",
                    curso = falta.curso
                )
            )
        }
    }

    suspend fun excluirReposicao(reposicao: Reposicao, autor: Usuario) {
        exigir(
            autor.perfil == Perfil.COORDENADOR,
            "Apenas coordenadores de curso podem gerir reposições."
        )
        db.withTransaction {
            db.reposicaoDao().excluir(reposicao)
            db.faltaDao().atualizarStatus(reposicao.faltaId, StatusFalta.PENDENTE.name)
        }
    }

    // ---------- Usuários (somente CAE) ----------

    suspend fun buscarUsuario(id: Long) = db.usuarioDao().buscar(id)

    suspend fun salvarUsuario(usuario: Usuario, autor: Usuario) {
        exigir(autor.perfil == Perfil.CAE, "Apenas a CAE pode gerenciar usuários.")
        exigir(
            db.usuarioDao().contarLogin(usuario.login, usuario.id) == 0,
            "O login \"${usuario.login}\" já está em uso."
        )
        exigir(
            !(usuario.id == autor.id && usuario.perfil != Perfil.CAE),
            "Você não pode remover o seu próprio perfil de CAE."
        )
        if (usuario.id == 0L) db.usuarioDao().inserir(usuario)
        else db.usuarioDao().atualizar(usuario)
    }

    suspend fun excluirUsuario(usuario: Usuario, autor: Usuario) {
        exigir(autor.perfil == Perfil.CAE, "Apenas a CAE pode gerenciar usuários.")
        exigir(usuario.id != autor.id, "Você não pode excluir o próprio usuário.")
        db.usuarioDao().excluir(usuario)
    }
}
