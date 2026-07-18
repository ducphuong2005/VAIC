package com.careercompass.controller;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.careercompass.entity.LlmPurpose;
import com.careercompass.repository.LlmCallRepository;
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
class ChatFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LlmCallRepository llmCallRepository;

    @Test
    void createsChatSessionAndStoresAssistantResponse() throws Exception {
        String token = registerAndGetAccessToken("chat-flow@example.com");
        MvcResult sessionResult = mockMvc.perform(post("/api/v1/chat/sessions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Career questions\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").exists())
                .andReturn();
        String sessionId = objectMapper.readTree(sessionResult.getResponse().getContentAsString())
                .path("data")
                .path("id")
                .asText();

        mockMvc.perform(post("/api/v1/chat/sessions/{id}/messages", sessionId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"Vì sao nghề Data Scientist phù hợp với tôi?\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.intent").value("CAREER_RECOMMENDATION"))
                .andExpect(jsonPath("$.data.confidence").value(76))
                .andExpect(jsonPath("$.data.sources.length()", greaterThanOrEqualTo(1)));

        mockMvc.perform(get("/api/v1/chat/sessions/{id}", sessionId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.messages.length()").value(2))
                .andExpect(jsonPath("$.data.messages[0].sender").value("USER"))
                .andExpect(jsonPath("$.data.messages[1].sender").value("ASSISTANT"));

        mockMvc.perform(get("/api/v1/recommendations/latest")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.recommendations.length()", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.data.recommendations[0].sources", hasItem("data/jobs.csv")));

        mockMvc.perform(get("/api/v1/learning-paths")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.data[0].steps.length()", greaterThanOrEqualTo(3)));
    }

    @Test
    void greetingDoesNotReturnCareerFallback() throws Exception {
        String token = registerAndGetAccessToken("chat-greeting@example.com");
        long llmCallsBefore = llmCallRepository.count();
        MvcResult sessionResult = mockMvc.perform(post("/api/v1/chat/sessions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Greeting\"}"))
                .andExpect(status().isOk())
                .andReturn();
        String sessionId = objectMapper.readTree(sessionResult.getResponse().getContentAsString())
                .path("data")
                .path("id")
                .asText();

        mockMvc.perform(post("/api/v1/chat/sessions/{id}/messages", sessionId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"hi\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.intent").value("GENERAL_CAREER_CHAT"))
                .andExpect(jsonPath("$.data.confidence").value(88))
                .andExpect(jsonPath("$.data.recommendations.length()").value(1));
        org.assertj.core.api.Assertions.assertThat(llmCallRepository.count()).isEqualTo(llmCallsBefore + 2);
        org.assertj.core.api.Assertions.assertThat(llmCallRepository.findAll().stream()
                        .filter(call -> call.getPurpose() == LlmPurpose.OPEN_RESPONSE_ANALYSIS)
                        .count())
                .isGreaterThanOrEqualTo(1);
        org.assertj.core.api.Assertions.assertThat(llmCallRepository.findAll().stream()
                        .filter(call -> call.getPurpose() == LlmPurpose.CHAT_RESPONSE)
                        .count())
                .isGreaterThanOrEqualTo(1);
    }

    private String registerAndGetAccessToken(String email) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "StrongPass123",
                                  "fullName": "Chat User"
                                }
                                """.formatted(email)))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        return root.path("data").path("accessToken").asText();
    }
}
