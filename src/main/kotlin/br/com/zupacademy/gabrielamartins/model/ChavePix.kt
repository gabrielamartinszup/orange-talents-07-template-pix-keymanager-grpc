package br.com.zupacademy.gabrielamartins.model

import br.com.zupacademy.gabrielamartins.model.enum.TipoChave
import br.com.zupacademy.gabrielamartins.model.enum.TipoConta
import java.util.*
import javax.persistence.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size


@Entity
class ChavePix(
    @field:NotNull val clienteId: UUID,
    @Enumerated(EnumType.STRING) val tipoChave: TipoChave?,
    @field:Size(max = 77) val chave: String?,
    @field:NotNull @Enumerated(EnumType.STRING) val tipoConta: TipoConta?,
    @field:NotNull @Embedded val conta: Conta
) {

    @Id
    @GeneratedValue
    var id: UUID? = null


    override fun toString(): String {
        return "ChavePix(clienteId=$clienteId, tipoChave=$tipoChave, chave=$chave, tipoConta=$tipoConta, conta=$conta, id=$id)"
    }


}