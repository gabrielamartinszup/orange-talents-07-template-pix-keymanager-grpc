package br.com.zupacademy.gabrielamartins.endpoint.cadastra

import br.com.zupacademy.gabrielamartins.CadastrarChavePixRequest
import br.com.zupacademy.gabrielamartins.KeyManagerCadastraServiceGrpc

import br.com.zupacademy.gabrielamartins.TipoChave
import br.com.zupacademy.gabrielamartins.TipoConta
import br.com.zupacademy.gabrielamartins.dto.response.DadosContaResponse
import br.com.zupacademy.gabrielamartins.dto.response.InstituicaoResponse
import br.com.zupacademy.gabrielamartins.dto.response.TitularResponse
import br.com.zupacademy.gabrielamartins.model.ChavePix
import br.com.zupacademy.gabrielamartins.model.Conta
import br.com.zupacademy.gabrielamartins.repository.ChavePixRepository
import br.com.zupacademy.gabrielamartins.service.ItauErpClient
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import java.util.*

@MicronautTest(transactional = false)
internal class CadastraChavePixEndpointTest(
    val repository: ChavePixRepository,
    val grpcClient: KeyManagerCadastraServiceGrpc.KeyManagerCadastraServiceBlockingStub
) {

    @Inject
    lateinit var itauClient: ItauErpClient

    companion object {
        val CLIENTE_ID = UUID.randomUUID()
    }

    @BeforeEach
    fun setup() {
        repository.deleteAll()
    }

    //1. happy path
    //2. nao cadastrar quando chave já existe
    //3. nao registrar quando cliente não existe
    //4. nao registrar quando parâmetros forem inválidos


    @Test
    fun deveRegistrarNovaChavePix() {

        //cenário

        `when`(itauClient.consulta(clienteId = CLIENTE_ID.toString(), tipo = "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.ok(dadosDaContaResponse()))

        //ação

        val response = grpcClient.cadastrarChavePix(
            CadastrarChavePixRequest.newBuilder()
                .setClienteId(CLIENTE_ID.toString())
                .setTipoChave(TipoChave.EMAIL)
                .setChave("rponte@gamil.com")
                .setTipoConta(TipoConta.CONTA_CORRENTE)
                .build()
        )

        //validacao

        with(response) {
            assertNotNull(pixId)
        }

    }

    @Test
    fun naoDeveCadastrarQuandoChaveJaExiste() {

        //cenario

        repository.save(
            chave(
                tipoChave = br.com.zupacademy.gabrielamartins.model.enums.TipoChave.CPF,
                chave = "63657520325",
                clienteId = CLIENTE_ID
            )
        )

        //acao

        val thrown = assertThrows<StatusRuntimeException>{
            grpcClient.cadastrarChavePix(CadastrarChavePixRequest.newBuilder()
                .setClienteId(CLIENTE_ID.toString())
                .setTipoChave(TipoChave.CPF)
                .setChave("63657520325")
                .setTipoConta(TipoConta.CONTA_CORRENTE)
                .build())
        }

        //validacao

        with(thrown){
            assertEquals(Status.ALREADY_EXISTS.code, status.code)
            assertEquals("Chave Pix '63657520325' já existe no sistema", status.description)
        }
    }

    @Test
    fun naoDeveCadastrarQuandoClienteNaoExistir(){

        //cenario
        `when`(itauClient.consulta(clienteId = CLIENTE_ID.toString(), tipo = "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.notFound())

        //ação

        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.cadastrarChavePix(CadastrarChavePixRequest.newBuilder()
                .setClienteId(CLIENTE_ID.toString())
                .setTipoChave(TipoChave.EMAIL)
                .setChave("rponte@gmail.com")
                .setTipoConta(TipoConta.CONTA_CORRENTE)
                .build())
        }

        //validação

        with(thrown){
            assertEquals(Status.FAILED_PRECONDITION.code, status.code)
            assertEquals("Cliente não encontrado", status.description)
        }
    }


    @Test
    fun naoDeveCadastrarQuandoParametrosForemInvalidos(){

        //ação

        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.cadastrarChavePix(CadastrarChavePixRequest.newBuilder().build())
        }

        //validação

        with(thrown){
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals(thrown.status.description, status.description)
        }
    }

    @MockBean(ItauErpClient::class)
    fun itauClient(): ItauErpClient {
        return Mockito.mock(ItauErpClient::class.java)
    }

    private fun dadosDaContaResponse(): DadosContaResponse {
        return DadosContaResponse(
            instituicao = InstituicaoResponse("UNIBANCO ITAU SA", "1"), agencia = "1281", numero = "291900",
            titular = TitularResponse("Rafael Ponte", "63657520325"), tipo = "CONTA_CORRENTE"
        )
    }

    private fun chave(
        tipoChave: br.com.zupacademy.gabrielamartins.model.enums.TipoChave,
        chave: String = UUID.randomUUID().toString(),
        clienteId: UUID = UUID.randomUUID()
    ): ChavePix {
        return ChavePix(
            clienteId = clienteId,
            tipoChave = tipoChave,
            chave = chave,
            tipoConta = br.com.zupacademy.gabrielamartins.model.enums.TipoConta.CONTA_CORRENTE,
            conta = Conta(
                instituicao = "UNIBANCO_ITAU",
                nomeTitular = "Rafael Ponte",
                cpfTitular = "63657520325",
                agencia = "1218",
                numeroConta = "291900"
            )
        )
    }

    @Factory
    class Clients {
        @Bean
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): KeyManagerCadastraServiceGrpc.KeyManagerCadastraServiceBlockingStub {
            return KeyManagerCadastraServiceGrpc.newBlockingStub(channel)
        }


    }
}
