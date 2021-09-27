package br.com.zupacademy.gabrielamartins.validation

import br.com.zupacademy.gabrielamartins.dto.request.ChavePixRequestDto
import io.micronaut.core.annotation.AnnotationValue
import io.micronaut.validation.validator.constraints.ConstraintValidator
import io.micronaut.validation.validator.constraints.ConstraintValidatorContext
import jakarta.inject.Singleton

@Singleton
class ChavePixValidaValidator : ConstraintValidator<ChavePixValida, ChavePixRequestDto> {


    override fun isValid(
        value: ChavePixRequestDto?,
        annotationMetadata: AnnotationValue<ChavePixValida>,
        context: ConstraintValidatorContext
    ): Boolean {
        if (value?.tipoChave == null) {
            return true
        }

        return value.tipoChave.valida(value.chave)
    }
}