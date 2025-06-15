package gogradually.sseandwebsocket.controller

import gogradually.sseandwebsocket.domain.MyEntity
import gogradually.sseandwebsocket.event.CountChangedEvent
import gogradually.sseandwebsocket.repository.MyEntityRepository
import gogradually.sseandwebsocket.service.BrokerService
import gogradually.sseandwebsocket.service.MyEntityService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.time.LocalDateTime

@RestController
class MyEntityController(
    private val myEntityService: MyEntityService,
    private val brokerService: BrokerService
) {
    @PostMapping("/add")
    fun addEntity(@RequestBody id: Long): MyEntity {
        return myEntityService.enroll(id)
    }

    @GetMapping("/{id}/sse")
    fun getForSse(@PathVariable id: Long): SseEmitter? {
        val sseEmitter = SseEmitter(105_000L)
        brokerService.addSubscribe(id, sseEmitter)
        val find = myEntityService.find(id).let { myEntity -> myEntity?.count }
        val let = find?.let { CountChangedEvent(id, it, LocalDateTime.now()) }
        if (let != null) {
            sseEmitter.send(let)
        }
        else
            return null
        return sseEmitter
    }

    @PostMapping("/{id}")
    fun update(@PathVariable id: Long) {
        myEntityService.addCount(id)
    }
}