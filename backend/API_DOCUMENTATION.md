# API Documentation

Base URL:

```text
/api/v1
```

## Auth

- `POST /auth/register`
- `POST /auth/login`
- `POST /auth/refresh`
- `GET /auth/me`

Sample login:

```json
{
  "email": "student@example.com",
  "password": "StrongPass123"
}
```

## Profile

- `GET /profile`
- `PUT /profile`
- `GET /profile/dimensions`
- `GET /profile/evidence`
- `POST /profile/recalculate`
- `GET /profile/completion`

## Careers

- `GET /careers?page=0&size=20&q=data&cluster=Công nghệ thông tin`
- `GET /careers/search?q=Data Analyst`
- `GET /careers/{onetCode}`
- `GET /careers/{onetCode}/skills`
- `GET /careers/{onetCode}/tasks`
- `GET /careers/{onetCode}/related`
- `GET /career-clusters`

## Assessment

- `GET /assessments`
- `GET /assessments/{id}`
- `POST /assessments/{id}/start`
- `POST /assessment-sessions/{sessionId}/answers`
- `POST /assessment-sessions/{sessionId}/complete`
- `GET /assessment-sessions/{sessionId}/result`

## Mini-game

- `GET /minigames`
- `GET /minigames/{id}`
- `POST /minigames/{id}/start`
- `POST /minigame-sessions/{sessionId}/actions`
- `POST /minigame-sessions/{sessionId}/complete`
- `GET /minigame-sessions/{sessionId}/result`

## Market

- `GET /market/trending`
- `GET /market/skills-in-demand`
- `GET /market/regions`
- `GET /market/careers/{onetCode}`
- `GET /market/careers/{onetCode}/trend`

## Recommendations

- `POST /recommendations/generate`
- `GET /recommendations/latest`
- `GET /recommendations/runs/{runId}`
- `GET /recommendations/{id}`
- `POST /recommendations/{id}/feedback`
- `GET /recommendations/{id}/skill-gaps`

## Chatbot

- `POST /chat/sessions`
- `GET /chat/sessions`
- `GET /chat/sessions/{id}`
- `POST /chat/sessions/{id}/messages`

## Learning

- `POST /learning-paths/generate`
- `GET /learning-paths`
- `GET /learning-paths/{id}`
- `PUT /learning-paths/{id}/steps/{stepId}/progress`

## Favorites and History

- `POST /favorites/careers/{onetCode}`
- `DELETE /favorites/careers/{onetCode}`
- `GET /favorites/careers`
- `POST /favorites/courses/{courseId}`
- `DELETE /favorites/courses/{courseId}`
- `GET /favorites/courses`
- `GET /activity-history`

## Internal Crawler API

Prefix:

```text
/api/internal/v1
```

Header:

```text
X-Internal-Api-Key: ${INTERNAL_CRAWLER_API_KEY}
```

- `POST /crawler/runs`
- `PUT /crawler/runs/{id}`
- `POST /jobs/batch`
- `POST /jobs/{id}/skills`
- `POST /market-signals/recalculate`

## Admin

- `POST /admin/fairness-tests/run`
- `GET /admin/fairness-tests`
