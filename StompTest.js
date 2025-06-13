import ws from "k6/ws";
import { Trend } from "k6/metrics";
import { sleep } from "k6";

// Metrics
const stompLatency = new Trend("stomp_latency_ms");

// Test phases durations (ms)
const WARMUP_DURATION = 30 * 1000;
const MAIN_DURATION   = 60 * 1000;
const TOTAL_DURATION_MS = WARMUP_DURATION + MAIN_DURATION + (15 * 1000);

export const options = {
    vus: 1,                  // 단일 조회용 VU
    duration: `${TOTAL_DURATION_MS}ms`, // 105초
};

// STOMP 프레임 파싱
function parseFrame(frame) {
    const [headerAndCmd, bodyWithNull] = frame.split("\n\n");
    const lines = headerAndCmd.split("\n");
    const command = lines.shift();
    const headers = {};
    lines.forEach(line => {
        const idx = line.indexOf(":");
        if (idx > -1) {
            const key = line.slice(0, idx).trim();
            const val = line.slice(idx + 1).trim();
            headers[key] = val;
        }
    });
    return { command, headers };
}

export default function () {
    const scriptStart = Date.now();
    const url = __ENV.STOMP_URL || "ws://your-broker-host:port/websocket";
    const res = ws.connect(url, {}, socket => {
        socket.on('open', () => {
            // STOMP CONNECT
            socket.send(
                'CONNECT\n' +
                'accept-version:1.2\n' +
                'host:your-host\n' +
                '\n' +
                '\u0000'
            );
            // STOMP SUBSCRIBE
            socket.send(
                'SUBSCRIBE\n' +
                'id:sub-0\n' +
                'destination:/topic/your-topic\n' +
                '\n' +
                '\u0000'
            );
        });

        socket.on('message', data => {
            const frame = parseFrame(data);
            const ts = frame.headers['timestamp'];
            if (ts) {
                const now     = Date.now();
                const elapsed = now - scriptStart;
                // 워밍업(30s) 후 본 부하(60s) 구간에만 기록
                if (elapsed >= WARMUP_DURATION && elapsed <= WARMUP_DURATION + MAIN_DURATION) {
                    const publishedAt = parseInt(ts, 10);
                    const latency     = now - publishedAt;
                    stompLatency.add(latency);
                }
            }
        });

        // 총 테스트 기간 후 연결 종료
        socket.setTimeout(() => socket.close(), TOTAL_DURATION_MS);
    });
    // VU가 종료되지 않도록 유지
    sleep(TOTAL_DURATION_MS / 1000);
}
