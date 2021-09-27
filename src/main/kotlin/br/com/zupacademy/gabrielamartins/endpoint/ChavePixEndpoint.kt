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


//    override fun cadastrarChavePix(request: CadastrarChavePixRequest, responseObserver: StreamObserver<CadastrarChavePixResponse>) {
//
//
//        try {
//
//            val chaveRequest = ChavePixRequestDto(
//                clienteId = request.clienteId,
//                tipoChave = request.tipoChave,
//                chave = request.chave,
//                tipoConta = request.tipoConta
//            )
//
//
//            if (repository.existsByChave(request.chave)) {
//                responseObserver.onError(Status.ALREADY_EXISTS
//                    .withDescription("Chave ${chaveRequest.chave} já cadastrada")
//                    .asRuntimeException())
//
//
//                return
//
//
//            }
//
//            val chave = chaveRequest.converteParaChavePix()
//
//            val client = itauClient.consulta(chave.clienteId)
//
//
//            if (client == null) {
//
//                responseObserver.onError(
//                    Status.NOT_FOUND
//                        .withDescription("Cliente com o id ${chave.clienteId} não encontrado")
//                        .asRuntimeException()
//                )
//                return
//
//            }
//
//            repository.save(chave)
//
//            val response = KeyManagerResponse.newBuilder()
//                .setPixId(chave.id!!)
//                .build()
//
//
//
//            responseObserver.onNext(response)
//            responseObserver.onCompleted()
//
//
//        } catch (e: HttpClientException) {
//            responseObserver.onError(
//                Status.UNAVAILABLE
//                    .withDescription("Serviço indisponível")
//                    .asRuntimeException()
//            )
//        } catch (e: Exception) {
//            responseObserver.onError(
//                Status.INTERNAL
//                    .withDescription("Falha ao processar requisição")
//                    .asRuntimeException()
//            )
//        } catch (e: ConstraintViolationException) {
//            responseObserver.onError(
//                Status.INVALID_ARGUMENT
//                    .withDescription(e.message)
//                    .withCause(e.cause)
//                    .asRuntimeException()
//            )
//        }
//
//    }
//
//
//        try {
//
//            val response = itauClient.consulta(request!!.clienteId)
//
//        } catch (e: HttpClientException) {
//
//            throw ServiceUnavailableException("Serviço Indisponível, tente novamente mais tarde!")
//        }
//
//
//        if (request.clienteId.isEmpty()){
//            throw HttpStatusException(HttpStatus.NOT_FOUND, "Cliente com o id ${request.clienteId} não encontrado")
//        }
//

}