package br.com.zupacademy.gabrielamartins.model

import br.com.zupacademy.gabrielamartins.model.enum.TipoChave
import br.com.zupacademy.gabrielamartins.model.enum.TipoConta
import javax.persistence.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size


@Entity
class ChavePix(
    @field:NotBlank val clienteId: String,
    @Enumerated(EnumType.STRING) val tipoChave: TipoChave?,
    @field:Size(max = 77) val chave: String?,
    @field:NotNull @Enumerated(EnumType.STRING) val tipoConta: TipoConta?,
    @field:NotNull @Embedded val conta: Conta
) {

    @Id
    @GeneratedValue
    var id: Long? = null
}