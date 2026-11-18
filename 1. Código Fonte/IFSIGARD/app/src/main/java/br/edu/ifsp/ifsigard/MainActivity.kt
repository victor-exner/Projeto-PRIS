package br.edu.ifsp.ifsigard

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import br.edu.ifsp.ifsigard.ui.AppViewModel
import br.edu.ifsp.ifsigard.ui.tema.TemaIfsigard
import br.edu.ifsp.ifsigard.ui.telas.AvisosTela
import br.edu.ifsp.ifsigard.ui.telas.FaltaFormTela
import br.edu.ifsp.ifsigard.ui.telas.FaltasTela
import br.edu.ifsp.ifsigard.ui.telas.InicioTela
import br.edu.ifsp.ifsigard.ui.telas.LoginTela
import br.edu.ifsp.ifsigard.ui.telas.ReposicaoFormTela
import br.edu.ifsp.ifsigard.ui.telas.ReposicoesTela
import br.edu.ifsp.ifsigard.ui.telas.UsuarioFormTela
import br.edu.ifsp.ifsigard.ui.telas.UsuariosTela

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TemaIfsigard {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    AppIfsigard()
                }
            }
        }
    }
}

@Composable
fun AppIfsigard(vm: AppViewModel = viewModel()) {
    val nav = rememberNavController()
    val contexto = LocalContext.current

    // Mensagens de sucesso e de erro (exceções tratadas no ViewModel).
    LaunchedEffect(Unit) {
        vm.mensagens.collect { Toast.makeText(contexto, it, Toast.LENGTH_SHORT).show() }
    }

    val argId = listOf(navArgument("id") { type = NavType.LongType })
    val voltar: () -> Unit = { nav.popBackStack() }

    NavHost(navController = nav, startDestination = "login") {
        composable("login") {
            LoginTela(vm) {
                nav.navigate("inicio") { popUpTo("login") { inclusive = true } }
            }
        }
        composable("inicio") {
            InicioTela(
                vm = vm,
                onFaltas = { nav.navigate("faltas") },
                onReposicoes = { nav.navigate("reposicoes") },
                onUsuarios = { nav.navigate("usuarios") },
                onAvisos = { nav.navigate("avisos") },
                onSair = {
                    vm.sair()
                    nav.navigate("login") { popUpTo("inicio") { inclusive = true } }
                }
            )
        }
        composable("faltas") {
            FaltasTela(vm, voltar, onNova = { nav.navigate("falta/0") }, onEditar = { nav.navigate("falta/$it") })
        }
        composable("falta/{id}", arguments = argId) {
            FaltaFormTela(vm, it.arguments?.getLong("id") ?: 0L, voltar)
        }
        composable("reposicoes") {
            ReposicoesTela(vm, voltar, onNova = { nav.navigate("reposicao/0") }, onEditar = { nav.navigate("reposicao/$it") })
        }
        composable("reposicao/{id}", arguments = argId) {
            ReposicaoFormTela(vm, it.arguments?.getLong("id") ?: 0L, voltar)
        }
        composable("usuarios") {
            UsuariosTela(vm, voltar, onNovo = { nav.navigate("usuario/0") }, onEditar = { nav.navigate("usuario/$it") })
        }
        composable("usuario/{id}", arguments = argId) {
            UsuarioFormTela(vm, it.arguments?.getLong("id") ?: 0L, voltar)
        }
        composable("avisos") {
            AvisosTela(vm, voltar)
        }
    }
}
