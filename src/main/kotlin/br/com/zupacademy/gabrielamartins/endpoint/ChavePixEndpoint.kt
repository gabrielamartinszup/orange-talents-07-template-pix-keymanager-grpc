package br.com.zupacademy.gabrielamartins.endpoint


import br.com.zupacademy.gabrielamartins.CadastrarChavePixRequest
import br.com.zupacademy.gabrielamartins.CadastrarChavePixResponse
import br.com.zupacademy.gabrielamartins.KeyManagerServiceGrpc

import br.com.zupacademy.gabrielamartins.exception.handler.ErrorHandler
import io.grpc.stub.StreamObserver

import jakarta.inject.Inject
import jakarta.inject.Singleton


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
                .setPixId(chaveCriada.id!!)
                .build()
        )

        responseObserver.onCompleted()
    }


}