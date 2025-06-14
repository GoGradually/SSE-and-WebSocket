package gogradually.sseandwebsocket.event

import java.time.LocalDateTime

data class CountChangedEvent(val id: Long, val count: Int, val timestamp: LocalDateTime)
