package br.com.zupacademy.gabrielamartins.model

import br.com.zupacademy.gabrielamartins.model.enums.TipoChave
import br.com.zupacademy.gabrielamartins.model.enums.TipoConta
import java.time.LocalDateTime
import java.util.*
import javax.persistence.*
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size


@Entity
class ChavePix(
    @field:NotNull val clienteId: UUID,
    @Enumerated(EnumType.STRING) val tipoChave: TipoChave,
    @field:Size(max = 77) var chave: String,
    @field:NotNull @Enumerated(EnumType.STRING) val tipoConta: TipoConta,
    @field:NotNull @Embedded val conta: Conta
) {

    @Id
    @GeneratedValue
    var id: UUID? = null

    val criadaEm: LocalDateTime = LocalDateTime.now()


    override fun toString(): String {
        return "ChavePix(clienteId=$clienteId, tipoChave=$tipoChave, chave=$chave, tipoConta=$tipoConta, conta=$conta, id=$id)"
    }

    fun pertenceAo(clienteId: UUID) = this.clienteId.equals(clienteId)

    private fun isAleatoria(): Boolean {

        return tipoChave == TipoChave.ALEATORIA
    }

    fun atualiza(chave: String): Boolean {
        if (isAleatoria()) {
            this.chave = chave
            return true
        }
        return false
    }


}