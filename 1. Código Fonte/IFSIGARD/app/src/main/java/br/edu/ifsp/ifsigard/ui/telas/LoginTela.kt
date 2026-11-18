package br.edu.ifsp.ifsigard.ui.telas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import br.edu.ifsp.ifsigard.ui.AppViewModel
import br.edu.ifsp.ifsigard.ui.componentes.CampoTexto

@Composable
fun LoginTela(vm: AppViewModel, onEntrar: () -> Unit) {
    var login by rememberSaveable { mutableStateOf("") }
    var senha by rememberSaveable { mutableStateOf("") }
    var tentou by rememberSaveable { mutableStateOf(false) }

    val erroLogin = if (tentou && login.isBlank()) "Informe o usuário." else null
    val erroSenha = if (tentou && senha.isBlank()) "Informe a senha." else null

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "IFSIGARD",
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            "Gestão de Ausências e Reposições Docentes",
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(Modifier.height(32.dp))

        CampoTexto(login, { login = it }, "Usuário", erroLogin)
        CampoTexto(
            senha, { senha = it }, "Senha", erroSenha,
            tipoTeclado = KeyboardType.Password,
            transformacao = PasswordVisualTransformation()
        )
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = {
                tentou = true
                if (login.isNotBlank() && senha.isNotBlank()) vm.entrar(login, senha, onEntrar)
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Entrar") }

        Spacer(Modifier.height(32.dp))
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("Usuários de teste (senha 123)", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(4.dp))
                Text("cae · coord · prof · aluno", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
