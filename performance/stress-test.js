import { setupPerformanceTest, runReadOnlyBookingFlow } from './common.js';

export const options = {
    scenarios: {
        stress_test: {
            executor: 'ramping-vus',
            stages: [
                { duration: '30s', target: 5 },
                { duration: '30s', target: 10 },
                { duration: '30s', target: 20 },
                { duration: '30s', target: 30 },
                { duration: '30s', target: 0 },
            ],
        },
    },
    thresholds: {
        http_req_failed: ['rate<0.05'],
        http_req_duration: ['p(95)<1500'],
        checks: ['rate>0.90'],
    },
};

export function setup() {
    return setupPerformanceTest();
}

export default function (data) {
    runReadOnlyBookingFlow(data);
}