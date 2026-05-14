import { test, expect } from '@playwright/test';

test.describe('API testing - Bookings', () => {
    test('TC-BKG-001 / TC-WEA-002: POST /api/bookings handles booking according to weather availability', async ({ request }) => {
        const activitiesResponse = await request.get('/api/activities');
        expect(activitiesResponse.status()).toBe(200);

        const activities = await activitiesResponse.json();
        expect(Array.isArray(activities)).toBeTruthy();
        expect(activities.length).toBeGreaterThan(0);

        const activityId = activities[0].id;

        const tomorrow = new Date();
        tomorrow.setDate(tomorrow.getDate() + 1);
        const bookingDate = tomorrow.toISOString().split('T')[0];
        const bookingTime = '14:00';

        const weatherResponse = await request.get(
            `/api/weather/availability?activityId=${activityId}&date=${bookingDate}&time=${bookingTime}`
        );

        expect(weatherResponse.status()).toBe(200);

        const weatherBody = await weatherResponse.json();
        expect(weatherBody).toHaveProperty('allowed');
        expect(typeof weatherBody.allowed).toBe('boolean');

        const startTime = Date.now();

        const response = await request.post('/api/bookings', {
            data: {
                activityId,
                customerName: 'Harry Potter',
                customerEmail: `harry.${Date.now()}@hogwarts.edu`,
                bookingDate,
                bookingTime
            }
        });

        const duration = Date.now() - startTime;

        expect(duration).toBeLessThan(1000);

        const body = await response.json();

        if (weatherBody.allowed === true) {
            expect(response.status()).toBe(201);

            expect(body).toHaveProperty('id');
            expect(body).toHaveProperty('activityId', activityId);
            expect(body).toHaveProperty('customerName', 'Harry Potter');
            expect(body.customerEmail).toContain('@hogwarts.edu');
            expect(body).toHaveProperty('bookingDate', bookingDate);
            expect(body).toHaveProperty('status', 'ACTIVE');
        } else {
            expect(response.status()).toBe(400);

            expect(body).toHaveProperty('status', 400);
            expect(body).toHaveProperty('title', 'Validation error');
            expect(body).toHaveProperty('detail', 'Booking rejected due to unsuitable weather.');
        }
    });

    test('TC-BKG-002/003/006: POST /api/bookings rejects invalid booking data', async ({ request }) => {
        const startTime = Date.now();

        const response = await request.post('/api/bookings', {
            data: {
                activityId: -1,
                customerName: 'A',
                customerEmail: 'invalid-email',
                bookingDate: '2026-06-10',
                bookingTime: '14:00'
            }
        });

        const duration = Date.now() - startTime;

        expect(response.status()).toBe(400);
        expect(duration).toBeLessThan(1000);

        const body = await response.json();

        expect(body).toHaveProperty('status', 400);
        expect(body.title ?? body.error).toBeTruthy();
        expect(body.detail ?? body.path ?? body.instance).toBeTruthy();
    });

    test('TC-BKG-006: GET /api/bookings/{bookingId} returns 404 for non-existing booking', async ({ request }) => {
        const nonExistingBookingId = '00000000-0000-0000-0000-000000000000';

        const startTime = Date.now();

        const response = await request.get(`/api/bookings/${nonExistingBookingId}`);

        const duration = Date.now() - startTime;

        expect(response.status()).toBe(404);
        expect(duration).toBeLessThan(1000);

        const body = await response.json();

        expect(body).toHaveProperty('status', 404);
        expect(body).toHaveProperty('title', 'Resource not found');
        expect(body.detail).toContain('Booking not found');
    });

    test('TC-BKG-008: DELETE /api/bookings/{bookingId} returns 404 for non-existing booking', async ({ request }) => {
        const nonExistingBookingId = '00000000-0000-0000-0000-000000000000';

        const startTime = Date.now();

        const response = await request.delete(`/api/bookings/${nonExistingBookingId}`);

        const duration = Date.now() - startTime;

        expect(response.status()).toBe(404);
        expect(duration).toBeLessThan(1000);

        const body = await response.json();

        expect(body).toHaveProperty('status', 404);
        expect(body).toHaveProperty('title', 'Resource not found');
        expect(body.detail).toContain('Booking not found');
    });
});