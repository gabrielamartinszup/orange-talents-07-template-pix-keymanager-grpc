package br.com.zupacademy.gabrielamartins.dto.response

import br.com.zupacademy.gabrielamartins.model.Conta
import io.micronaut.core.annotation.Introspected

@Introspected
data class DadosContaResponse(


    val instituicao: InstituicaoResponse,
    val agencia: String,
    val numero: String,
    val titular: TitularResponse,
    val tipo: String

) {

    fun converteParaConta(): Conta {

        return Conta(
            instituicao = this.instituicao.nome,
            nomeTitular = this.titular.nome,
            cpfTitular = this.titular.cpf,
            agencia = this.agencia,
            numeroConta = this.numero
        )


    }


}