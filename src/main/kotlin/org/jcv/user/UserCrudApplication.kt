package org.jcv.user

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class UserCrudApplication

fun main(args: Array<String>) {
	runApplication<UserCrudApplication>(*args)
}
