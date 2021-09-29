package br.com.zupacademy.gabrielamartins.endpoint


import br.com.zupacademy.gabrielamartins.CadastrarChavePixRequest
import br.com.zupacademy.gabrielamartins.TipoChave.CHAVE_DESCONHECIDA
import br.com.zupacademy.gabrielamartins.TipoConta.DESCONHECIDO
import br.com.zupacademy.gabrielamartins.dto.request.ChavePixRequestDto
import br.com.zupacademy.gabrielamartins.model.enums.TipoChave
import br.com.zupacademy.gabrielamartins.model.enums.TipoConta


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