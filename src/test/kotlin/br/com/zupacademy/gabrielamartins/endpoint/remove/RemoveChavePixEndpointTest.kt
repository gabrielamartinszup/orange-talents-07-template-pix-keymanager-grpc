package br.com.zupacademy.gabrielamartins.endpoint.remove

import br.com.zupacademy.gabrielamartins.KeyManagerRemoveServiceGrpc
import br.com.zupacademy.gabrielamartins.RemoverChavePixRequest
import br.com.zupacademy.gabrielamartins.model.ChavePix
import br.com.zupacademy.gabrielamartins.model.Conta
import br.com.zupacademy.gabrielamartins.model.enums.TipoChave
import br.com.zupacademy.gabrielamartins.repository.ChavePixRepository
import br.com.zupacademy.gabrielamartins.service.BcbClient
import br.com.zupacademy.gabrielamartins.service.DeletePixRequest
import br.com.zupacademy.gabrielamartins.service.DeletePixResponse
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.Assert.assertEquals
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import java.time.LocalDateTime
import java.util.*

@MicronautTest(transactional = false)
internal class RemoveChavePixEndpointTest(
    val repository: ChavePixRepository,
    val grpcClient: KeyManagerRemoveServiceGrpc.KeyManagerRemoveServiceBlockingStub
) {

    //1. happy path - deve remover a chave pix existente
    //2. nao deve remover quando a chave nao existir
    //3. nao deve remover quando chave existe mas não pertence ao cliente


    @Inject
    lateinit var bcbClient: BcbClient

    lateinit var CHAVE_EXISTENTE: ChavePix

    @BeforeEach
    fun setUp() {
        CHAVE_EXISTENTE = repository.save(
            chave(
                tipoChave = TipoChave.EMAIL,
                chave = "rponte@gmail.com",
                clienteId = UUID.randomUUID()
            )
        )
    }

    @AfterEach
    fun tearDown() {
        repository.deleteAll()
    }


    @Test
    fun deveRemoverChavePix() {

        //cenário

        `when`(bcbClient.delete("rponte@gmail.com", DeletePixRequest("rponte@gmail.com")))
            .thenReturn(HttpResponse.ok(DeletePixResponse(key = "rponte@gmail.com", participant = Conta.ITAU_UNIBANCO_ISPB, deletedAt = LocalDateTime.now())))

        //acao

        val response = grpcClient.removerChavePix(
            RemoverChavePixRequest.newBuilder()
                .setPixId(CHAVE_EXISTENTE.id.toString())
                .setClienteId(CHAVE_EXISTENTE.clienteId.toString())
                .build()
        )


        //validacao

        assertEquals(CHAVE_EXISTENTE.id.toString(), response.pixId)
        assertEquals(CHAVE_EXISTENTE.clienteId.toString(), response.clienteId)
    }

    @Test
    fun naoDeveRemoverChavePixQuandoAChaveNaoExistir() {

        //cenario

        val chavePixInexistente = UUID.randomUUID().toString()

        //ação

        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.removerChavePix(
                RemoverChavePixRequest.newBuilder()
                    .setClienteId(CHAVE_EXISTENTE.clienteId.toString())
                    .setPixId(chavePixInexistente)
                    .build()
            )
        }

        //validação

        with(thrown) {

            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Chave Pix não foi encontrada ou não pertence ao cliente", status.description)
        }

    }

    @Test
    fun naoDeveRemoverChavePixQuandoChaveExisteMasPertenceAOutroCliente() {

        //cenário

        val outroClienteId = UUID.randomUUID().toString()

        //ação

        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.removerChavePix(
                RemoverChavePixRequest.newBuilder()
                    .setPixId(CHAVE_EXISTENTE.id.toString())
                    .setClienteId(outroClienteId)
                    .build()
            )
        }

        //validação

        with(thrown) {

            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Chave Pix não foi encontrada ou não pertence ao cliente", status.description)
        }

    }

    @Test
    fun naoDeveRemoverQuandoOcorrerAlgumErroNoServicoDoBcb(){

        //cenário

        `when`(bcbClient.delete("rponte@gmail.com", DeletePixRequest("rponte@gmail.com", participant = Conta.ITAU_UNIBANCO_ISPB)))
            .thenReturn(HttpResponse.unprocessableEntity())

        //ação

        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.removerChavePix(RemoverChavePixRequest.newBuilder()
                .setPixId(CHAVE_EXISTENTE.id.toString())
                .setClienteId(CHAVE_EXISTENTE.clienteId.toString())
                .build())
        }

        //validação

        with(thrown){
            assertEquals(Status.FAILED_PRECONDITION.code, status.code)
            assertEquals("Erro ao remover chave Pix do BCB", status.description)
        }
    }

    @MockBean(BcbClient::class)
    fun bcbClient(): BcbClient? {
        return mock(BcbClient::class.java)
    }

    @Factory
    class Clients {
        @Bean
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): KeyManagerRemoveServiceGrpc.KeyManagerRemoveServiceBlockingStub {
            return KeyManagerRemoveServiceGrpc.newBlockingStub(channel)
        }


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
}