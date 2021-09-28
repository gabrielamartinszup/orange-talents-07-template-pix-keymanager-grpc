package br.com.zupacademy.gabrielamartins.endpoint.create


import br.com.zupacademy.gabrielamartins.*
import br.com.zupacademy.gabrielamartins.endpoint.converteParaChavePixRequestDto

import br.com.zupacademy.gabrielamartins.exception.handler.ErrorHandler
import io.grpc.stub.StreamObserver

import jakarta.inject.Inject
import jakarta.inject.Singleton


@Singleton
@ErrorHandler
class CadastraChavePixEndpoint(
    @Inject val chavePixService: CadastraChavePixService
) : KeyManagerCadastraServiceGrpc.KeyManagerCadastraServiceImplBase() {


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





}