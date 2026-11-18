package br.edu.ifsp.ifsigard.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import br.edu.ifsp.ifsigard.data.AppDatabase
import br.edu.ifsp.ifsigard.data.Falta
import br.edu.ifsp.ifsigard.data.Perfil
import br.edu.ifsp.ifsigard.data.RegraNegocioException
import br.edu.ifsp.ifsigard.data.Reposicao
import br.edu.ifsp.ifsigard.data.Repositorio
import br.edu.ifsp.ifsigard.data.Usuario
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AppViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = Repositorio(AppDatabase.obter(app))

    private val _usuario = MutableStateFlow<Usuario?>(null)
    val usuario: StateFlow<Usuario?> = _usuario.asStateFlow()

    private val _mensagens = Channel<String>(Channel.BUFFERED)
    val mensagens: Flow<String> = _mensagens.receiveAsFlow()

    private fun <T> Flow<List<T>>.emEstado(): StateFlow<List<T>> =
        stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val usuarios = repo.usuarios.emEstado()
    val docentes = repo.docentes.emEstado()
    val faltas = repo.faltas.emEstado()
    val reposicoes = repo.reposicoes.emEstado()

    /** Alunos veem apenas os avisos do seu curso (ou avisos gerais, sem curso). */
    val avisos = combine(repo.avisos, _usuario) { lista, usuario ->
        if (usuario?.perfil == Perfil.ALUNO && usuario.curso.isNotBlank()) {
            lista.filter { it.curso.isBlank() || it.curso.equals(usuario.curso, ignoreCase = true) }
        } else {
            lista
        }
    }.emEstado()

    /** Executa uma ação, trata as exceções e envia a mensagem de resultado. */
    private fun executar(
        sucesso: String? = null,
        aoConcluir: () -> Unit = {},
        acao: suspend () -> Unit
    ) {
        viewModelScope.launch {
            try {
                acao()
                sucesso?.let { _mensagens.send(it) }
                aoConcluir()
            } catch (e: CancellationException) {
                throw e
            } catch (e: RegraNegocioException) {
                _mensagens.send(e.message ?: "Operação não permitida.")
            } catch (e: Exception) {
                _mensagens.send("Erro inesperado: ${e.message}")
            }
        }
    }

    private fun autor(): Usuario =
        _usuario.value ?: throw RegraNegocioException("Sessão expirada. Entre novamente.")

    // ---------- Sessão ----------

    fun entrar(login: String, senha: String, aoEntrar: () -> Unit) = executar(aoConcluir = aoEntrar) {
        _usuario.value = repo.autenticar(login, senha)
    }

    fun sair() {
        _usuario.value = null
    }

    // ---------- Faltas ----------

    suspend fun buscarFalta(id: Long) = repo.buscarFalta(id)

    fun salvarFalta(falta: Falta, aoSalvar: () -> Unit) =
        executar("Falta salva e aviso publicado.", aoSalvar) { repo.salvarFalta(falta, autor()) }

    fun excluirFalta(falta: Falta) =
        executar("Falta excluída.") { repo.excluirFalta(falta, autor()) }

    // ---------- Reposições ----------

    suspend fun buscarReposicao(id: Long) = repo.buscarReposicao(id)

    fun salvarReposicao(reposicao: Reposicao, aoSalvar: () -> Unit) =
        executar("Reposição salva e aviso publicado.", aoSalvar) {
            repo.salvarReposicao(reposicao, autor())
        }

    fun excluirReposicao(reposicao: Reposicao) =
        executar("Reposição excluída. A falta voltou a ficar pendente.") {
            repo.excluirReposicao(reposicao, autor())
        }

    // ---------- Usuários ----------

    suspend fun buscarUsuario(id: Long) = repo.buscarUsuario(id)

    fun salvarUsuario(usuario: Usuario, aoSalvar: () -> Unit) =
        executar("Usuário salvo.", aoSalvar) { repo.salvarUsuario(usuario, autor()) }

    fun excluirUsuario(usuario: Usuario) =
        executar("Usuário excluído.") { repo.excluirUsuario(usuario, autor()) }
}
