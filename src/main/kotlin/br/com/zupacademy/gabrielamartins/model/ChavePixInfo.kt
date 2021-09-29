package br.com.zupacademy.gabrielamartins.model

import br.com.zupacademy.gabrielamartins.model.enums.TipoChave
import br.com.zupacademy.gabrielamartins.model.enums.TipoConta
import java.time.LocalDateTime
import java.util.*

class ChavePixInfo(
    val pixId: UUID? = null,
    val clienteId: UUID? = null,
    val tipoChave: TipoChave,
    val chave: String,
    val tipoConta: TipoConta,
    val conta: Conta,
    val registradaEm: LocalDateTime = LocalDateTime.now()
){

    companion object{

        fun of(chave: ChavePix):ChavePixInfo{
            return ChavePixInfo(
                pixId = chave.id,
                clienteId = chave.clienteId,
                tipoChave = chave.tipoChave,
                chave = chave.chave,
                tipoConta = chave.tipoConta,
                conta = chave.conta,
                registradaEm = chave.criadaEm
            )
        }
    }
}

