package com.careercompass.controller;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
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
class MiniGameFlowIntegrationTest {

    private static final UUID DATA_DETECTIVE_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void completesMiniGameAndUpdatesDynamicProfile() throws Exception {
        String token = registerAndGetAccessToken("minigame-flow@example.com");

        mockMvc.perform(get("/api/v1/minigames").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()", greaterThanOrEqualTo(3)));

        MvcResult startResult = mockMvc.perform(post("/api/v1/minigames/{id}/start", DATA_DETECTIVE_ID)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("IN_PROGRESS"))
                .andReturn();
        String sessionId = objectMapper.readTree(startResult.getResponse().getContentAsString())
                .path("data")
                .path("sessionId")
                .asText();

        mockMvc.perform(post("/api/v1/minigame-sessions/{sessionId}/actions", sessionId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "actions": [
                                    {"actionType": "SELECT_OPTION", "actionPayload": {"question": 1, "option": "A"}},
                                    {"actionType": "CHANGE_ANSWER", "actionPayload": {"question": 1, "from": "A", "to": "B"}}
                                  ]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2));

        mockMvc.perform(post("/api/v1/minigame-sessions/{sessionId}/complete", sessionId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "rawMetrics": {
                                    "accuracy": 0.8,
                                    "durationSeconds": 300,
                                    "answerChanges": 2
                                  },
                                  "resultSummary": "Phân tích dữ liệu tốt, còn có thể tối ưu tốc độ."
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("COMPLETED"))
                .andExpect(jsonPath("$.data.metrics[0].normalizedScore").exists())
                .andExpect(jsonPath("$.data.evidenceCreated").value(3));

        mockMvc.perform(get("/api/v1/profile/evidence").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()", greaterThanOrEqualTo(3)));
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
