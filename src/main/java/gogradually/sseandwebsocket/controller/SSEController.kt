package gogradually.sseandwebsocket.controller

import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import reactor.core.publisher.Flux
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.Executors

/**
 * # 커넥션 연결 과정
 * - 일반적인 HTTP 메시지와 유사하나, Accept:text/event-stream 헤더를 포함한 Get 요청을 보낸다.
 * (즉, 클라이언트가 해당 API가 SSE임을 알아야 한다.)
 * (Sse의 연결 주체는 클라이언트이다.)
 *
 * # 커넥션 유지 확인
 * - 서버는 새로운 이벤트가 있을 때마다, 아래와 같은 형태로 데이터를 전송한다.
 * ```
 * event: <이벤트타입>\n
 * data: <이벤트데이터>\n
 * \n
 * ```
 * - 빈 줄 두번으로 이벤트의 경계를 구분한다.
 * - 서버가 명시적인 커넥션 종료 요청을 보내지 않는 한, HTTP 연결은 계속 유지된다.
 *
 * # 재연결과 타임아웃
 * - 네트워크 오류나 서버 측 연결 끊김 시 클라이언트는 자동으로 재연결을 시도한다.
 * - 클라이언트는 retry 필드를 통해, 클라이언트가 재연결을 무한정 대기하지 않도록 재연결 대기시간을 지정할수도 있다.
 * - 서버는 keep-alive\n\n 을 보내 연결 유지를 돕기도 한다.
 *
 * # 커넥션 종료 과정
 * 커넥션의 종료 과정은 "클라이언트가 중단하는 경우"와 "서버가 중단하는 경우"로 나눌 수 있다.
 * ## 클라이언트가 중단하는 경우
 * - 단순히 TCP 연결이 끊긴 것이기 때문에, 서버측에 `IOException: Broken pipe`가 발생하고, 서버는 이를 유의미하게 처리하지 않는다.
 * ## 서버가 중단하는 경우
 * - 서버가 `sseEmitter` 스트림에 명시적으로 `sseEmitter.complete()`두었을 경우, 해당 메소드에 도달하는 즉시 SSE의 연결이 종료된다.
 * - 이벤트를 발행하던 스레드에서 해당 emitter를 complete() 하면, 그 emitter를 observing 하고 있던 ResponseBodyEmitter가 complete 작업을 수행한다.
 * ```java
 * 		@Override
 * 		public void complete() {
 * 			try {
 * 				this.outputMessage.flush();
 * 				this.deferredResult.setResult(null);
 * 			}
 * 			catch (IOException ex) {
 * 				this.deferredResult.setErrorResult(ex);
 * 			}
 * 		}
 * ```
 * ```java
 * 		try {
 * 			interceptorChain.applyPreProcess(this.asyncWebRequest, deferredResult);
 * 			deferredResult.setResultHandler(result -> {
 * 				result = interceptorChain.applyPostProcess(this.asyncWebRequest, deferredResult, result);
 * 				setConcurrentResultAndDispatch(result);
 * 			});
 * 		}
 * ```
 * - 혹은 complete() 를 명시적으로 호출하지 않았을 경우, 스프링에 설정된 자동 타임아웃 옵션으로 인해 아무 이벤트도 발행하지 않고 10~30초정도가 지나면 자동으로 커넥션이 종료도니다.
 *
 * # 커넥션이 많아졌을 때 관리
 * - 서블릿 3.0 비동기를 이용하기 때문에, 이벤트 루프 방식으로 효율적으로 동작한다.
 * - 하지만 작업이 지나치게 많아지면 좋을건 없으니, 명시적으로 complete()를 수행하자.
 *
 * > 프록시 사용 시 헤더 누락을 주의하자.
 *
 * # 일괄 전송 가능 여부 - 구독 시스템
 * - 클라이언트가 특정 API를 호출한다 - 구독 흉내내기
 * - 서버가 값이 변경됐을 때, 관련된 SseEmitter에게 전부 이벤트를 발행한다. - 발행 흉내내기
 * - 서버가 스케일아웃 될 가능성은? -> 메시지 브로커 활용, WAS도 해당 토픽을 구독하도록
 *   -> 구현을 위한 중간 계층이 발생한다. WebSocket은 이를 BrokerRelay 기능으로 간단하게 사용 가능하다.
 */
@RestController
class SSEController {
    /**
     * WebFlux 방식 SSE
     * (현재 라이브러리가 Web과 Reactive Web이 동시에 사용되고 있으므로, 기본적으로 Netty가 아닌 Tomcat 스레드 모델에서 서블릿 3.0 비동기 처리 모델을 사용함)
     * - WAS-nio 소켓 채널 간에 리액터 모델 사용
     * - 클라이언트 - nio 소켓 채널 간 비동기 이벤트 루프 모델 사용 (톰캣 기본 방식, Webflux와 관련 X)
     */
    @GetMapping("/reactive/sse-flux",produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun streamFlux(): Flux<String>{
        return Flux.interval(Duration.ofSeconds(1))
            .map { sequence -> "Flux - " + LocalDateTime.now().toString() }
            .take(10)
    }

    /**
     * SseEventBuilder 는 그 자체로 이벤트 객체 역할을 수행한다.
     * 실제로 build는 sseEmitter 내부에서 이루어지고, 해당 build가 완로되면 DataWithMediaType 객체가 리턴된다.
     * 불필요한 추상화를 없애고, Builder를 이벤트 객체처럼 사용할 수 있도록 만든 설계이다.
     *
     * `SseEmitter`는 내부적으로 `DeferredResult`와 유사한 개념을 사용하며,
     * 해당 SseEmitter를 반환함으로써 비동기 방식을 효율적으로 관리한다.
     *
     * 실제로는 `ResponseBodyEmitterReturnValueHandler` 를 사용해서, 비동기 스트림 형태로 지속적으로 반환을 수행한다.
     * 각 이벤트를 전송할 때마다, `HttpServletResponse.getWriter().write(...)` + `flush()` 를 통해 응답을 웹서버 레벨에서 직접 반환하도록 만든다.
     */
    @GetMapping("/mvc/sse-emitter")
    fun streamEmitter(): SseEmitter{
        val sseEmitter = SseEmitter()
        val sseMvcExecutor = Executors.newSingleThreadExecutor()
        sseMvcExecutor.execute {
            try{
                for(i in 1..10){
                    val event = SseEmitter.event()
                        .data("SSE MVC - " + LocalDateTime.now().toString())
                        .id(i.toString())
                        .name("sse event - mvc")
                    sseEmitter.send(event)
                    Thread.sleep(1000)
                }
                sseEmitter.complete()
            }catch(e: Exception){
                sseEmitter.completeWithError(e)
            }
        }
        return sseEmitter
    }
}
