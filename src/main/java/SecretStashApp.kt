package com.secretstash

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SecretStashApp

fun main(args: Array<String>) {
    runApplication<SecretStashApp>(*args)
}