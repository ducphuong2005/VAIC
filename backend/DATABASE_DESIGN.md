# Database Design

Database dùng MySQL 8 và Flyway.

## Key Design

- UUID/CHAR(36): users, assessment sessions, mini-game sessions, recommendation runs, learning paths, conversation sessions, llm calls.
- BIGINT auto-increment: detail, evidence, activity, skill gaps, market rows.
- UTC timestamps.
- Foreign keys/indexes cho lookup phổ biến.

## Core Groups

- Identity: `users`, `user_settings`
- Profile: `student_profiles`, `user_existing_skills`, `user_profile_dimensions`, `profile_evidence`
- Career/O*NET: `occupations`, `onet_elements`, `occupation_element_scores`, `occupation_tasks`, `technology_skills`
- Assessment: `assessments`, `assessment_questions`, `assessment_options`, `assessment_sessions`, `assessment_answers`
- Mini-game: `mini_games`, `mini_game_metrics`, `mini_game_onet_mapping`, `mini_game_sessions`, `mini_game_actions`, `mini_game_metric_results`
- Market: `job_postings`, `job_posting_skills`, `market_signals`, `crawl_runs`, `job_postings_raw`
- Recommendation: `recommendation_runs`, `career_recommendations`, `recommendation_evidence`, `skill_gaps`, `recommendation_feedback`
- LLM/RAG/Chat: `llm_calls`, `rag_documents`, `rag_retrieval_logs`, `conversation_sessions`, `conversation_messages`
- Learning/Favorites/History: `courses`, `learning_paths`, `learning_path_steps`, `user_favorite_occupations`, `user_favorite_courses`, `activity_logs`
- Fairness: `fairness_test_runs`, `recommendation_audits`

## Migration Files

Migrations live at `src/main/resources/db/migration`.
