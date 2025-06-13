import { Trend } from "k6/metrics";
import { sleep } from "k6";
import { EventSource } from "k6/experimental/eventsource";

// Subscriber 측 이벤트 수신 지연 기록용 메트릭
const sseLatency = new Trend("sse_latency_ms");

// Warm-up과 Cool-down 구간 제외한 본 부하 구간 설정(ms)
const WARMUP_DURATION = 30 * 1000;
const MAIN_DURATION   = 60 * 1000;

export const options = {
    vus: 1,            // 단일 조회용 VU
    duration: "105s"  // 워밍업(30s) + 본 부하(60s) + 쿨다운(15s)
};

export default function () {
    const scriptStart = Date.now();
    const url = __ENV.SSE_URL || "http://your-server/stream";
    const es = new EventSource(url);

    es.on('message', e => {
        try {
            const payload = JSON.parse(e.data);
            if (payload.timestamp) {
                const now     = Date.now();
                const elapsed = now - scriptStart;
                // 본 부하 구간(30s~90s) 동안만 메트릭에 기록
                if (elapsed >= WARMUP_DURATION && elapsed <= WARMUP_DURATION + MAIN_DURATION) {
                    const latency = now - parseInt(payload.timestamp, 10);
                    sseLatency.add(latency);
                }
            }
        } catch (_) {
            // 무시
        }
    });

    es.on('error', () => {
        es.close();
    });

    // 전체 테스트 기간 동안 구독 유지
    sleep(105);
    es.close();
}
