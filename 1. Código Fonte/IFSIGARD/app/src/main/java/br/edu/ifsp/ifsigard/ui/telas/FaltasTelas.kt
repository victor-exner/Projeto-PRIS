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
import androidx.compose.material3.AssistChip
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.edu.ifsp.ifsigard.data.Falta
import br.edu.ifsp.ifsigard.data.Perfil
import br.edu.ifsp.ifsigard.data.StatusFalta
import br.edu.ifsp.ifsigard.ui.AppViewModel
import br.edu.ifsp.ifsigard.ui.componentes.CampoSelecao
import br.edu.ifsp.ifsigard.ui.componentes.CampoTexto
import br.edu.ifsp.ifsigard.ui.componentes.DialogoConfirmacao
import br.edu.ifsp.ifsigard.ui.componentes.ListaVazia
import br.edu.ifsp.ifsigard.ui.componentes.TelaBase
import br.edu.ifsp.ifsigard.util.Formatos

@Composable
fun FaltasTela(
    vm: AppViewModel,
    onVoltar: () -> Unit,
    onNova: () -> Unit,
    onEditar: (Long) -> Unit
) {
    val faltas by vm.faltas.collectAsStateWithLifecycle()
    var paraExcluir by remember { mutableStateOf<Falta?>(null) }

    TelaBase(
        titulo = "Faltas",
        onVoltar = onVoltar,
        botaoFlutuante = {
            FloatingActionButton(onClick = onNova) {
                Icon(Icons.Default.Add, contentDescription = "Registrar falta")
            }
        }
    ) { padding ->
        if (faltas.isEmpty()) {
            ListaVazia("Nenhuma falta registrada. Toque em + para registrar.", padding)
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp, end = 16.dp,
                    top = padding.calculateTopPadding() + 8.dp, bottom = 88.dp
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(faltas, key = { it.id }) { falta ->
                    CartaoFalta(falta, onClick = { onEditar(falta.id) }, onExcluir = { paraExcluir = falta })
                }
            }
        }
    }

    paraExcluir?.let { falta ->
        DialogoConfirmacao(
            titulo = "Excluir falta?",
            texto = "A falta de ${falta.faltanteNome} em ${Formatos.isoParaBr(falta.data)} " +
                "e a reposição vinculada serão excluídas.",
            onConfirmar = {
                vm.excluirFalta(falta)
                paraExcluir = null
            },
            onCancelar = { paraExcluir = null }
        )
    }
}

@Composable
private fun CartaoFalta(falta: Falta, onClick: () -> Unit, onExcluir: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Text(falta.disciplina, style = MaterialTheme.typography.titleMedium)
                Text("${falta.faltanteNome} · ${Formatos.isoParaBr(falta.data)} · ${falta.curso}")
                Text(
                    falta.descricao,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                AssistChip(
                    onClick = onClick,
                    label = {
                        Text(
                            falta.status.rotulo,
                            color = if (falta.status == StatusFalta.PENDENTE)
                                MaterialTheme.colorScheme.tertiary
                            else MaterialTheme.colorScheme.primary
                        )
                    }
                )
            }
            IconButton(onClick = onExcluir) {
                Icon(Icons.Default.Delete, contentDescription = "Excluir")
            }
        }
    }
}

@Composable
fun FaltaFormTela(vm: AppViewModel, faltaId: Long, onVoltar: () -> Unit) {
    val usuario by vm.usuario.collectAsStateWithLifecycle()
    val docentes by vm.docentes.collectAsStateWithLifecycle()

    var original by remember { mutableStateOf<Falta?>(null) }
    var faltanteId by rememberSaveable { mutableStateOf<Long?>(null) }
    var data by rememberSaveable { mutableStateOf("") }
    var disciplina by rememberSaveable { mutableStateOf("") }
    var curso by rememberSaveable { mutableStateOf("") }
    var descricao by rememberSaveable { mutableStateOf("") }
    var tentou by rememberSaveable { mutableStateOf(false) }

    // Carrega a falta para edição, ou sugere o próprio professor como faltante.
    LaunchedEffect(faltaId) {
        if (faltaId > 0) {
            vm.buscarFalta(faltaId)?.let { f ->
                original = f
                faltanteId = f.faltanteId
                data = Formatos.isoParaBr(f.data)
                disciplina = f.disciplina
                curso = f.curso
                descricao = f.descricao
            }
        } else if (faltanteId == null && usuario?.perfil != Perfil.CAE) {
            faltanteId = usuario?.id
            curso = usuario?.curso.orEmpty()
        }
    }

    val docente = docentes.firstOrNull { it.id == faltanteId }
    val dataLida = Formatos.lerData(data)
    val erros = mapOf(
        "faltante" to if (docente == null) "Selecione o docente." else null,
        "data" to when {
            data.isBlank() -> "Informe a data."
            dataLida == null -> "Data inválida. Use dd/mm/aaaa."
            else -> null
        },
        "disciplina" to if (disciplina.trim().length < 3) "Informe a disciplina (mín. 3 letras)." else null,
        "curso" to if (curso.isBlank()) "Informe o curso." else null,
        "descricao" to if (descricao.trim().length < 5) "Descreva o motivo (mín. 5 letras)." else null
    )
    fun erro(campo: String) = if (tentou) erros[campo] else null

    TelaBase(
        titulo = if (faltaId > 0) "Editar falta" else "Registrar falta",
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
                rotulo = "Docente faltante",
                opcoes = docentes,
                selecionado = docente,
                textoDe = { it.nome },
                onSelecionar = {
                    faltanteId = it.id
                    if (curso.isBlank()) curso = it.curso
                },
                erro = erro("faltante")
            )
            CampoTexto(data, { data = it }, "Data da ausência", erro("data"), dica = "dd/mm/aaaa")
            CampoTexto(disciplina, { disciplina = it }, "Disciplina", erro("disciplina"))
            CampoTexto(curso, { curso = it }, "Curso", erro("curso"), dica = "Ex.: ADS")
            CampoTexto(descricao, { descricao = it }, "Motivo / descrição", erro("descricao"), umaLinha = false)
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    tentou = true
                    if (erros.values.all { it == null } && docente != null && dataLida != null) {
                        val falta = Falta(
                            id = original?.id ?: 0,
                            faltanteId = docente.id,
                            faltanteNome = docente.nome,
                            data = dataLida.toString(),
                            disciplina = disciplina.trim(),
                            curso = curso.trim().uppercase(),
                            descricao = descricao.trim(),
                            registradoPor = original?.registradoPor ?: usuario?.nome.orEmpty(),
                            status = original?.status ?: StatusFalta.PENDENTE
                        )
                        vm.salvarFalta(falta, onVoltar)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Salvar e avisar as turmas") }

            original?.let {
                Text(
                    "Registrada por ${it.registradoPor}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
