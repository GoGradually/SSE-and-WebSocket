package gogradually.sseandwebsocket.domain

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id

@Entity
class MyEntity (@Id val id: Long){
    var count = 0
        private set
    fun addCount(){
        count++
    }
}