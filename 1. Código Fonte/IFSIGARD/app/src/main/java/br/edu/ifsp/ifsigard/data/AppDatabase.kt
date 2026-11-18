package br.edu.ifsp.ifsigard.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [Usuario::class, Falta::class, Reposicao::class, Aviso::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun usuarioDao(): UsuarioDao
    abstract fun faltaDao(): FaltaDao
    abstract fun reposicaoDao(): ReposicaoDao
    abstract fun avisoDao(): AvisoDao

    companion object {
        @Volatile
        private var instancia: AppDatabase? = null

        /** Usuários criados na primeira execução, para testar cada perfil (senha: 123). */
        private val usuariosIniciais = listOf(
            arrayOf("Ana (CAE)", "cae", "123", Perfil.CAE.name, ""),
            arrayOf("Carlos (Coordenador)", "coord", "123", Perfil.COORDENADOR.name, "ADS"),
            arrayOf("Paula (Professora)", "prof", "123", Perfil.PROFESSOR.name, "ADS"),
            arrayOf("Lucas (Aluno)", "aluno", "123", Perfil.ALUNO.name, "ADS")
        )

        fun obter(context: Context): AppDatabase =
            instancia ?: synchronized(this) {
                instancia ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ifsigard.db"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            usuariosIniciais.forEach { valores ->
                                db.execSQL(
                                    "INSERT INTO usuarios (nome, login, senha, perfil, curso) VALUES (?, ?, ?, ?, ?)",
                                    valores
                                )
                            }
                        }
                    })
                    .build()
                    .also { instancia = it }
            }
    }
}
