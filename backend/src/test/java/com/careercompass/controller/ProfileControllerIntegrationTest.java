package com.careercompass.controller;

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
class ProfileControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void updatesProfileAndReturnsCompletionForFrontend() throws Exception {
        String token = registerAndGetAccessToken("profile-user@example.com");
        String request = """
                {
                  "educationLevel": "UNIVERSITY",
                  "school": "Đại học Kinh tế Quốc dân",
                  "currentMajor": "Hệ thống thông tin",
                  "preferredRegions": ["Hà Nội", "Remote"],
                  "learningBudget": 5000000,
                  "availableLearningHoursPerWeek": 10,
                  "preferredEducationRoutes": ["UNIVERSITY", "CERTIFICATE"],
                  "careerGoals": "Trở thành Data Analyst",
                  "existingSkills": [
                    {"skillName": "SQL", "score": 70},
                    {"skillName": "Python", "score": 60}
                  ]
                }
                """;

        mockMvc.perform(put("/api/v1/profile")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.school").value("Đại học Kinh tế Quốc dân"))
                .andExpect(jsonPath("$.data.existingSkills.length()").value(2));

        mockMvc.perform(get("/api/v1/profile/completion")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.completionPercent").value(100))
                .andExpect(jsonPath("$.data.status").value("GOOD"));
    }

    @Test
    void rejectsProfileWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/profile"))
                .andExpect(status().isUnauthorized());
    }

    private String registerAndGetAccessToken(String email) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "StrongPass123",
                                  "fullName": "Profile User"
                                }
                                """.formatted(email)))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        return root.path("data").path("accessToken").asText();
    }
}
