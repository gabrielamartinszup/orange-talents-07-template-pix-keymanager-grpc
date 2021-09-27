package br.com.zupacademy.gabrielamartins.endpoint

import br.com.zupacademy.gabrielamartins.dto.request.ChavePixRequestDto
import br.com.zupacademy.gabrielamartins.exception.custom.ChaveExistenteException
import br.com.zupacademy.gabrielamartins.exception.custom.ChavePixNaoEncontradaException
import br.com.zupacademy.gabrielamartins.model.ChavePix
import br.com.zupacademy.gabrielamartins.repository.ChavePixRepository
import br.com.zupacademy.gabrielamartins.service.ItauErpClient
import br.com.zupacademy.gabrielamartins.validation.ValidUUID
import io.micronaut.validation.Validated
import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.util.*
import javax.transaction.Transactional
import javax.validation.Valid
import javax.validation.constraints.NotBlank


@Singleton
@Validated
class ChavePixService(@Inject val repository: ChavePixRepository, @Inject val itauErpClient: ItauErpClient) {

    @Transactional
    fun cadastra(@Valid chavePixRequestDto: ChavePixRequestDto): ChavePix {

        // verifica se chave já existe no sistema

        if (repository.existsByChave(chavePixRequestDto.chave))
            throw ChaveExistenteException("Chave Pix '${chavePixRequestDto.chave}' já existe no sistema")

        //busca dados da conta no ERP do Itau

        val response = itauErpClient.consulta(chavePixRequestDto.clienteId, chavePixRequestDto.tipoConta!!.name)
        val conta = response.body()?.converteParaConta() ?: throw IllegalStateException("Cliente não encontrado")


        // salva no banco de dados

        val chave = chavePixRequestDto.converteParaChavePix(conta)
        repository.save(chave)

        return chave
    }


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
    }

}