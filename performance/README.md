# Stress performance testing

This folder contains k6 performance tests for the Hogwarts Booking System.

The exam task requires:

- load testing
- stress testing
- spike testing
- reports or screenshots documenting the tests

## Tool

The tests are implemented with k6.

## Tested endpoints

The tests use read-only API endpoints:

- GET /api/activities
- GET /api/activities/{activityId}/slots?date=...

The POST /api/bookings endpoint is not used in the performance tests because it writes to the database and depends on live weather data. Using read-only endpoints makes the performance tests more stable and repeatable.

## Prerequisites

Start the database:

```bash
cd database
docker compose -f docker-compose.psql.yml up -d
cd ..
```

## Git integration.
performance tests can be added to our git integration by adding these lines:
``
      - name: Setup k6
        uses: grafana/setup-k6-action@ffe7d7290dfa715e48c2ccc924d068444c94bde2 # v1.1.0

      - name: Run k6 performance tests
        run: |
          k6 run performance/load-test.js
          k6 run performance/spike-test.js
          k6 run performance/stress-test.js
``
to this file:
.github/workflows/continuous-testing.yml