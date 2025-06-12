package gogradually.sseandwebsocket.controller

import gogradually.sseandwebsocket.controller.dto.Greeting
import gogradually.sseandwebsocket.controller.dto.HelloMessage
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.stereotype.Controller
import org.springframework.web.util.HtmlUtils

/**
 * ## TCP/웹소켓/STOMP
 * - TCP: 가장 기본적인 전송 계층 프로토콜
 * - 웹소켓: TCP를 로우레벨로 사용하는 응용 계층 프로토콜 - TCP위에 이진/텍스트 프레임으로 WebSocket 프레임 정의
 * - STOMP: 웹소켓 위에서 동작하는 메시징 방식을 정의한 프로토콜 - 웹소켓의 메시지 패턴 표준화
 *
 * ## 웹소켓이란?
 * - 애플리케이션 계층 프로토콜
 * - TCP를 바로 쓸 수 없는 브라우저를 위한 특별한 양방향 통신 방식 - HTTP를 통해 인증단계를 한 단계 거쳐, 양쪽 모두 안전한 통신 보장
 * - HTTP 업그레이드를 통해 연결을 시작한 뒤 지속적인 TCP 연결 유지
 * - 바이너리 또는 텍스트 프레임 단위로 자유로운 메시지 교환 가능 (이진 포맷도 지원하지만, 이진 포맷 특성상 직접 필요한 메소드들을 정의해야 한다.)
 * - 주로 STOMP의 명확한 명령 세트(CONNECT/DISCONNECT, 구독/구독취소, ACK/NACK, SEND)를 이용해 간단하게 사용한다.
 *
 * ## STOMP란?
 * - WebSocket(또는 TCP) 위에서 동작하는 애플리케이션 계층의 **메시징 프로토콜**이다.
 * - 메시징 방식에 최적화 되어있다. (비교대상: 요청-응답(HTTP), 보안(SSL/TLS), 파일전송(FTP))
 * - 발행·구독(Pub/Sub), ACK/NACK, 트랜잭션 등 메시지 패턴을 표준화했다.
 * - 텍스트 기반으로 동작한다.
 * - TCP에서도 사용 가능하지만, TCP연결의 보안 안정성이 보장되는 상황에서는 이진 포맷의 유용성이 훨신 효율적이기 때문에, AMQP, MQTT, Protobuf(gRPC)와 같은 이진 포맷 메시징 프로토콜을 사용한다.
 */
@Controller("/hello") // 무시됨. MessageMapping은 RequestMapping과 완전히 별개로 동작.
class STOMPController {


    /**
     * - MessageMapping: 메시지가 `/hello`에 전송되면, greeting() 메소드가 호출되도록 보장한다. 메시지를 매핑하는 역할을 수행한다. HTTP의 RequestMapping과 완전히 별개로 동작한다.
     *
     * - SendTo: 응답 메시지를 `/topic/greetings`를 구독하는 모든 구독자에게 브로드캐스트한다.
     *
     * Thread.sleep(1000)이 걸려있는데, 이는 클라이언트가 메시지를 보내놓고, 다른 작업을 수행하는 것이 가능하다는 것을 보여주기 위함이다.
     * 웹소켓이 요청-응답 모델이 아닌 양방향 통신 모델이기 때문에, 서버에서 원할 때 데이터를 푸쉬하는게 가능하다.
     * 여기서는 @MessageMapping과 @SendTo가 동시에 있기 때문에, 실제로는 양방향 푸쉬 모델이지만, 마치 요청/응답 모델처럼 동작한다.
     */
    @MessageMapping("/hello")
    @SendTo("/topic/greetings")
    fun greeting(message: HelloMessage) : Greeting {
        Thread.sleep(1000)
        return Greeting("Hello, ${HtmlUtils.htmlEscape(message.name)}!")
    }
}