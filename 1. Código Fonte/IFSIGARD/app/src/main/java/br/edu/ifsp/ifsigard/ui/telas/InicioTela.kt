package br.edu.ifsp.ifsigard.ui.telas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.edu.ifsp.ifsigard.data.Perfil
import br.edu.ifsp.ifsigard.data.StatusFalta
import br.edu.ifsp.ifsigard.ui.AppViewModel
import br.edu.ifsp.ifsigard.ui.componentes.TelaBase

@Composable
fun InicioTela(
    vm: AppViewModel,
    onFaltas: () -> Unit,
    onReposicoes: () -> Unit,
    onUsuarios: () -> Unit,
    onAvisos: () -> Unit,
    onSair: () -> Unit
) {
    val usuario by vm.usuario.collectAsStateWithLifecycle()
    val faltas by vm.faltas.collectAsStateWithLifecycle()
    val avisos by vm.avisos.collectAsStateWithLifecycle()
    val perfil = usuario?.perfil ?: return
    val pendentes = faltas.count { it.status == StatusFalta.PENDENTE }

    TelaBase(
        titulo = "Início",
        acoes = {
            IconButton(onClick = onSair) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Sair")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Olá, ${usuario?.nome}", style = MaterialTheme.typography.headlineSmall)
            Text("Perfil: ${perfil.rotulo}", color = MaterialTheme.colorScheme.onSurfaceVariant)

            if (perfil != Perfil.ALUNO) {
                CartaoMenu("Faltas", "$pendentes com reposição pendente · registrar e editar", onFaltas)
            }
            if (perfil == Perfil.COORDENADOR) {
                CartaoMenu("Reposições", "Agendar a reposição das aulas", onReposicoes)
            }
            if (perfil == Perfil.CAE) {
                CartaoMenu("Usuários", "Cadastrar alunos, professores e coordenadores", onUsuarios)
            }
            CartaoMenu("Avisos", "${avisos.size} aviso(s) no dashboard", onAvisos)
        }
    }
}

@Composable
private fun CartaoMenu(titulo: String, descricao: String, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(Modifier.padding(20.dp)) {
            Text(titulo, style = MaterialTheme.typography.titleLarge)
            Text(descricao, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
