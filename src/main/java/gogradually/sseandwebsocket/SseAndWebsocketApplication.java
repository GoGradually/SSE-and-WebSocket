package gogradually.sseandwebsocket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class SseAndWebsocketApplication {

    public static void main(String[] args) {
        SpringApplication.run(SseAndWebsocketApplication.class, args);
    }

}
