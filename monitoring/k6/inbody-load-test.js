import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE_URL = __ENV.BASE_URL || 'http://host.docker.internal:8080';

export const options = {
  scenarios: {
    inbody_flow: {
      executor: 'ramping-vus',
      startVUs: 5,
      stages: [
        { duration: '30s', target: 20 },
        { duration: '1m', target: 80 },
        { duration: '1m', target: 120 },
        { duration: '30s', target: 0 }
      ],
      gracefulRampDown: '15s'
    }
  },
  thresholds: {
    http_req_failed: ['rate<0.05'],
    http_req_duration: ['p(95)<1200']
  }
};

function nowMeasuredAt() {
  return new Date().toISOString().slice(0, 19);
}

export function setup() {
  const email = `k6_user_${Date.now()}@test.com`;
  const password = 'Password123!';

  const signupPayload = JSON.stringify({
    email,
    password,
    nickname: 'k6user',
    gender: 'MALE',
    birthDate: '2000-01-01'
  });

  const signupRes = http.post(`${BASE_URL}/api/v1/auth/signup`, signupPayload, {
    headers: { 'Content-Type': 'application/json' }
  });

  check(signupRes, {
    'signup status is 200 or 409': (r) => r.status === 200 || r.status === 409
  });

  const loginPayload = JSON.stringify({ email, password });
  const loginRes = http.post(`${BASE_URL}/api/v1/auth/login`, loginPayload, {
    headers: { 'Content-Type': 'application/json' }
  });

  check(loginRes, {
    'login status is 200': (r) => r.status === 200
  });

  const accessToken = loginRes.json('data.accessToken');
  return { accessToken };
}

export default function (data) {
  const headers = {
    'Content-Type': 'application/json',
    Authorization: `Bearer ${data.accessToken}`
  };

  const inbodyPayload = JSON.stringify({
    measuredAt: nowMeasuredAt(),
    heightCm: 173,
    weightKg: 72.4,
    bodyFatMassKg: 12.5,
    skeletalMuscleMassKg: 33.2,
    bodyWaterL: 46.1,
    waistHipRatio: 0.82,
    visceralFatLevel: 5
  });

  const createRes = http.post(`${BASE_URL}/api/v1/inbody`, inbodyPayload, { headers });
  check(createRes, {
    'create inbody status is 200': (r) => r.status === 200
  });

  const listRes = http.get(`${BASE_URL}/api/v1/inbody/me?page=0&size=5`, { headers });
  check(listRes, {
    'list status is 200': (r) => r.status === 200
  });

  sleep(0.2);
}
