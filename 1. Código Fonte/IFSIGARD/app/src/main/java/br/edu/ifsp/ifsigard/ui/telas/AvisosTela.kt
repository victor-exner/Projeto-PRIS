package br.edu.ifsp.ifsigard.ui.telas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.edu.ifsp.ifsigard.data.TipoAviso
import br.edu.ifsp.ifsigard.ui.AppViewModel
import br.edu.ifsp.ifsigard.ui.componentes.ListaVazia
import br.edu.ifsp.ifsigard.ui.componentes.TelaBase
import br.edu.ifsp.ifsigard.util.Formatos

/** Caso de uso "Visualizar Avisos": o dashboard que o aluno consulta. */
@Composable
fun AvisosTela(vm: AppViewModel, onVoltar: () -> Unit) {
    val avisos by vm.avisos.collectAsStateWithLifecycle()

    TelaBase(titulo = "Avisos", onVoltar = onVoltar) { padding ->
        if (avisos.isEmpty()) {
            ListaVazia("Nenhum aviso por enquanto.", padding)
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp, end = 16.dp,
                    top = padding.calculateTopPadding() + 8.dp, bottom = 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(avisos, key = { it.id }) { aviso ->
                    val falta = aviso.tipo == TipoAviso.FALTA
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (falta) MaterialTheme.colorScheme.errorContainer
                            else MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(
                                "${aviso.tipo.rotulo} · ${aviso.curso} · ${Formatos.momento(aviso.criadoEm)}",
                                style = MaterialTheme.typography.labelMedium
                            )
                            Text(aviso.titulo, style = MaterialTheme.typography.titleMedium)
                            Text(aviso.mensagem, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}
