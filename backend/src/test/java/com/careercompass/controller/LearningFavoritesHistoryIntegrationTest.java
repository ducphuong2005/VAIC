package com.careercompass.controller;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
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
class LearningFavoritesHistoryIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void managesLearningPathFavoritesAndActivityHistory() throws Exception {
        String token = registerAndGetAccessToken("learning-flow@example.com");

        mockMvc.perform(post("/api/v1/favorites/careers/15-2051.00").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/favorites/careers").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].onetCode").value("15-2051.00"));

        mockMvc.perform(post("/api/v1/favorites/courses/1").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/favorites/courses").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(1));

        mockMvc.perform(post("/api/v1/favorites/links")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "linkType": "JOB",
                                  "provider": "TopCV",
                                  "title": "Data Analyst trên TopCV",
                                  "url": "https://www.topcv.vn/tim-viec-lam-data-analyst",
                                  "description": "Link tìm job phù hợp"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.linkType").value("JOB"));
        mockMvc.perform(post("/api/v1/favorites/links")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "linkType": "LEARNING",
                                  "provider": "W3Schools",
                                  "title": "SQL Tutorial",
                                  "url": "https://www.w3schools.com/sql/",
                                  "description": "Ôn SQL"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.linkType").value("LEARNING"));
        mockMvc.perform(get("/api/v1/favorites/links").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()", greaterThanOrEqualTo(2)))
                .andExpect(jsonPath("$.data[*].provider", hasItem("TopCV")))
                .andExpect(jsonPath("$.data[*].provider", hasItem("W3Schools")));

        MvcResult pathResult = mockMvc.perform(post("/api/v1/learning-paths/generate")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                .content("{\"onetCode\":\"15-2051.00\",\"route\":\"SELF_STUDY\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.steps.length()").value(3))
                .andExpect(jsonPath("$.data.steps[0].resources.length()", greaterThanOrEqualTo(2)))
                .andExpect(jsonPath("$.data.steps[0].resources[*].provider", hasItem("Coursera")))
                .andReturn();
        JsonNode path = objectMapper.readTree(pathResult.getResponse().getContentAsString()).path("data");
        String pathId = path.path("id").asText();
        long stepId = path.path("steps").get(0).path("id").asLong();

        mockMvc.perform(put("/api/v1/learning-paths/{id}/steps/{stepId}/progress", pathId, stepId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"progressPercent\":100}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.steps[0].completed").value(true));

        mockMvc.perform(get("/api/v1/activity-history").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()", greaterThanOrEqualTo(2)));
    }

    private String registerAndGetAccessToken(String email) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "StrongPass123",
                                  "fullName": "Learning User"
                                }
                                """.formatted(email)))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).path("data").path("accessToken").asText();
    }
}
