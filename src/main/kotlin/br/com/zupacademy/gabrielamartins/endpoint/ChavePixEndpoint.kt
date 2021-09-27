package br.com.zupacademy.gabrielamartins.endpoint


import br.com.zupacademy.gabrielamartins.*

import br.com.zupacademy.gabrielamartins.exception.handler.ErrorHandler
import io.grpc.stub.StreamObserver

import jakarta.inject.Inject
import jakarta.inject.Singleton
import javax.transaction.Transactional


@Singleton
@ErrorHandler
class ChavePixEndpoint(
    @Inject val chavePixService: ChavePixService
) : KeyManagerServiceGrpc.KeyManagerServiceImplBase() {


    override fun cadastrarChavePix(
        request: CadastrarChavePixRequest,
        responseObserver: StreamObserver<CadastrarChavePixResponse>
    ) {

        val novaChave = request.converteParaChavePixRequestDto()


        val chaveCriada = chavePixService.cadastra(novaChave)




        responseObserver.onNext(
            CadastrarChavePixResponse.newBuilder()
                .setPixId(chaveCriada.id.toString())
                .setClienteId(chaveCriada.clienteId.toString())
                .build()
        )

        responseObserver.onCompleted()
    }


    override fun removerChavePix(
        request: RemoverChavePixRequest,
        responseObserver: StreamObserver<RemoverChavePixResponse>
    ) {
        chavePixService.remove(clienteId = request.clienteId, pixId = request.pixId)

        responseObserver.onNext(RemoverChavePixResponse.newBuilder()
            .setClienteId(request.clienteId)
            .setPixId(request.pixId)
            .build())

        responseObserver.onCompleted()
    }


}