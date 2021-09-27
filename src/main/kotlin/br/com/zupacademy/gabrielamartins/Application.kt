package br.com.zupacademy.gabrielamartins

import io.micronaut.runtime.Micronaut.*
fun main(args: Array<String>) {
	build()
	    .args(*args)
		.packages("br.com.zupacademy.gabrielamartins")
		.start()
}

