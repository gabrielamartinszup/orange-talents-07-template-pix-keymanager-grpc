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
import br.com.zupacademy.gabrielamartins.service.*
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
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import java.time.LocalDateTime
import java.util.*

@MicronautTest(transactional = false)
internal class CadastraChavePixEndpointTest(
    val repository: ChavePixRepository,
    val grpcClient: KeyManagerCadastraServiceGrpc.KeyManagerCadastraServiceBlockingStub
) {

    @Inject
    lateinit var bcbClient: BcbClient

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
        // cenário
        `when`(itauClient.consulta(clienteId = CLIENTE_ID.toString(), tipo = "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.ok(dadosDaContaResponse()))

        `when`(bcbClient.cadastra(createPixKeyRequest()))
            .thenReturn(HttpResponse.created(createPixKeyResponse()))

        // ação
        val response = grpcClient.cadastrarChavePix(
            CadastrarChavePixRequest.newBuilder()
                .setClienteId(CLIENTE_ID.toString())
                .setTipoChave(TipoChave.EMAIL)
                .setChave("rponte@gmail.com")
                .setTipoConta(TipoConta.CONTA_CORRENTE)
                .build()
        )

        // validação
        with(response) {
            assertEquals(CLIENTE_ID.toString(), clienteId)
            assertNotNull(pixId)
        }
    }

    @Test
    fun naoDeveCadastrarQuandoChaveJaExiste() {
        // cenário
        repository.save(
            chave(
                tipo = br.com.zupacademy.gabrielamartins.model.enums.TipoChave.CPF,
                chave = "63657520325",
                clienteId = CLIENTE_ID
            )
        )

        // ação
        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.cadastrarChavePix(
                CadastrarChavePixRequest.newBuilder()
                    .setClienteId(CLIENTE_ID.toString())
                    .setTipoChave(TipoChave.CPF)
                    .setChave("63657520325")
                    .setTipoConta(TipoConta.CONTA_CORRENTE)
                    .build()
            )
        }

        // validação
        with(thrown) {
            assertEquals(Status.ALREADY_EXISTS.code, status.code)
            assertEquals("Chave Pix '63657520325' já existe no sistema", status.description)
        }
    }

    @Test
    fun naoDeveCadastrarQuandoClienteNaoExistir() {
        // cenário
        `when`(itauClient.consulta(clienteId = CLIENTE_ID.toString(), tipo = "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.notFound())

        // ação
        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.cadastrarChavePix(
                CadastrarChavePixRequest.newBuilder()
                    .setClienteId(CLIENTE_ID.toString())
                    .setTipoChave(TipoChave.EMAIL)
                    .setChave("rponte@gmail.com")
                    .setTipoConta(TipoConta.CONTA_CORRENTE)
                    .build()
            )
        }

        // validação
        with(thrown) {
            assertEquals(Status.FAILED_PRECONDITION.code, status.code)
            assertEquals("Cliente não encontrado", status.description)
        }
    }

    @Test
    fun naoDeveRegistrarQuandoNaoForPossivelCadastrarNoBcb() {
        // cenário
        `when`(itauClient.consulta(clienteId = CLIENTE_ID.toString(), tipo = "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.ok(dadosDaContaResponse()))

        `when`(bcbClient.cadastra(createPixKeyRequest()))
            .thenReturn(HttpResponse.badRequest())

        // ação
        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.cadastrarChavePix(
                CadastrarChavePixRequest.newBuilder()
                    .setClienteId(CLIENTE_ID.toString())
                    .setTipoChave(TipoChave.EMAIL)
                    .setChave("rponte@gmail.com")
                    .setTipoConta(TipoConta.CONTA_CORRENTE)
                    .build()
            )
        }

        // validação
        with(thrown) {
            assertEquals(Status.FAILED_PRECONDITION.code, status.code)
            assertEquals("Erro ao registrar chave Pix no BCB", status.description)
        }
    }

    @Test
    fun naoDeveCadastrarQuandoParametrosForemInvalidos() {
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



    @MockBean(BcbClient::class)
    fun bcbClient(): BcbClient? {
        return Mockito.mock(BcbClient::class.java)
    }

    @MockBean(ItauErpClient::class)
    fun itauClient(): ItauErpClient? {
        return Mockito.mock(ItauErpClient::class.java)
    }

    @Factory
    class Clients {
        @Bean
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): KeyManagerCadastraServiceGrpc.KeyManagerCadastraServiceBlockingStub? {
            return KeyManagerCadastraServiceGrpc.newBlockingStub(channel)
        }
    }

    private fun dadosDaContaResponse(): DadosContaResponse {
        return DadosContaResponse(
            tipo = "CONTA_CORRENTE",
            instituicao = InstituicaoResponse("UNIBANCO ITAU SA", Conta.ITAU_UNIBANCO_ISPB),
            agencia = "1218",
            numero = "291900",
            titular = TitularResponse("Rafael Ponte", "63657520325")
        )
    }

    private fun createPixKeyRequest(): CreatePixRequest {
        return CreatePixRequest(
            keyType = KeyType.EMAIL,
            key = "rponte@gmail.com",
            bankAccount = bankAccount(),
            owner = owner()
        )
    }

    private fun createPixKeyResponse(): CreatePixResponse {
        return CreatePixResponse(
            keyType = KeyType.EMAIL,
            key = "rponte@gmail.com",
            bankAccount = bankAccount(),
            owner = owner(),
            createdAt = LocalDateTime.now()
        )
    }

    private fun bankAccount(): BankAccount {
        return BankAccount(
            participant = Conta.ITAU_UNIBANCO_ISPB,
            branch = "1218",
            accountNumber = "291900",
            accountType = BankAccount.AccountType.CACC
        )
    }

    private fun owner(): Owner {
        return Owner(
            type = Owner.OwnerType.NATURAL_PERSON,
            name = "Rafael Ponte",
            taxIdNumber = "63657520325"
        )
    }

    private fun chave(
        tipo: br.com.zupacademy.gabrielamartins.model.enums.TipoChave,
        chave: String = UUID.randomUUID().toString(),
        clienteId: UUID = UUID.randomUUID(),
    ): ChavePix {
        return ChavePix(
            clienteId = clienteId,
            tipoChave = tipo,
            chave = chave,
            tipoConta = br.com.zupacademy.gabrielamartins.model.enums.TipoConta.CONTA_CORRENTE,
            conta = Conta(
                instituicao = "UNIBANCO ITAU",
                nomeTitular = "Rafael Ponte",
                cpfTitular = "63657520325",
                agencia = "1218",
                numeroConta = "291900"
            )
        )
    }

}
