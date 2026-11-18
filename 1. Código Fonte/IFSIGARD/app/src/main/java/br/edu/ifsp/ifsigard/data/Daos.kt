package br.edu.ifsp.ifsigard.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UsuarioDao {
    @Query("SELECT * FROM usuarios ORDER BY nome")
    fun listar(): Flow<List<Usuario>>

    @Query("SELECT * FROM usuarios WHERE perfil IN ('PROFESSOR', 'COORDENADOR') ORDER BY nome")
    fun listarDocentes(): Flow<List<Usuario>>

    @Query("SELECT * FROM usuarios WHERE login = :login AND senha = :senha LIMIT 1")
    suspend fun autenticar(login: String, senha: String): Usuario?

    @Query("SELECT * FROM usuarios WHERE id = :id")
    suspend fun buscar(id: Long): Usuario?

    @Query("SELECT COUNT(*) FROM usuarios WHERE login = :login AND id != :ignorarId")
    suspend fun contarLogin(login: String, ignorarId: Long): Int

    @Insert
    suspend fun inserir(usuario: Usuario): Long

    @Update
    suspend fun atualizar(usuario: Usuario)

    @Delete
    suspend fun excluir(usuario: Usuario)
}

@Dao
interface FaltaDao {
    @Query("SELECT * FROM faltas ORDER BY data DESC, id DESC")
    fun listar(): Flow<List<Falta>>

    @Query("SELECT * FROM faltas WHERE id = :id")
    suspend fun buscar(id: Long): Falta?

    @Insert
    suspend fun inserir(falta: Falta): Long

    @Update
    suspend fun atualizar(falta: Falta)

    @Delete
    suspend fun excluir(falta: Falta)

    @Query("UPDATE faltas SET status = :status WHERE id = :id")
    suspend fun atualizarStatus(id: Long, status: String)
}

@Dao
interface ReposicaoDao {
    @Query("SELECT * FROM reposicoes ORDER BY data DESC, horario DESC")
    fun listar(): Flow<List<Reposicao>>

    @Query("SELECT * FROM reposicoes WHERE id = :id")
    suspend fun buscar(id: Long): Reposicao?

    @Query("SELECT * FROM reposicoes WHERE faltaId = :faltaId LIMIT 1")
    suspend fun buscarPorFalta(faltaId: Long): Reposicao?

    @Insert
    suspend fun inserir(reposicao: Reposicao): Long

    @Update
    suspend fun atualizar(reposicao: Reposicao)

    @Delete
    suspend fun excluir(reposicao: Reposicao)
}

@Dao
interface AvisoDao {
    @Query("SELECT * FROM avisos ORDER BY criadoEm DESC")
    fun listar(): Flow<List<Aviso>>

    @Insert
    suspend fun inserir(aviso: Aviso): Long
}
