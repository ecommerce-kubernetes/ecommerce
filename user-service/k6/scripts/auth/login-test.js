import http from 'k6/http';
import { check, sleep } from 'k6';
import { SharedArray } from 'k6/data';
import { htmlReport } from "https://cdn.jsdelivr.net/gh/benc-uk/k6-reporter/dist/bundle.js";
import { textSummary } from "https://jslib.k6.io/k6-summary/0.0.1/index.js";

const users = new SharedArray('users', function () {
    return JSON.parse(open('../../data/users.json'));
});

const maxVus = Number(__ENV.MAX_VUS) || 10;
const rampUp = __ENV.RAMP_UP || '10s';
const hold = __ENV.HOLD || '20s';
const rampDown = __ENV.RAMP_DOWN || '10s';

// 테스트 설정
export const options = {
    stages: [
        { duration: rampUp, target: maxVus }, // 처음 10초 동안 10 명까지 증가
        { duration: hold, target: maxVus }, // 20초 동안 10명 유지
        { duration: rampDown, target: 0 }, 
    ],

    thresholds: {
        http_req_duration: ['p(95)<2000'], //전체 요청중 95% 가 2초 이내로 응답을 요함
        http_req_failed: ['rate<0.01'], //전체 요청중 에러율이 1% 미만을 요함
    }
}

export default function () {
    // 계정 여러개 사용
    const user = users[(__VU - 1) % users.length];

    const payload = JSON.stringify({
        email: user.email,
        password: user.password,
    });

    const params = {
        headers: {
            'Content-Type': 'application/json',
        }
    };
    
    //로그인 post 요청
    const res = http.post(`${__ENV.BASE_URL}/auth/login`, payload, params);

    check(res, {
        "로그인 상태 코드 200": (r) => r.status === 200,
        'JWT 토큰 정상 발급 확인': (r) => r.body.includes('accessToken'),
    });

    //요청 시간 대기
    sleep(1);
}

export function handleSummary(data) {
    return {
        "reports/login-test-report.html": htmlReport(data),
        stdout: textSummary(data, { indent: " ", enableColors: true }),
    };
}