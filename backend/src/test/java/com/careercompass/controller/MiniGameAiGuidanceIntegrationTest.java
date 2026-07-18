package com.careercompass.controller;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
class MiniGameAiGuidanceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void completingMiniGameCreatesAiAdviceRecommendationsAndLearningPath() throws Exception {
        String token = registerAndGetAccessToken("minigame-ai@example.com");

        MvcResult sessionResult = mockMvc.perform(post("/api/v1/minigames/{id}/start", "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.sessionId").exists())
                .andReturn();
        String sessionId = objectMapper.readTree(sessionResult.getResponse().getContentAsString())
                .path("data")
                .path("sessionId")
                .asText();

        mockMvc.perform(post("/api/v1/minigame-sessions/{sessionId}/complete", sessionId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "rawMetrics": {
                                    "accuracy": 0.92,
                                    "durationSeconds": 180,
                                    "answerChanges": 1
                                  },
                                  "resultSummary": "{\\"game\\":\\"RIASEC Escape Room\\",\\"topTypes\\":[\\"I\\",\\"C\\"]}"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("COMPLETED"))
                .andExpect(jsonPath("$.data.advice.content").isNotEmpty())
                .andExpect(jsonPath("$.data.advice.careerOptions.length()", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.data.advice.marketJobs.length()", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.data.advice.sources", hasItem("data/jobs.csv")));

        mockMvc.perform(get("/api/v1/recommendations/latest")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.recommendations.length()", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.data.recommendations[0].reasons.length()", greaterThanOrEqualTo(2)))
                .andExpect(jsonPath("$.data.recommendations[0].sources", hasItem("data/jobs.csv")));

        mockMvc.perform(get("/api/v1/minigames/latest-result")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("COMPLETED"))
                .andExpect(jsonPath("$.data.resultSummary").exists())
                .andExpect(jsonPath("$.data.advice.sources", hasItem("data/jobs.csv")));

        mockMvc.perform(get("/api/v1/learning-paths")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.data[0].steps.length()", greaterThanOrEqualTo(3)))
                .andExpect(jsonPath("$.data[0].steps[0].resources.length()", greaterThanOrEqualTo(2)))
                .andExpect(jsonPath("$.data[0].steps[0].resources[*].provider", hasItem("Coursera")));
    }

    private String registerAndGetAccessToken(String email) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "StrongPass123",
                                  "fullName": "Mini Game User"
                                }
                                """.formatted(email)))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        return root.path("data").path("accessToken").asText();
    }
}
