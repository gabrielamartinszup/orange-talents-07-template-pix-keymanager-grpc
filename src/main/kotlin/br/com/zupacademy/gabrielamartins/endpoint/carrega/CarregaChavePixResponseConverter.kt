package br.com.zupacademy.gabrielamartins.endpoint.carrega

import br.com.zupacademy.gabrielamartins.CarregarChavePixResponse
import br.com.zupacademy.gabrielamartins.TipoChave
import br.com.zupacademy.gabrielamartins.TipoConta
import br.com.zupacademy.gabrielamartins.model.ChavePixInfo
import com.google.protobuf.Timestamp
import java.time.ZoneId

class CarregaChavePixResponseConverter {

    fun convert(chaveInfo: ChavePixInfo): CarregarChavePixResponse {
        return CarregarChavePixResponse.newBuilder()
            .setClienteId(chaveInfo.clienteId?.toString() ?: "") // Protobuf usa "" como default value para String
            .setPixId(chaveInfo.pixId?.toString() ?: "") // Protobuf usa "" como default value para String
            .setChave(
                CarregarChavePixResponse.ChavePix // 1
                    .newBuilder()
                    .setTipoChave(TipoChave.valueOf(chaveInfo.tipoChave.name)) // 2
                    .setChave(chaveInfo.chave)
                    .setConta(
                        CarregarChavePixResponse.ChavePix.ContaInfo.newBuilder() // 1
                            .setTipoConta(TipoConta.valueOf(chaveInfo.tipoConta.name)) // 2
                            .setInstituicao(chaveInfo.conta.instituicao) // 1 (Conta)
                            .setNomeTitular(chaveInfo.conta.nomeTitular)
                            .setCpfTitular(chaveInfo.conta.cpfTitular)
                            .setAgencia(chaveInfo.conta.agencia)
                            .setNumeroConta(chaveInfo.conta.numeroConta)
                            .build()
                    )
                    .setCriadaEm(chaveInfo.registradaEm.let {
                        val createdAt = it.atZone(ZoneId.of("UTC")).toInstant()
                        Timestamp.newBuilder()
                            .setSeconds(createdAt.epochSecond)
                            .setNanos(createdAt.nano)
                            .build()
                    })
            )
            .build()
    }

}
