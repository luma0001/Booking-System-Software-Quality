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