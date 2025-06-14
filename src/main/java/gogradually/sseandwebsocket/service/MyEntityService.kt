package gogradually.sseandwebsocket.service

import gogradually.sseandwebsocket.event.CountChangedEvent
import gogradually.sseandwebsocket.repository.MyEntityRepository
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class MyEntityService(
    private val myEntityRepository: MyEntityRepository,
    private val applicationEventPublisher: ApplicationEventPublisher
) {
    @Transactional
    fun addCount(id: Long) {
        val myEntity = myEntityRepository.findByIdForUpdate(id)
        myEntity?.addCount()
        myEntity?.count?.let { applicationEventPublisher.publishEvent(CountChangedEvent(id, it, LocalDateTime.now())) }
    }

}