package br.edu.ifsp.ifsigard.util

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.ResolverStyle

/** Conversão e validação de datas e horários digitados nos formulários. */
object Formatos {
    private val dataBr: DateTimeFormatter =
        DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT)
    private val hora: DateTimeFormatter =
        DateTimeFormatter.ofPattern("HH:mm").withResolverStyle(ResolverStyle.STRICT)
    private val dataHora: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")

    /** "25/11/2026" -> LocalDate, ou null se a data for inválida. */
    fun lerData(texto: String): LocalDate? =
        try {
            LocalDate.parse(texto.trim(), dataBr)
        } catch (e: DateTimeParseException) {
            null
        }

    /** "14:30" -> LocalTime, ou null se o horário for inválido. */
    fun lerHora(texto: String): LocalTime? =
        try {
            LocalTime.parse(texto.trim(), hora)
        } catch (e: DateTimeParseException) {
            null
        }

    fun isoParaBr(iso: String): String =
        try {
            LocalDate.parse(iso).format(dataBr)
        } catch (e: DateTimeParseException) {
            iso
        }

    fun momento(millis: Long): String =
        Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).format(dataHora)
}
