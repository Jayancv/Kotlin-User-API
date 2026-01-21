package org.jcv.user.config

import org.jcv.user.entity.User
import org.jcv.user.repository.UserRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class DataInitializer {

    @Bean
    fun initDatabase(userRepository: UserRepository): CommandLineRunner {
        return CommandLineRunner {
            if (userRepository.count() == 0L) {
                val users = listOf(
                    User(
                        firstName = "John",
                        lastName = "Doe",
                        email = "john.doe@example.com",
                        phone = "123-456-7890",
                        address = "123 Main St"
                    ),
                    User(
                        firstName = "Jane",
                        lastName = "Smith",
                        email = "jane.smith@example.com",
                        phone = "987-654-3210",
                        address = "456 Oak Ave"
                    ),
                    User(
                        firstName = "Bob",
                        lastName = "Johnson",
                        email = "bob.johnson@example.com",
                        phone = "555-123-4567",
                        address = "789 Pine Rd",
                        active = false
                    )
                )

                userRepository.saveAll(users)
                println("Sample users added to the database")
            }
        }
    }
}