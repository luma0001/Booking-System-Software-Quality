import { test, expect } from '@playwright/test';

test.describe('API testing - Weather', () => {
    test('GET /api/weather/availability returns weather availability result', async ({ request }) => {
        const activitiesResponse = await request.get('/api/activities');
        expect(activitiesResponse.status()).toBe(200);

        const activities = await activitiesResponse.json();
        expect(Array.isArray(activities)).toBeTruthy();
        expect(activities.length).toBeGreaterThan(0);

        const activityId = activities[0].id;

        const tomorrow = new Date();
        tomorrow.setDate(tomorrow.getDate() + 1);
        const date = tomorrow.toISOString().split('T')[0];

        const startTime = Date.now();

        const response = await request.get(
            `/api/weather/availability?activityId=${activityId}&date=${date}&time=14:00`
        );

        expect(response.status()).toBe(200);

        const body = await response.json();

        expect(body).toHaveProperty('activityId', activityId);
        expect(body).toHaveProperty('date', date);
        expect(body).toHaveProperty('time');
        expect(body).toHaveProperty('allowed');
        expect(typeof body.allowed).toBe('boolean');
        expect(body).toHaveProperty('reason');
    });
});