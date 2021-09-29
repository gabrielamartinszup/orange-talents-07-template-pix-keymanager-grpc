package br.com.zupacademy.gabrielamartins.endpoint.create

import br.com.zupacademy.gabrielamartins.dto.request.ChavePixRequestDto
import br.com.zupacademy.gabrielamartins.exception.custom.ChaveExistenteException
import br.com.zupacademy.gabrielamartins.model.ChavePix
import br.com.zupacademy.gabrielamartins.repository.ChavePixRepository
import br.com.zupacademy.gabrielamartins.service.BcbClient
import br.com.zupacademy.gabrielamartins.service.CreatePixRequest
import br.com.zupacademy.gabrielamartins.service.ItauErpClient
import io.micronaut.http.HttpStatus
import io.micronaut.validation.Validated
import jakarta.inject.Inject
import jakarta.inject.Singleton
import javax.transaction.Transactional
import javax.validation.Valid


@Singleton
@Validated
class CadastraChavePixService(@Inject val repository: ChavePixRepository, @Inject val itauErpClient: ItauErpClient, @Inject val bancoCentralClient: BcbClient) {

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

        // registra chave no BCB

        val bcbRequest = CreatePixRequest.of(chave)

        val bcbResponse = bancoCentralClient.cadastra(bcbRequest)
        if(bcbResponse.status != HttpStatus.CREATED){
            throw IllegalStateException("Erro ao registrar chave Pix no BCB")
        }

        //atualiza chave do domínio com a chave gerada pelo BCB

        chave.atualiza(bcbResponse.body()!!.key)

        return chave
    }


//
}