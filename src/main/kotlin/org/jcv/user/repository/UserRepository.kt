package org.jcv.user.repository


import org.jcv.user.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface UserRepository : JpaRepository<User, Long> {

    fun findByEmail(email: String): Optional<User>;
    fun findByActive(active: Boolean): List<User>;
    fun existsByEmail(email: String): Boolean;
    fun existsByEmailAndIdNot(email: String, id: Long): Boolean
}