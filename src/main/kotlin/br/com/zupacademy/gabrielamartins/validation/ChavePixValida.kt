package br.com.zupacademy.gabrielamartins.validation

import javax.validation.Constraint
import javax.validation.Payload
import kotlin.reflect.KClass

@MustBeDocumented
@Target(AnnotationTarget.CLASS, AnnotationTarget.TYPE)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [ChavePixValidaValidator::class])
annotation class ChavePixValida(
    val message: String = "Chave Pix com formato inválido", val groups: Array<KClass<Any>> = [],
    val payload: Array<KClass<Payload>> = []
)

