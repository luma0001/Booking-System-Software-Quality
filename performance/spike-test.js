import { setupPerformanceTest, runReadOnlyBookingFlow } from './common.js';

export const options = {
    scenarios: {
        spike_test: {
            executor: 'ramping-vus',
            stages: [
                { duration: '10s', target: 5 },
                { duration: '10s', target: 50 },
                { duration: '30s', target: 50 },
                { duration: '10s', target: 5 },
                { duration: '20s', target: 0 },
            ],
        },
    },
    thresholds: {
        http_req_failed: ['rate<0.10'],
        http_req_duration: ['p(95)<2000'],
        checks: ['rate>0.85'],
    },
};

export function setup() {
    return setupPerformanceTest();
}

export default function (data) {
    runReadOnlyBookingFlow(data);
}