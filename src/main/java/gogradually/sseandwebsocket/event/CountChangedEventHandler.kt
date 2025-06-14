package gogradually.sseandwebsocket.event

import gogradually.sseandwebsocket.service.BrokerService
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class CountChangedEventHandler(private val brokerService: BrokerService) {
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun on(event: CountChangedEvent){
        brokerService.spreadEvent(event)
    }
}