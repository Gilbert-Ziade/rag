package org.efrei.rag.web.test;

import org.efrei.rag.RagApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = { RagApplication.class })
@AutoConfigureMockMvc
public class SampleResourceIT {

    private static final String ENTITY_API_URL = "/return-text";

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private MockMvc restProjectMockMvc;

    @Test
    public void sampleTest() throws Exception {
        String value = "EFREI";
        restProjectMockMvc
                .perform(get(ENTITY_API_URL + "/"+value))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/plain;charset=UTF-8"))
                .andExpect(content().string("you wrote : " + value));

    }

}