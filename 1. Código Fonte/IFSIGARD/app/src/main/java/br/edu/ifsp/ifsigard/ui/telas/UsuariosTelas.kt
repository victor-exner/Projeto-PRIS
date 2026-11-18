package br.edu.ifsp.ifsigard.ui.telas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.edu.ifsp.ifsigard.data.Perfil
import br.edu.ifsp.ifsigard.data.Usuario
import br.edu.ifsp.ifsigard.ui.AppViewModel
import br.edu.ifsp.ifsigard.ui.componentes.CampoSelecao
import br.edu.ifsp.ifsigard.ui.componentes.CampoTexto
import br.edu.ifsp.ifsigard.ui.componentes.DialogoConfirmacao
import br.edu.ifsp.ifsigard.ui.componentes.TelaBase

@Composable
fun UsuariosTela(
    vm: AppViewModel,
    onVoltar: () -> Unit,
    onNovo: () -> Unit,
    onEditar: (Long) -> Unit
) {
    val usuarios by vm.usuarios.collectAsStateWithLifecycle()
    var paraExcluir by remember { mutableStateOf<Usuario?>(null) }

    TelaBase(
        titulo = "Usuários",
        onVoltar = onVoltar,
        botaoFlutuante = {
            FloatingActionButton(onClick = onNovo) {
                Icon(Icons.Default.Add, contentDescription = "Novo usuário")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp, end = 16.dp,
                top = padding.calculateTopPadding() + 8.dp, bottom = 88.dp
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(usuarios, key = { it.id }) { u ->
                Card(onClick = { onEditar(u.id) }, modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(u.nome, style = MaterialTheme.typography.titleMedium)
                            Text(
                                buildString {
                                    append(u.perfil.rotulo)
                                    append(" · login: ")
                                    append(u.login)
                                    if (u.curso.isNotBlank()) append(" · ${u.curso}")
                                },
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        IconButton(onClick = { paraExcluir = u }) {
                            Icon(Icons.Default.Delete, contentDescription = "Excluir")
                        }
                    }
                }
            }
        }
    }

    paraExcluir?.let { u ->
        DialogoConfirmacao(
            titulo = "Excluir usuário?",
            texto = "${u.nome} não poderá mais acessar o aplicativo.",
            onConfirmar = {
                vm.excluirUsuario(u)
                paraExcluir = null
            },
            onCancelar = { paraExcluir = null }
        )
    }
}

@Composable
fun UsuarioFormTela(vm: AppViewModel, usuarioId: Long, onVoltar: () -> Unit) {
    var original by remember { mutableStateOf<Usuario?>(null) }
    var nome by rememberSaveable { mutableStateOf("") }
    var login by rememberSaveable { mutableStateOf("") }
    var senha by rememberSaveable { mutableStateOf("") }
    var perfil by rememberSaveable { mutableStateOf<Perfil?>(null) }
    var curso by rememberSaveable { mutableStateOf("") }
    var tentou by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(usuarioId) {
        if (usuarioId > 0) {
            vm.buscarUsuario(usuarioId)?.let { u ->
                original = u
                nome = u.nome
                login = u.login
                senha = u.senha
                perfil = u.perfil
                curso = u.curso
            }
        }
    }

    val erros = mapOf(
        "nome" to if (nome.trim().length < 3) "Informe o nome completo." else null,
        "login" to when {
            login.isBlank() -> "Informe o login."
            login.contains(' ') -> "O login não pode ter espaços."
            else -> null
        },
        "senha" to if (senha.length < 3) "A senha deve ter ao menos 3 caracteres." else null,
        "perfil" to if (perfil == null) "Selecione o perfil." else null,
        "curso" to if (perfil == Perfil.ALUNO && curso.isBlank()) "Informe o curso do aluno." else null
    )
    fun erro(campo: String) = if (tentou) erros[campo] else null

    TelaBase(
        titulo = if (usuarioId > 0) "Editar usuário" else "Novo usuário",
        onVoltar = onVoltar
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            CampoTexto(nome, { nome = it }, "Nome", erro("nome"))
            CampoTexto(login, { login = it }, "Login", erro("login"))
            CampoTexto(
                senha, { senha = it }, "Senha", erro("senha"),
                transformacao = PasswordVisualTransformation()
            )
            CampoSelecao(
                rotulo = "Perfil",
                opcoes = Perfil.entries.toList(),
                selecionado = perfil,
                textoDe = { it.rotulo },
                onSelecionar = { perfil = it },
                erro = erro("perfil")
            )
            CampoTexto(
                curso, { curso = it },
                if (perfil == Perfil.ALUNO) "Curso" else "Curso (opcional)",
                erro("curso"), dica = "Ex.: ADS"
            )
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    tentou = true
                    val p = perfil
                    if (erros.values.all { it == null } && p != null) {
                        vm.salvarUsuario(
                            Usuario(
                                id = original?.id ?: 0,
                                nome = nome.trim(),
                                login = login.trim().lowercase(),
                                senha = senha,
                                perfil = p,
                                curso = curso.trim().uppercase()
                            ),
                            onVoltar
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Salvar") }
        }
    }
}
