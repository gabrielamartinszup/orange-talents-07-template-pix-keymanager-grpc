package br.com.zupacademy.gabrielamartins.repository

import br.com.zupacademy.gabrielamartins.model.ChavePix
import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository

@Repository
interface ChavePixRepository : JpaRepository<ChavePix, Long> {

    fun existsByChave(chave: String?): Boolean

}