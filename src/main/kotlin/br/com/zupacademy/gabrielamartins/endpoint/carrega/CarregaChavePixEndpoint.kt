package br.com.zupacademy.gabrielamartins.endpoint.carrega

import br.com.zupacademy.gabrielamartins.CarregarChavePixRequest
import br.com.zupacademy.gabrielamartins.CarregarChavePixResponse
import br.com.zupacademy.gabrielamartins.KeyManagerCarregaServiceGrpc
import br.com.zupacademy.gabrielamartins.endpoint.toModel
import br.com.zupacademy.gabrielamartins.exception.handler.ErrorHandler
import br.com.zupacademy.gabrielamartins.repository.ChavePixRepository
import br.com.zupacademy.gabrielamartins.service.BcbClient
import io.grpc.stub.StreamObserver
import jakarta.inject.Inject
import jakarta.inject.Singleton
import javax.validation.Validator

@ErrorHandler
@Singleton
class CarregaChavePixEndpoint(
    @Inject val repository: ChavePixRepository,
    @Inject val bcbClient: BcbClient,
    @Inject val validator: Validator
) : KeyManagerCarregaServiceGrpc.KeyManagerCarregaServiceImplBase() {

    override fun carregarChavePix(
        request: CarregarChavePixRequest,
        responseObserver: StreamObserver<CarregarChavePixResponse>
    ) {

        val filtro = request.toModel(validator)
        val chaveInfo = filtro.filtra(repository = repository, bcbClient = bcbClient)

        responseObserver.onNext(CarregaChavePixResponseConverter().convert(chaveInfo))
        responseObserver.onCompleted()
    }
}