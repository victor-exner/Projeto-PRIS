package br.edu.ifsp.ifsigard.ui.tema

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val VerdeIF = Color(0xFF2E7D32)
private val VerdeIFClaro = Color(0xFF81C784)

private val Claro = lightColorScheme(
    primary = VerdeIF,
    secondary = Color(0xFF00695C),
    tertiary = Color(0xFFC62828)
)

private val Escuro = darkColorScheme(
    primary = VerdeIFClaro,
    secondary = Color(0xFF80CBC4),
    tertiary = Color(0xFFEF9A9A)
)

@Composable
fun TemaIfsigard(conteudo: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) Escuro else Claro,
        content = conteudo
    )
}
