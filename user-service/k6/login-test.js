import http from 'k6/http';

// 테스트 설정
export const options = {
    stages: [
        { duration: '10s', target: 10 }, // 처음 10초 동안 10 명까지 증가
        { duration: '20s', target: 10 }, // 20초 동안 10명 유지
        { duration: '10s', target: 0 }, 
    ],

    thresholds: {
        http_req_duration: ['p(95)<2000'], //전체 요청중 95% 가 2초 이내로 응답을 요함
        http_req_failed: ['rate<0.01'], //전체 요청중 에러율이 1% 미만을 요함
    }
}

export default function () {
    const baseUrl = __ENV.BASE_URL
    const url = `${baseUrl}/login`
    const payload = JSON.stringify({
        email: "user@naver.com",
        password: "user1234*"
    });

    const params = {
        headers: {
            'Content-Type': 'application/json',
        }
    };
    
    //로그인 post 요청
    const res = http.post(url, payload, params);

    check(res, {
        "로그인 상태 코드 200": (r) => r.status === 200,

        'JWT 토큰 정상 발급 확인': (r) => {
            const hasTokenInBody = r.body.includes('accessToken');
        }
    });

    //요청 시간 대기
    sleep(1);
}