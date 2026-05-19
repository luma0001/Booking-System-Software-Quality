import { test, expect } from '@playwright/test';

test.describe('API testing - Activities', () => {
    test('GET /api/activities returns activities list', async ({ request }) => {
        const startTime = Date.now();

        const response = await request.get('/api/activities');
        const duration = Date.now() - startTime;

        expect(response.status()).toBe(200);
        expect(duration).toBeLessThan(1000);

        const body = await response.json();

        expect(Array.isArray(body)).toBeTruthy();
        expect(body.length).toBeGreaterThan(0);

        expect(body[0]).toHaveProperty('id');
        expect(body[0]).toHaveProperty('name');
        expect(body[0]).toHaveProperty('description');
        expect(body[0]).toHaveProperty('location');
        expect(body[0]).toHaveProperty('maxParticipants');
    });

    test('GET /api/activities/{activityId}/slots returns available slots for valid activity and date', async ({ request }) => {
        const activitiesResponse = await request.get('/api/activities');

        expect(activitiesResponse.status()).toBe(200);

        const activities = await activitiesResponse.json();

        expect(Array.isArray(activities)).toBeTruthy();
        expect(activities.length).toBeGreaterThan(0);

        const activityId = activities[0].id;
        const bookingDate = '2026-06-10';

        const startTime = Date.now();

        const response = await request.get(`/api/activities/${activityId}/slots?date=${bookingDate}`);

        const duration = Date.now() - startTime;

        expect(response.status()).toBe(200);
        expect(duration).toBeLessThan(1000);

        const body = await response.json();

        expect(body).toHaveProperty('activityId', activityId);
        expect(body).toHaveProperty('date', bookingDate);
        expect(body).toHaveProperty('availableSlots');
        expect(Array.isArray(body.availableSlots)).toBeTruthy();
    });
});