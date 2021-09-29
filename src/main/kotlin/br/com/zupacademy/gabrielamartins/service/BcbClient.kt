package br.com.zupacademy.gabrielamartins.service

import br.com.zupacademy.gabrielamartins.model.ChavePix
import br.com.zupacademy.gabrielamartins.model.ChavePixInfo
import br.com.zupacademy.gabrielamartins.model.Conta
import br.com.zupacademy.gabrielamartins.model.Instituicoes
import br.com.zupacademy.gabrielamartins.model.enums.TipoChave
import br.com.zupacademy.gabrielamartins.model.enums.TipoConta
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*
import io.micronaut.http.client.annotation.Client
import java.lang.IllegalArgumentException
import java.time.LocalDateTime

@Client("http://localhost:8082/api/v1/pix/keys")
interface BcbClient {

    @Post
    @Produces(MediaType.APPLICATION_XML)
    @Consumes(MediaType.APPLICATION_XML)
    fun cadastra(@Body request: CreatePixRequest): HttpResponse<CreatePixResponse>


    @Delete("/{key}")
    @Produces(MediaType.APPLICATION_XML)
    @Consumes(MediaType.APPLICATION_XML)
    fun delete(@PathVariable key: String, @Body request: DeletePixRequest): HttpResponse<DeletePixResponse>


    @Get("/{key}")
    @Consumes(MediaType.APPLICATION_XML)
    fun findByKey(@PathVariable key: String): HttpResponse<PixKeyDetailsResponse>

}

data class CreatePixRequest(
    val keyType: KeyType,
    val key: String,
    val bankAccount: BankAccount,
    val owner: Owner
) {
    companion object {

        fun of(chave: ChavePix): CreatePixRequest {
            return CreatePixRequest(
                keyType = KeyType.by(chave.tipoChave),
                key = chave.chave,
                bankAccount = BankAccount(
                    participant = Conta.ITAU_UNIBANCO_ISPB,
                    branch = chave.conta.agencia,
                    accountNumber = chave.conta.numeroConta,
                    accountType = BankAccount.AccountType.by(chave.tipoConta)
                ), owner = Owner(
                    type = Owner.OwnerType.NATURAL_PERSON,
                    name = chave.conta.nomeTitular,
                    taxIdNumber = chave.conta.cpfTitular
                )
            )
        }
    }
}


data class CreatePixResponse(
    val keyType: KeyType,
    val key: String,
    val bankAccount: BankAccount,
    val owner: Owner,
    val createdAt: LocalDateTime
)

data class DeletePixRequest(
    val key: String,
    val participant: String = Conta.ITAU_UNIBANCO_ISPB
)

data class DeletePixResponse(
    val key: String,
    val participant: String,
    val deletedAt: LocalDateTime
)

data class PixKeyDetailsResponse(
    val keyType: KeyType,
    val key: String,
    val bankAccount: BankAccount,
    val owner: Owner,
    val createdAt: LocalDateTime
) {
    fun converteParaChavePixInfo(): ChavePixInfo {

        return ChavePixInfo(
            tipoChave = keyType.domainType!!, chave = this.key, tipoConta = when (this.bankAccount.accountType) {
                BankAccount.AccountType.CACC -> TipoConta.CONTA_CORRENTE
                BankAccount.AccountType.SVGS -> TipoConta.CONTA_POUPANCA
            },
            conta = Conta(
                instituicao = Instituicoes.nome(bankAccount.participant),
                nomeTitular = owner.name,
                cpfTitular = owner.taxIdNumber,
                agencia = bankAccount.branch,
                numeroConta = bankAccount.accountNumber
            )
        )

    }
}

enum class KeyType(val domainType: TipoChave?) {

    CPF(TipoChave.CPF),
    CNPJ(null),
    PHONE(TipoChave.TELEFONE),
    EMAIL(TipoChave.EMAIL),
    RANDOM(TipoChave.ALEATORIA);

    companion object {

        private val mapping = KeyType.values().associateBy(KeyType::domainType)

        fun by(domainType: TipoChave): KeyType {
            return mapping[domainType]
                ?: throw IllegalArgumentException("PixKeyType invalid or not found for $domainType")
        }
    }

}

data class BankAccount(
    val participant: String,
    val branch: String,
    val accountNumber: String,
    val accountType: AccountType
) {
    enum class AccountType() {
        CACC,
        SVGS;

        companion object {
            fun by(domainType: TipoConta): AccountType {
                return when (domainType) {
                    TipoConta.CONTA_CORRENTE -> CACC
                    TipoConta.CONTA_POUPANCA -> SVGS
                }
            }
        }
    }

}

data class Owner(val type: OwnerType, val name: String, val taxIdNumber: String) {

    enum class OwnerType {
        NATURAL_PERSON,
        LEGAL_PERSON
    }

}