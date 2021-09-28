package br.com.zupacademy.gabrielamartins.endpoint.remove

import br.com.zupacademy.gabrielamartins.KeyManagerRemoveServiceGrpc
import br.com.zupacademy.gabrielamartins.RemoverChavePixRequest
import br.com.zupacademy.gabrielamartins.RemoverChavePixResponse
import br.com.zupacademy.gabrielamartins.exception.handler.ErrorHandler
import io.grpc.stub.StreamObserver
import jakarta.inject.Inject
import jakarta.inject.Singleton

@ErrorHandler
@Singleton
class RemoveChavePixEndpoint(@Inject private val service: RemoveChavePixService) : KeyManagerRemoveServiceGrpc.KeyManagerRemoveServiceImplBase(){

    override fun removerChavePix(
        request: RemoverChavePixRequest,
        responseObserver: StreamObserver<RemoverChavePixResponse>
    ) {
        service.remove(clienteId = request.clienteId, pixId = request.pixId)

        responseObserver.onNext(RemoverChavePixResponse.newBuilder()
            .setClienteId(request.clienteId)
            .setPixId(request.pixId)
            .build())

        responseObserver.onCompleted()
    }
}