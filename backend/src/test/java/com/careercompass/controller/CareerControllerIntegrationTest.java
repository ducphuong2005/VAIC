package com.careercompass.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CareerControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void searchesCareersByAliasAndReturnsPagedResponse() throws Exception {
        mockMvc.perform(get("/api/v1/careers/search").param("q", "Data Analyst"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].onetCode").value("15-2051.00"))
                .andExpect(jsonPath("$.data.content[0].titleVi").value("Nhà khoa học dữ liệu"));
    }

    @Test
    void returnsCareerDetailWithSkillsTasksAndRelatedCareers() throws Exception {
        mockMvc.perform(get("/api/v1/careers/15-2051.00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.onetCode").value("15-2051.00"))
                .andExpect(jsonPath("$.data.topSkills[0].category").value("SKILL"))
                .andExpect(jsonPath("$.data.technologySkills[0].skillName").value("Python"))
                .andExpect(jsonPath("$.data.tasks[0].taskText").exists())
                .andExpect(jsonPath("$.data.relatedOccupations[0].onetCode").exists());
    }
}
