package gogradually.sseandwebsocket.config

import org.springframework.context.annotation.Configuration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer

@Configuration
@EnableWebSocketMessageBroker
class WebSocketConfig: WebSocketMessageBrokerConfigurer {
    /**
     * 메시지 브로커를 설정하는 메소드
     */
    override fun configureMessageBroker(registry: MessageBrokerRegistry) {
        /**
         * enableSimpleBroker - 메모리 기반의 단순 메시지 브로커.
         * `/topic` 접두사로 온 클라이언트의 요청은 클라이언트의 구독 요청으로 보고, 메시지 브로커에게 전달하도록 등록한다.
         * 이는 @SendTo와 결합되어 사용되는데, @SendTo에서 ("/topic/greeting")으로 설정된 경우, 해당 리턴값은 메시지 브로커에게 전달되고,
         * 메시지 브로커는 해당 응답을 구독자들에게 전달하게 된다.
         */
        registry.enableSimpleBroker("/topic")
        /**
         * setApplicationDestinationPrefixes - @MessageMapping 어노테이션이 매핑된 메소드로 바인딩될 메시지에 대해, `/app` 접두사를 지정한다.
         * 이는 MessageMapping에 강제로 /app을 붙이는 효과로, 컨트롤러의 매핑에 "/app"을 붙이는 것과 같은 효과를 지닌다.
         */
        registry.setApplicationDestinationPrefixes("/app")
    }

    /**
     * 클라이언트가 WebSocket 연결을 맺기 위해 호출할 엔드포인트 URL을 /gs-guide-websocket으로 등록한다.
     * 보통 SockJS 폴백(fallback)을 함께 설정하여, WebSocket을 지원하지 않는 환경에서도 폴링 방식으로 통신할 수 있게 만든다.
     */
    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        registry.addEndpoint("/gs-guide-websocket")
    }
}
