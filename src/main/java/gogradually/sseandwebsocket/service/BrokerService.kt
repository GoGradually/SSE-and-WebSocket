package gogradually.sseandwebsocket.service

import gogradually.sseandwebsocket.event.CountChangedEvent
import org.springframework.stereotype.Service
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

@Service
class BrokerService {
    val sseMap: MutableMap<Long, MutableList<SseEmitter>> = ConcurrentHashMap()

    fun addSubscribe(id: Long, sseEmitter : SseEmitter){
        val subscribers = sseMap.computeIfAbsent(id, ) { CopyOnWriteArrayList() }
        subscribers.add(sseEmitter)
    }

    fun spreadEvent(changedEvent: CountChangedEvent) {
        val subscribers = sseMap.computeIfAbsent(changedEvent.id) { CopyOnWriteArrayList() }
        subscribers
            .map { sseEmitter -> sseEmitter.send(changedEvent) }
    }
}