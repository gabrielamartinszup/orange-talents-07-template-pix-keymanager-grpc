package br.com.zupacademy.gabrielamartins.exception.handler

import br.com.zupacademy.gabrielamartins.exception.custom.ChaveExistenteException
import br.com.zupacademy.gabrielamartins.exception.custom.ChavePixNaoEncontradaException
import io.grpc.Status
import io.grpc.stub.StreamObserver
import io.micronaut.aop.InterceptorBean
import io.micronaut.aop.MethodInterceptor
import io.micronaut.aop.MethodInvocationContext
import io.micronaut.context.MessageSource
import jakarta.inject.Inject
import jakarta.inject.Singleton
import javax.naming.ServiceUnavailableException
import javax.persistence.PersistenceException
import javax.validation.ConstraintViolationException

@Singleton
@InterceptorBean(ErrorHandler::class)
class ErrorHandlerInterceptor() : MethodInterceptor<Any, Any> {

    @Inject
    lateinit var messageSource: MessageSource

    override fun intercept(context: MethodInvocationContext<Any, Any>?): Any? {

        try {
            return context?.proceed()
        } catch (ex: Exception) {
            val responseObserver = context!!.parameterValues[1] as StreamObserver<*>
            val status: Status = fromException(ex)
            responseObserver.onError(status.asRuntimeException())
        }

        return null
    }

    private fun fromException(ex: Exception): Status {
        return when (ex) {

            is ServiceUnavailableException -> status(Status.UNAVAILABLE, ex)
            is IllegalStateException -> status(Status.FAILED_PRECONDITION, ex)
            is ConstraintViolationException -> status(Status.INVALID_ARGUMENT, ex)
            is PersistenceException -> status(Status.INVALID_ARGUMENT, ex)
            is ChaveExistenteException -> status(Status.ALREADY_EXISTS, ex)
            is ChavePixNaoEncontradaException -> status(Status.NOT_FOUND, ex)

            else -> Status.UNKNOWN.withCause(ex).withDescription("Erro inesperado ocorreu")
        }
    }

    private fun status(status: Status, ex: Exception): Status {
        return Status.fromCode(status.code).withCause(ex).withDescription(ex.message)
    }


}