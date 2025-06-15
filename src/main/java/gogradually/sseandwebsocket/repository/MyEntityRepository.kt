package gogradually.sseandwebsocket.repository

import gogradually.sseandwebsocket.domain.MyEntity
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query

interface MyEntityRepository: JpaRepository<MyEntity, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from MyEntity e where e.id = :id")
    fun findByIdForUpdate(id: Long): MyEntity?
}