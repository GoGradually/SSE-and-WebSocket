package gogradually.sseandwebsocket.controller

import gogradually.sseandwebsocket.domain.MyEntity
import gogradually.sseandwebsocket.repository.MyEntityRepository
import gogradually.sseandwebsocket.service.BrokerService
import gogradually.sseandwebsocket.service.MyEntityService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

@RestController
class MyEntityController(
    private val myEntityRepository: MyEntityRepository,
    private val myEntityService: MyEntityService,
    private val brokerService: BrokerService
) {
    @PostMapping("/add")
    fun addEntity(id: Long): MyEntity {
        return myEntityRepository.save(MyEntity(id))
    }

    @GetMapping("/{id}/sse")
    fun getForSse(@PathVariable id: Long): SseEmitter {
        val sseEmitter = SseEmitter()
        brokerService.addSubscribe(id, sseEmitter)
        return sseEmitter
    }

    @PostMapping("/{id}")
    fun update(@PathVariable id: Long) {
        myEntityService.addCount(id)
    }
}