SSE와 웹소켓을 비교하며, 현재 [SuperBoard 프로젝트](https://github.com/GoGradually/SuperBoard)에 적용할 방식을 고민하기 위한 프로젝트

# SSE

text/event-stream으로 단방향 스트리밍

브라우저가 자동 재연결·Last-Event-ID 관리 내장

SSE 프록시나 로드밸런서에서 SSE 스트리밍을 긴 폴링으로 오인해 커넥션 타임아웃 위험

텍스트 UTF-8 전용 단방향 스트리밍. 단방향만 필요하다면 유용함.

Transfer-Encoding: chunked 로 전송하기 때문에, 프록시에서 최적화를 위해 proxy buffering을 적용할 수도 있음

### 스케일 아웃을 가정하면?

직접 이벤트를 애플리케이션 코드로 작성 & 메시지 브로커와 클라이언트 간의 발행/구독 정보를 연결하기 위한 중간 계층을 구현해야 함.

# WebSocket
초기 HTTP 업그레이드 핸드셰이크 후 TCP 소켓으로 전환

양방향(full-duplex) 통신 지원

바이너리 프레임 전송 가능, 서브프로토콜(STOMP, MQTT 등)도 얹기 쉬움

### 스케일 아웃을 가정하면?

STOMP 사용 시 **BrokerRelay 기능을 통한 빠른 구독 토픽 전환 & 이벤트 발행 가능**

**STOMP의 브로커 릴레이를 켜면, "어떤 세션이 어떤 토픽을 구독했는가"라는 정보를 WAS에 저장할 필요가 없음**

-> 웹소켓의 sticky session 문제도 해결할 수 있나?

그대신 여전히 노드가 바뀌면 클라이언트랑 다시 연결해야 하니, 웹소켓의 연결 지속성 문제는 지켜봐야 할 것 같은데

직접 해보면서, 웹 소켓을 좀 더 제대로 이해해보자.
- [ ] 로드 밸런서와 웹소켓 문제 이해
- [ ] sticky session