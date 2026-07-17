package com.careercompass.controller;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RecommendationFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void generatesRecommendationSkillGapsAndAcceptsFeedback() throws Exception {
        String token = registerAndGetAccessToken("recommendation-flow@example.com");
        updateProfile(token);
        completeAssessment(token);
        importMarketData();

        MvcResult result = mockMvc.perform(post("/api/v1/recommendations/generate")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"region\":\"Hà Nội\",\"limit\":3}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.runId").exists())
                .andExpect(jsonPath("$.data.recommendations.length()", greaterThanOrEqualTo(3)))
                .andExpect(jsonPath("$.data.recommendations[0].scores.finalScore").exists())
                .andExpect(jsonPath("$.data.recommendations[0].sources[0]").exists())
                .andReturn();

        JsonNode firstRecommendation = objectMapper.readTree(result.getResponse().getContentAsString())
                .path("data")
                .path("recommendations")
                .get(0);
        long recommendationId = firstRecommendation.path("id").asLong();

        mockMvc.perform(get("/api/v1/recommendations/latest")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.recommendations[0].id").value(recommendationId));

        mockMvc.perform(get("/api/v1/recommendations/{id}/skill-gaps", recommendationId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].skillName").exists())
                .andExpect(jsonPath("$.data[0].priority").exists());

        mockMvc.perform(post("/api/v1/recommendations/{id}/feedback", recommendationId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rating\":5,\"feedbackText\":\"Helpful recommendation\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.rating").value(5));
    }

    private String registerAndGetAccessToken(String email) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "StrongPass123",
                                  "fullName": "Recommendation User"
                                }
                                """.formatted(email)))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString())
                .path("data")
                .path("accessToken")
                .asText();
    }

    private void updateProfile(String token) throws Exception {
        mockMvc.perform(put("/api/v1/profile")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "educationLevel": "UNIVERSITY",
                                  "school": "Demo University",
                                  "currentMajor": "Information Systems",
                                  "preferredRegions": ["Hà Nội"],
                                  "learningBudget": 5000000,
                                  "availableLearningHoursPerWeek": 12,
                                  "preferredEducationRoutes": ["CERTIFICATE"],
                                  "careerGoals": "Become a data analyst",
                                  "existingSkills": [
                                    {"skillName": "SQL", "score": 70},
                                    {"skillName": "Python", "score": 40}
                                  ]
                                }
                                """))
                .andExpect(status().isOk());
    }

    private void completeAssessment(String token) throws Exception {
        MvcResult startResult = mockMvc.perform(post("/api/v1/assessments/22222222-2222-2222-2222-222222222222/start")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        String sessionId = objectMapper.readTree(startResult.getResponse().getContentAsString())
                .path("data")
                .path("sessionId")
                .asText();

        mockMvc.perform(post("/api/v1/assessment-sessions/{sessionId}/answers", sessionId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "answers": [
                                    {"questionId": 1, "optionId": 1},
                                    {"questionId": 2, "optionId": 3}
                                  ]
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/assessment-sessions/{sessionId}/complete", sessionId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    private void importMarketData() throws Exception {
        mockMvc.perform(post("/api/internal/v1/jobs/batch")
                        .header("X-Internal-Api-Key", "test-crawler-api-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "jobs": [
                                    {
                                      "sourceName": "TopCV",
                                      "externalId": "rec-job-1",
                                      "contentHash": "rec-hash-1",
                                      "title": "Data Analyst",
                                      "region": "Hà Nội",
                                      "remote": true,
                                      "entryLevel": true,
                                      "onetCode": "15-2051.00",
                                      "skills": ["SQL", "Python"]
                                    }
                                  ]
                                }
                                """))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/internal/v1/market-signals/recalculate")
                        .header("X-Internal-Api-Key", "test-crawler-api-key"))
                .andExpect(status().isOk());
    }
}
