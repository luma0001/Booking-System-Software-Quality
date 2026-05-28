import { setupPerformanceTest, runReadOnlyBookingFlow } from './common.js';

export const options = {
    scenarios: {
        load_test: {
            executor: 'ramping-vus',
            stages: [
                { duration: '30s', target: 5 },
                { duration: '1m', target: 5 },
                { duration: '30s', target: 0 },
            ],
        },
    },
    thresholds: {
        http_req_failed: ['rate<0.01'],
        http_req_duration: ['p(95)<1000'],
        checks: ['rate>0.95'],
    },
};

export function setup() {
    return setupPerformanceTest();
}

export default function (data) {
    runReadOnlyBookingFlow(data);
}