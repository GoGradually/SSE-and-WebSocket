package gogradually.sseandwebsocket.domain

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id

@Entity
class MyEntity (@Id var id: Long? = null) {
    constructor() : this(0L)

    var count = 0
        private set
    fun addCount(){
        count++
    }
}