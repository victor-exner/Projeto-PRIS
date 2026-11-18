package br.edu.ifsp.ifsigard.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** Perfis de acesso definidos no diagrama de casos de uso. */
enum class Perfil(val rotulo: String) {
    CAE("CAE"),
    COORDENADOR("Coordenador"),
    PROFESSOR("Professor"),
    ALUNO("Aluno")
}

enum class StatusFalta(val rotulo: String) {
    PENDENTE("Reposição pendente"),
    REPOSTA("Reposta")
}

enum class TipoAviso(val rotulo: String) {
    FALTA("Falta"),
    REPOSICAO("Reposição")
}

@Entity(
    tableName = "usuarios",
    indices = [Index(value = ["login"], unique = true)]
)
data class Usuario(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nome: String,
    val login: String,
    val senha: String,
    val perfil: Perfil,
    /** Curso do aluno (usado para filtrar os avisos). Opcional para os demais perfis. */
    val curso: String = ""
)

/** Caso de uso "Manter Falta": faltante, data, disciplina, curso e descrição. */
@Entity(tableName = "faltas")
data class Falta(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val faltanteId: Long,
    val faltanteNome: String,
    /** Data no formato ISO (aaaa-mm-dd), para ordenar corretamente. */
    val data: String,
    val disciplina: String,
    val curso: String,
    val descricao: String,
    val registradoPor: String,
    val status: StatusFalta = StatusFalta.PENDENTE
)

/** Caso de uso "Manter Reposição": somente o Coordenador. Uma reposição por falta. */
@Entity(
    tableName = "reposicoes",
    foreignKeys = [
        ForeignKey(
            entity = Falta::class,
            parentColumns = ["id"],
            childColumns = ["faltaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["faltaId"], unique = true)]
)
data class Reposicao(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val faltaId: Long,
    /** Data no formato ISO (aaaa-mm-dd). */
    val data: String,
    /** Horário no formato HH:mm. */
    val horario: String,
    val sala: String,
    val observacao: String,
    val coordenadorNome: String
)

/** Item do "dashboard" de avisos, gerado ao registrar faltas e reposições. */
@Entity(tableName = "avisos")
data class Aviso(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tipo: TipoAviso,
    val titulo: String,
    val mensagem: String,
    val curso: String,
    val criadoEm: Long = System.currentTimeMillis()
)
