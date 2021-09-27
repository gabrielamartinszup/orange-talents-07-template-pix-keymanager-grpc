package br.com.zupacademy.gabrielamartins.model

import javax.persistence.Embeddable
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

@Embeddable
class Conta(
    @field:NotBlank val instituicao: String,
    @field:NotBlank val nomeTitular: String,
    @field:NotBlank @field:Size(max = 11) val cpfTitular: String,
    @field:NotBlank @field:Size(max = 4) val agencia: String,
    @field:NotBlank @field:Size(max = 6) val numeroConta: String
) {

}
