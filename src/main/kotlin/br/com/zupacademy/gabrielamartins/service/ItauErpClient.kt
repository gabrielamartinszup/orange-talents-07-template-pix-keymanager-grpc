package br.com.zupacademy.gabrielamartins.service

import br.com.zupacademy.gabrielamartins.dto.response.DadosContaResponse
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client


@Client("http://localhost:9091/api/v1/clientes")
interface ItauErpClient {

    @Get("/{clienteId}/contas{?tipo}")
    fun consulta(@PathVariable clienteId: String, @QueryValue tipo: String): HttpResponse<DadosContaResponse>
}