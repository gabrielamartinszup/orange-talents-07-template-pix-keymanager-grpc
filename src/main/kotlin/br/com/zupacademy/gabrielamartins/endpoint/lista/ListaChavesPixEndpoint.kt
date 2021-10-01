package br.com.zupacademy.gabrielamartins.endpoint.lista

import br.com.zupacademy.gabrielamartins.*
import br.com.zupacademy.gabrielamartins.exception.handler.ErrorHandler
import br.com.zupacademy.gabrielamartins.repository.ChavePixRepository
import com.google.protobuf.Timestamp
import io.grpc.stub.StreamObserver
import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.time.ZoneId
import java.util.*

@ErrorHandler
@Singleton
class ListaChavesPixEndpoint(@Inject private val repository: ChavePixRepository) :
    KeyManagerListaServiceGrpc.KeyManagerListaServiceImplBase() {

    override fun listarChavesPix(
        request: ListarChavesPixRequest,
        responseObserver: StreamObserver<ListarChavesPixResponse>
    ) {

        if (request.clienteId.isNullOrBlank()) 
            throw IllegalArgumentException("Cliente ID não pode ser nulo ou vazio")

        val clienteId = UUID.fromString(request.clienteId)
        val chaves = repository.findAllByClienteId(clienteId).map {
            ListarChavesPixResponse.ChavePix.newBuilder()
                .setPixId(it.id.toString())
                .setTipoChave(TipoChave.valueOf(it.tipoChave.name))
                .setChave(it.chave)
                .setTipoConta(TipoConta.valueOf(it.tipoConta.name))
                .setCriadaEm(it.criadaEm.let {
                    val createdAt = it.atZone(ZoneId.of("UTC")).toInstant()
                    Timestamp.newBuilder()
                        .setSeconds(createdAt.epochSecond)
                        .setNanos(createdAt.nano)
                        .build()
                })
                .build()
        }

        responseObserver.onNext(
            ListarChavesPixResponse.newBuilder()
                .setClienteId(clienteId.toString())
                .addAllChaves(chaves)
                .build()
        )
        responseObserver.onCompleted()
    }


}