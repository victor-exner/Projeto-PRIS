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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.edu.ifsp.ifsigard.data.Falta
import br.edu.ifsp.ifsigard.data.Reposicao
import br.edu.ifsp.ifsigard.data.StatusFalta
import br.edu.ifsp.ifsigard.ui.AppViewModel
import br.edu.ifsp.ifsigard.ui.componentes.CampoSelecao
import br.edu.ifsp.ifsigard.ui.componentes.CampoTexto
import br.edu.ifsp.ifsigard.ui.componentes.DialogoConfirmacao
import br.edu.ifsp.ifsigard.ui.componentes.ListaVazia
import br.edu.ifsp.ifsigard.ui.componentes.TelaBase
import br.edu.ifsp.ifsigard.util.Formatos

private fun descreverFalta(f: Falta) =
    "${f.disciplina} · ${f.faltanteNome} · ${Formatos.isoParaBr(f.data)}"

@Composable
fun ReposicoesTela(
    vm: AppViewModel,
    onVoltar: () -> Unit,
    onNova: () -> Unit,
    onEditar: (Long) -> Unit
) {
    val reposicoes by vm.reposicoes.collectAsStateWithLifecycle()
    val faltas by vm.faltas.collectAsStateWithLifecycle()
    val faltasPorId = faltas.associateBy { it.id }
    val pendentes = faltas.count { it.status == StatusFalta.PENDENTE }
    var paraExcluir by remember { mutableStateOf<Reposicao?>(null) }

    TelaBase(
        titulo = "Reposições",
        onVoltar = onVoltar,
        botaoFlutuante = {
            FloatingActionButton(onClick = onNova) {
                Icon(Icons.Default.Add, contentDescription = "Agendar reposição")
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
            item {
                Text(
                    "$pendentes falta(s) aguardando reposição",
                    style = MaterialTheme.typography.titleSmall,
                    color = if (pendentes > 0) MaterialTheme.colorScheme.tertiary
                    else MaterialTheme.colorScheme.primary
                )
            }
            if (reposicoes.isEmpty()) {
                item { ListaVazia("Nenhuma reposição agendada.", PaddingValues()) }
            }
            items(reposicoes, key = { it.id }) { r ->
                val falta = faltasPorId[r.faltaId]
                Card(onClick = { onEditar(r.id) }, modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                "${Formatos.isoParaBr(r.data)} às ${r.horario} · sala ${r.sala}",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(falta?.let { "Repõe: ${descreverFalta(it)}" } ?: "Falta removida")
                            if (r.observacao.isNotBlank()) {
                                Text(
                                    r.observacao,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        IconButton(onClick = { paraExcluir = r }) {
                            Icon(Icons.Default.Delete, contentDescription = "Excluir")
                        }
                    }
                }
            }
        }
    }

    paraExcluir?.let { r ->
        DialogoConfirmacao(
            titulo = "Excluir reposição?",
            texto = "A falta vinculada voltará a ficar com reposição pendente.",
            onConfirmar = {
                vm.excluirReposicao(r)
                paraExcluir = null
            },
            onCancelar = { paraExcluir = null }
        )
    }
}

@Composable
fun ReposicaoFormTela(vm: AppViewModel, reposicaoId: Long, onVoltar: () -> Unit) {
    val faltas by vm.faltas.collectAsStateWithLifecycle()

    var original by remember { mutableStateOf<Reposicao?>(null) }
    var faltaId by rememberSaveable { mutableStateOf<Long?>(null) }
    var data by rememberSaveable { mutableStateOf("") }
    var horario by rememberSaveable { mutableStateOf("") }
    var sala by rememberSaveable { mutableStateOf("") }
    var observacao by rememberSaveable { mutableStateOf("") }
    var tentou by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(reposicaoId) {
        if (reposicaoId > 0) {
            vm.buscarReposicao(reposicaoId)?.let { r ->
                original = r
                faltaId = r.faltaId
                data = Formatos.isoParaBr(r.data)
                horario = r.horario
                sala = r.sala
                observacao = r.observacao
            }
        }
    }

    // Na criação, só faltas pendentes; na edição, a falta já vinculada.
    val opcoes = faltas.filter { it.status == StatusFalta.PENDENTE || it.id == original?.faltaId }
    val falta = faltas.firstOrNull { it.id == faltaId }
    val dataLida = Formatos.lerData(data)
    val horaLida = Formatos.lerHora(horario)
    val erros = mapOf(
        "falta" to if (falta == null) "Selecione a falta a repor." else null,
        "data" to when {
            data.isBlank() -> "Informe a data."
            dataLida == null -> "Data inválida. Use dd/mm/aaaa."
            falta != null && dataLida.toString() < falta.data -> "A reposição deve ser depois da falta."
            else -> null
        },
        "horario" to when {
            horario.isBlank() -> "Informe o horário."
            horaLida == null -> "Horário inválido. Use hh:mm."
            else -> null
        },
        "sala" to if (sala.isBlank()) "Informe a sala." else null
    )
    fun erro(campo: String) = if (tentou) erros[campo] else null

    TelaBase(
        titulo = if (reposicaoId > 0) "Editar reposição" else "Agendar reposição",
        onVoltar = onVoltar
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            CampoSelecao(
                rotulo = "Falta a repor",
                opcoes = opcoes,
                selecionado = falta,
                textoDe = ::descreverFalta,
                onSelecionar = { faltaId = it.id },
                erro = erro("falta"),
                habilitado = original == null
            )
            falta?.let {
                Text(
                    "Motivo: ${it.descricao}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            CampoTexto(data, { data = it }, "Data da reposição", erro("data"), dica = "dd/mm/aaaa")
            CampoTexto(
                horario, { horario = it }, "Horário", erro("horario"),
                dica = "hh:mm", tipoTeclado = KeyboardType.Number
            )
            CampoTexto(sala, { sala = it }, "Sala", erro("sala"))
            CampoTexto(observacao, { observacao = it }, "Observação (opcional)", umaLinha = false)
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    tentou = true
                    if (erros.values.all { it == null } && falta != null && dataLida != null && horaLida != null) {
                        vm.salvarReposicao(
                            Reposicao(
                                id = original?.id ?: 0,
                                faltaId = falta.id,
                                data = dataLida.toString(),
                                horario = horaLida.toString(),
                                sala = sala.trim(),
                                observacao = observacao.trim(),
                                coordenadorNome = original?.coordenadorNome.orEmpty()
                            ),
                            onVoltar
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Salvar e avisar as turmas") }
        }
    }
}
