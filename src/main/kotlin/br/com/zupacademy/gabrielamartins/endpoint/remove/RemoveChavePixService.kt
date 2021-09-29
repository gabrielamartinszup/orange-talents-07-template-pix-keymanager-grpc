package br.com.zupacademy.gabrielamartins.endpoint.remove

import br.com.zupacademy.gabrielamartins.exception.custom.ChavePixNaoEncontradaException
import br.com.zupacademy.gabrielamartins.repository.ChavePixRepository
import br.com.zupacademy.gabrielamartins.service.BcbClient
import br.com.zupacademy.gabrielamartins.service.DeletePixRequest
import br.com.zupacademy.gabrielamartins.service.ItauErpClient
import br.com.zupacademy.gabrielamartins.validation.ValidUUID
import io.micronaut.http.HttpStatus
import io.micronaut.validation.Validated
import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.util.*
import javax.transaction.Transactional
import javax.validation.constraints.NotBlank

@Singleton
@Validated
class RemoveChavePixService(
    @Inject val repository: ChavePixRepository,
    @Inject val itauErpClient: ItauErpClient,
    @Inject val bancoCentralClient: BcbClient
) {

    @Transactional
    fun remove(
        @NotBlank @ValidUUID("ClienteID com formato inválido") clienteId: String,
        @NotBlank @ValidUUID("PixID com formato inválido") pixId: String
    ) {

        val uuidPixId = UUID.fromString(pixId)
        val uuidClienteId = UUID.fromString(clienteId)


        val chave = repository.findByIdAndClienteId(uuidPixId, uuidClienteId)
            .orElseThrow { ChavePixNaoEncontradaException("Chave Pix não foi encontrada ou não pertence ao cliente") }

        repository.deleteById(uuidPixId)

        val bcbrequest = DeletePixRequest(chave.chave)

        val bcbresponse = bancoCentralClient.delete(key = chave.chave, request = bcbrequest)
        if (bcbresponse.status != HttpStatus.OK) {
            throw IllegalStateException("Erro ao remover chave Pix do BCB")
        }
    }

}