import http from 'k6/http';
import { check, group, sleep, fail } from 'k6';

export const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

function tomorrowAsIsoDate() {
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    return tomorrow.toISOString().split('T')[0];
}

export function setupPerformanceTest() {
    const response = http.get(`${BASE_URL}/api/activities`);

    if (response.status !== 200) {
        fail(`Setup failed: GET /api/activities returned ${response.status}`);
    }

    const activities = response.json();

    if (!Array.isArray(activities) || activities.length === 0) {
        fail('Setup failed: GET /api/activities returned no activities');
    }

    return {
        activityId: activities[0].id,
        date: tomorrowAsIsoDate(),
    };
}

export function runReadOnlyBookingFlow(data) {
    group('GET /api/activities', () => {
        const response = http.get(`${BASE_URL}/api/activities`);

        check(response, {
            'activities status is 200': (res) => res.status === 200,
            'activities response is JSON array': (res) => Array.isArray(res.json()),
            'activities response time below 1000ms': (res) => res.timings.duration < 1000,
        });
    });

    group('GET /api/activities/{activityId}/slots', () => {
        const response = http.get(
            `${BASE_URL}/api/activities/${data.activityId}/slots?date=${data.date}`
        );

        check(response, {
            'slots status is 200': (res) => res.status === 200,
            'slots response has activityId': (res) => res.json('activityId') === data.activityId,
            'slots response has availableSlots': (res) => Array.isArray(res.json('availableSlots')),
            'slots response time below 1000ms': (res) => res.timings.duration < 1000,
        });
    });

    sleep(1);
}