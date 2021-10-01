package br.com.zupacademy.gabrielamartins.endpoint


import br.com.zupacademy.gabrielamartins.CadastrarChavePixRequest
import br.com.zupacademy.gabrielamartins.CarregarChavePixRequest
import br.com.zupacademy.gabrielamartins.TipoChave.CHAVE_DESCONHECIDA
import br.com.zupacademy.gabrielamartins.TipoConta.DESCONHECIDO
import br.com.zupacademy.gabrielamartins.dto.request.ChavePixRequestDto
import br.com.zupacademy.gabrielamartins.model.Filtro
import br.com.zupacademy.gabrielamartins.model.enums.TipoChave
import br.com.zupacademy.gabrielamartins.model.enums.TipoConta
import javax.validation.ConstraintViolationException
import javax.validation.Validator


fun CadastrarChavePixRequest.converteParaChavePixRequestDto(): ChavePixRequestDto {


    return ChavePixRequestDto(
        clienteId = clienteId,
        tipoChave = when (tipoChave) {
            CHAVE_DESCONHECIDA -> null
            else -> TipoChave.valueOf(tipoChave.name)
        },
        chave = chave,
        tipoConta = when (tipoConta) {
            DESCONHECIDO -> null
            else -> TipoConta.valueOf(tipoConta.name)

        }
    )
}

fun CarregarChavePixRequest.toModel(validator: Validator): Filtro {

    val filtro = when (filtroCase!!) {
        CarregarChavePixRequest.FiltroCase.PIXID -> pixId.let {
            Filtro.PorPixId(clienteId = it.clienteId, pixId = it.pixId)
        }
        CarregarChavePixRequest.FiltroCase.CHAVE -> Filtro.PorChave(chave)
        CarregarChavePixRequest.FiltroCase.FILTRO_NOT_SET -> Filtro.Invalido()
    }

    val violations = validator.validate(filtro)
    if (violations.isNotEmpty()) {
        throw ConstraintViolationException(violations)
    }

    return filtro

}