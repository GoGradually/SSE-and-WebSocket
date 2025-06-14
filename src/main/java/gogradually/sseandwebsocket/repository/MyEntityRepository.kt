package gogradually.sseandwebsocket.repository

import gogradually.sseandwebsocket.domain.MyEntity
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock

interface MyEntityRepository: JpaRepository<MyEntity, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    fun findByIdForUpdate(id: Long): MyEntity?
}