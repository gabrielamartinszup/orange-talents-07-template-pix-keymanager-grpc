package br.com.zupacademy.gabrielamartins.dto.response

import io.micronaut.core.annotation.Introspected

@Introspected
data class InstituicaoResponse(
    val nome: String,
    val ispb: String
) {

}
