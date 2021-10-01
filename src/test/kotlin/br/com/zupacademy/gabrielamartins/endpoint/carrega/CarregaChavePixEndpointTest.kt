package br.com.zupacademy.gabrielamartins.endpoint.carrega

import br.com.zupacademy.gabrielamartins.CarregarChavePixRequest
import br.com.zupacademy.gabrielamartins.KeyManagerCarregaServiceGrpc
import br.com.zupacademy.gabrielamartins.model.ChavePix
import br.com.zupacademy.gabrielamartins.model.Conta
import br.com.zupacademy.gabrielamartins.model.enums.TipoChave
import br.com.zupacademy.gabrielamartins.model.enums.TipoConta
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
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import java.time.LocalDateTime
import java.util.*

@MicronautTest(transactional = false)
internal class CarregaChavePixEndpointTest(
    val repository: ChavePixRepository,
    val grpcClient: KeyManagerCarregaServiceGrpc.KeyManagerCarregaServiceBlockingStub
) {


    @Inject
    lateinit var bcbClient: BcbClient

    companion object {
        val CLIENTE_ID = UUID.randomUUID()
    }

    /**
     * TIP: por padrão roda numa transação isolada
     */
    @BeforeEach
    fun setup() {
        repository.save(chave(tipo = TipoChave.EMAIL, chave = "rafael.ponte@zup.com.br", clienteId = CLIENTE_ID))
        repository.save(chave(tipo = TipoChave.CPF, chave = "63657520325", clienteId = UUID.randomUUID()))
        repository.save(chave(tipo = TipoChave.ALEATORIA, chave = "randomkey-3", clienteId = CLIENTE_ID))
        repository.save(chave(tipo = TipoChave.TELEFONE, chave = "+551155554321", clienteId = CLIENTE_ID))
    }

    /**
     * TIP: por padrão roda numa transação isolada
     */
    @AfterEach
    fun cleanUp() {
        repository.deleteAll()
    }

    @Test
    fun `deve carregar chave por pixId e clienteId`() {
        // cenário
        val chaveExistente = repository.findByChave("+551155554321").get()

        // ação
        val response = grpcClient.carregarChavePix(
            CarregarChavePixRequest.newBuilder()
                .setPixId(
                    CarregarChavePixRequest.FiltroPorPixId.newBuilder()
                        .setPixId(chaveExistente.id.toString())
                        .setClienteId(chaveExistente.clienteId.toString())
                        .build()
                ).build()
        )

        // validação
        with(response) {
            assertEquals(chaveExistente.id.toString(), this.pixId)
            assertEquals(chaveExistente.clienteId.toString(), this.clienteId)
            assertEquals(chaveExistente.tipoChave.name, this.chave.tipoChave.name)
            assertEquals(chaveExistente.chave, this.chave.chave)
        }
    }

    @Test
    fun `nao deve carregar chave por pixId e clienteId quando filtro invalido`() {
        // ação
        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.carregarChavePix(
                CarregarChavePixRequest.newBuilder()
                    .setPixId(
                        CarregarChavePixRequest.FiltroPorPixId.newBuilder()
                            .setPixId("")
                            .setClienteId("")
                            .build()
                    ).build()
            )
        }

        // validação
        with(thrown) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals(status.description, status.description)

        }
    }

    @Test
    fun `nao deve carregar chave por pixId e clienteId quando registro nao existir`() {
        // ação
        val pixIdNaoExistente = UUID.randomUUID().toString()
        val clienteIdNaoExistente = UUID.randomUUID().toString()
        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.carregarChavePix(
                CarregarChavePixRequest.newBuilder()
                    .setPixId(
                        CarregarChavePixRequest.FiltroPorPixId.newBuilder()
                            .setPixId(pixIdNaoExistente)
                            .setClienteId(clienteIdNaoExistente)
                            .build()
                    ).build()
            )
        }

        // validação
        with(thrown) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Chave Pix não encontrada", status.description)
        }
    }

    @Test
    fun `deve carregar chave por valor da chave quando registro existir localmente`() {
        // cenário
        val chaveExistente = repository.findByChave("rafael.ponte@zup.com.br").get()

        // ação
        val response = grpcClient.carregarChavePix(
            CarregarChavePixRequest.newBuilder()
                .setChave("rafael.ponte@zup.com.br")
                .build()
        )

        // validação
        with(response) {
            assertEquals(chaveExistente.id.toString(), this.pixId)
            assertEquals(chaveExistente.clienteId.toString(), this.clienteId)
            assertEquals(chaveExistente.tipoChave.name, this.chave.tipoChave.name)
            assertEquals(chaveExistente.chave, this.chave.chave)
        }
    }

    @Test
    fun `deve carregar chave por valor da chave quando registro nao existir localmente mas existir no BCB`() {
        // cenário
        val bcbResponse = pixKeyDetailsResponse()
        `when`(bcbClient.findByKey(key = "user.from.another.bank@santander.com.br"))
            .thenReturn(HttpResponse.ok(pixKeyDetailsResponse()))

        // ação
        val response = grpcClient.carregarChavePix(
            CarregarChavePixRequest.newBuilder()
                .setChave("user.from.another.bank@santander.com.br")
                .build()
        )

        // validação
        with(response) {
            assertEquals("", this.pixId)
            assertEquals("", this.clienteId)
            assertEquals(bcbResponse.keyType.name, this.chave.tipoChave.name)
            assertEquals(bcbResponse.key, this.chave.chave)
        }
    }

    @Test
    fun `nao deve carregar chave por valor da chave quando registro nao existir localmente nem no BCB`() {
        // cenário
        `when`(bcbClient.findByKey(key = "not.existing.user@santander.com.br"))
            .thenReturn(HttpResponse.notFound())

        // ação
        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.carregarChavePix(
                CarregarChavePixRequest.newBuilder()
                    .setChave("not.existing.user@santander.com.br")
                    .build()
            )
        }

        // validação
        with(thrown) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Chave Pix não encontrada", status.description)
        }
    }

    @Test
    fun `nao deve carregar chave por valor da chave quando filtro invalido`() {
        // ação
        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.carregarChavePix(CarregarChavePixRequest.newBuilder().setChave("").build())
        }

        // validação
        with(thrown) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("chave: não deve estar em branco", status.description)

        }
    }

//    @Test
//    fun `nao deve carregar chave quando filtro invalido`() {
//
//        // ação
//        val thrown = assertThrows<StatusRuntimeException> {
//            grpcClient.carregarChavePix(CarregarChavePixRequest.newBuilder().build())
//        }
//
//        // validação
//        with(thrown) {
//            Assertions.assertEquals(Status.INVALID_ARGUMENT.code, status.code)
//            Assertions.assertEquals("Chave Pix inválida ou não informada", status.description)
//        }
//    }


    @MockBean(BcbClient::class)
    fun bcbClient(): BcbClient? {
        return mock(BcbClient::class.java)
    }

    @Factory
    class Clients {
        @Bean
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): KeyManagerCarregaServiceGrpc.KeyManagerCarregaServiceBlockingStub? {
            return KeyManagerCarregaServiceGrpc.newBlockingStub(channel)
        }
    }

    private fun chave(
        tipo: TipoChave,
        chave: String = UUID.randomUUID().toString(),
        clienteId: UUID = UUID.randomUUID(),
    ): ChavePix {
        return ChavePix(
            clienteId = clienteId,
            tipoChave = tipo,
            chave = chave,
            tipoConta = TipoConta.CONTA_CORRENTE,
            conta = Conta(
                instituicao = "UNIBANCO ITAU",
                nomeTitular = "Rafael Ponte",
                cpfTitular = "12345678900",
                agencia = "1218",
                numeroConta = "123456"
            )
        )
    }

    private fun pixKeyDetailsResponse(): PixKeyDetailsResponse {
        return PixKeyDetailsResponse(
            keyType = KeyType.EMAIL,
            key = "user.from.another.bank@santander.com.br",
            bankAccount = bankAccount(),
            owner = owner(),
            createdAt = LocalDateTime.now()
        )
    }

    private fun bankAccount(): BankAccount {
        return BankAccount(
            participant = "90400888",
            branch = "9871",
            accountNumber = "987654",
            accountType = BankAccount.AccountType.SVGS
        )
    }

    private fun owner(): Owner {
        return Owner(
            type = Owner.OwnerType.NATURAL_PERSON,
            name = "Another User",
            taxIdNumber = "12345678901"
        )
    }


}