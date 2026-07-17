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
class InternalCrawlerMarketIntegrationTest {

    private static final String API_KEY = "test-crawler-api-key";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void importsJobsDeduplicatesAndRecalculatesMarketSignals() throws Exception {
        MvcResult runResult = mockMvc.perform(post("/api/internal/v1/crawler/runs")
                        .header("X-Internal-Api-Key", API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sourceName\":\"TopCV\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("RUNNING"))
                .andReturn();
        long crawlRunId = objectMapper.readTree(runResult.getResponse().getContentAsString()).path("data").path("id").asLong();

        mockMvc.perform(post("/api/internal/v1/jobs/batch")
                        .header("X-Internal-Api-Key", API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "crawlRunId": %d,
                                  "jobs": [
                                    {
                                      "sourceName": "TopCV",
                                      "externalId": "job-1",
                                      "contentHash": "hash-1",
                                      "title": "Data Analyst Intern",
                                      "companyName": "Demo Co",
                                      "location": "Hà Nội",
                                      "region": "Hà Nội",
                                      "salaryMin": 10000000,
                                      "salaryMax": 15000000,
                                      "remote": true,
                                      "entryLevel": true,
                                      "onetCode": "15-2051.00",
                                      "rawUrl": "https://topcv.vn/job-1",
                                      "description": "Analyze data",
                                      "skills": ["SQL", "Python"]
                                    },
                                    {
                                      "sourceName": "TopCV",
                                      "externalId": "job-duplicate",
                                      "contentHash": "hash-1",
                                      "title": "Duplicate",
                                      "onetCode": "15-2051.00"
                                    },
                                    {
                                      "sourceName": "TopCV",
                                      "externalId": "job-rejected",
                                      "contentHash": "hash-rejected",
                                      "title": "Missing O*NET"
                                    }
                                  ]
                                }
                                """.formatted(crawlRunId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.inserted").value(1))
                .andExpect(jsonPath("$.data.duplicate").value(1))
                .andExpect(jsonPath("$.data.rejected").value(1));

        mockMvc.perform(post("/api/internal/v1/market-signals/recalculate")
                        .header("X-Internal-Api-Key", API_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(greaterThanOrEqualTo(1)));

        mockMvc.perform(put("/api/internal/v1/crawler/runs/{id}", crawlRunId)
                        .header("X-Internal-Api-Key", API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"COMPLETED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("COMPLETED"))
                .andExpect(jsonPath("$.data.inserted").value(1));

        mockMvc.perform(get("/api/v1/market/trending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].onetCode").value("15-2051.00"))
                .andExpect(jsonPath("$.data[0].source").value("TopCV"));

        mockMvc.perform(get("/api/v1/market/skills-in-demand"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].skillName").exists());

        mockMvc.perform(get("/api/v1/market/careers/15-2051.00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.limitation").exists())
                .andExpect(jsonPath("$.data.signals[0].sampleSize").value(1));
    }

    @Test
    void rejectsInternalApiWithoutApiKey() throws Exception {
        mockMvc.perform(post("/api/internal/v1/crawler/runs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sourceName\":\"TopCV\"}"))
                .andExpect(status().isForbidden());
    }
}
