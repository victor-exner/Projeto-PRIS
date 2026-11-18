package br.edu.ifsp.ifsigard.ui.componentes

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

/** Estrutura comum das telas: barra superior com título e botão voltar. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaBase(
    titulo: String,
    onVoltar: (() -> Unit)? = null,
    acoes: @Composable () -> Unit = {},
    botaoFlutuante: @Composable () -> Unit = {},
    conteudo: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(titulo) },
                navigationIcon = {
                    if (onVoltar != null) {
                        IconButton(onClick = onVoltar) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                        }
                    }
                },
                actions = { acoes() },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = botaoFlutuante,
        content = conteudo
    )
}

/** Campo de texto com mensagem de erro de validação. */
@Composable
fun CampoTexto(
    valor: String,
    onValorChange: (String) -> Unit,
    rotulo: String,
    erro: String? = null,
    dica: String? = null,
    tipoTeclado: KeyboardType = KeyboardType.Text,
    umaLinha: Boolean = true,
    transformacao: VisualTransformation = VisualTransformation.None
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onValorChange,
        label = { Text(rotulo) },
        placeholder = dica?.let { { Text(it) } },
        isError = erro != null,
        supportingText = erro?.let { { Text(it) } },
        singleLine = umaLinha,
        minLines = if (umaLinha) 1 else 3,
        keyboardOptions = KeyboardOptions(keyboardType = tipoTeclado),
        visualTransformation = transformacao,
        modifier = Modifier.fillMaxWidth()
    )
}

/** Lista suspensa para escolher um item (docente, falta, perfil...). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> CampoSelecao(
    rotulo: String,
    opcoes: List<T>,
    selecionado: T?,
    textoDe: (T) -> String,
    onSelecionar: (T) -> Unit,
    erro: String? = null,
    habilitado: Boolean = true
) {
    var aberto by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = aberto && habilitado,
        onExpandedChange = { if (habilitado) aberto = it }
    ) {
        OutlinedTextField(
            value = selecionado?.let(textoDe) ?: "",
            onValueChange = {},
            readOnly = true,
            enabled = habilitado,
            label = { Text(rotulo) },
            isError = erro != null,
            supportingText = erro?.let { { Text(it) } },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = aberto) },
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = habilitado)
                .fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = aberto, onDismissRequest = { aberto = false }) {
            if (opcoes.isEmpty()) {
                DropdownMenuItem(text = { Text("Nenhuma opção disponível") }, onClick = { aberto = false })
            }
            opcoes.forEach { opcao ->
                DropdownMenuItem(
                    text = { Text(textoDe(opcao)) },
                    onClick = {
                        onSelecionar(opcao)
                        aberto = false
                    }
                )
            }
        }
    }
}

@Composable
fun DialogoConfirmacao(
    titulo: String,
    texto: String,
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text(titulo) },
        text = { Text(texto) },
        confirmButton = { TextButton(onClick = onConfirmar) { Text("Excluir") } },
        dismissButton = { TextButton(onClick = onCancelar) { Text("Cancelar") } }
    )
}

@Composable
fun ListaVazia(texto: String, padding: PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                texto,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
