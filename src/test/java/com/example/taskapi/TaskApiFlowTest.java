package com.example.taskapi;

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

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end flow test: register -> login -> JWT -> create/list tasks -> authorization checks.
 * Runs against H2 in-memory, so no MySQL server is required.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TaskApiFlowTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void fullJwtFlow() throws Exception {
        // 1) Register → returns a token
        String registerBody = """
                {"fullName":"Ahmed Hassan","email":"ahmed@example.com","password":"secret123"}
                """;
        MvcResult registerResult = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.email").value("ahmed@example.com"))
                .andReturn();
        String registerToken = tokenFrom(registerResult);

        // 2) Login → returns a (valid) token
        String loginBody = """
                {"email":"ahmed@example.com","password":"secret123"}
                """;
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn();
        String loginToken = tokenFrom(loginResult);

        // 3) Create a task with the token
        String taskBody = """
                {"title":"Prepare for the interview","description":"Review OOP, SQL and JWT","completed":false}
                """;
        mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + loginToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(taskBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Prepare for the interview"));

        // 4) List my tasks with the token
        mockMvc.perform(get("/api/tasks")
                        .header("Authorization", "Bearer " + loginToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        // 5) Token generated on register is equally valid
        mockMvc.perform(get("/api/tasks")
                        .header("Authorization", "Bearer " + registerToken))
                .andExpect(status().isOk());

        // 6) No token → 403 (authenticated endpoint)
        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isForbidden());

        // 7) Garbage token → request stays unauthenticated → 403
        mockMvc.perform(get("/api/tasks")
                        .header("Authorization", "Bearer not.a.real.token"))
                .andExpect(status().isForbidden());
    }

    @Test
    void loginWithWrongPasswordFails() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fullName":"Sara Ali","email":"sara@example.com","password":"secret123"}
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"sara@example.com","password":"wrong-password"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void duplicateEmailIsRejected() throws Exception {
        String body = """
                {"fullName":"Mona Omar","email":"mona@example.com","password":"secret123"}
                """;
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isUnauthorized());
    }

    private String tokenFrom(MvcResult result) throws Exception {
        JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
        return node.get("token").asText();
    }
}