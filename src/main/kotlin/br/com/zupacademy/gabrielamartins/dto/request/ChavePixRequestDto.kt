package br.com.zupacademy.gabrielamartins.dto.request


import br.com.zupacademy.gabrielamartins.model.ChavePix
import br.com.zupacademy.gabrielamartins.model.Conta
import br.com.zupacademy.gabrielamartins.model.enums.TipoChave

import br.com.zupacademy.gabrielamartins.model.enums.TipoConta
import br.com.zupacademy.gabrielamartins.validation.ChavePixValida
import br.com.zupacademy.gabrielamartins.validation.ValidUUID
import io.micronaut.core.annotation.Introspected
import java.util.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@ChavePixValida
@Introspected
data class ChavePixRequestDto(
    @field: NotBlank @field: ValidUUID val clienteId: String,
    @field:NotNull val tipoChave: TipoChave?,
    @field:Size(max = 77) val chave: String,
    @field:NotNull val tipoConta: TipoConta?
)
{


    fun converteParaChavePix(conta: Conta): ChavePix {


        return ChavePix(
            clienteId = UUID.fromString(this.clienteId),
            tipoChave = TipoChave.valueOf(this.tipoChave!!.name),
            chave = if (this.tipoChave == TipoChave.ALEATORIA) UUID.randomUUID().toString() else this.chave,
            tipoConta = TipoConta.valueOf(this.tipoConta!!.name),
            conta = conta
        )
    }


}
