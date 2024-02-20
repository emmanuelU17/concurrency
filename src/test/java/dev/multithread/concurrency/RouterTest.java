package dev.multithread.concurrency;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import java.time.Duration;
import java.time.Instant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class RouterTest {

    private static final Logger log = LoggerFactory
            .getLogger(RouterTest.class);

    @Value(value = "/${api.base.url}")
    private String url;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void singleThread() throws Exception {
        var now = Instant.now();
        mockMvc.perform(get(url))
                .andExpect(status().isOk());
        var end = Instant.now();

        log.info("Start of single thread {}", now);
        log.info("End of single thread {}", end);
        log.info("Time taken for single thread {}", Duration.between(now, end).getSeconds());
    }

    @Test
    void future () throws Exception {
        var now = Instant.now();
        MvcResult result = mockMvc
                .perform(get(url + "/future"))
                .andExpect(request().asyncStarted())
                .andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.log())
                .andReturn();

        this.mockMvc.perform(asyncDispatch(result))
                .andExpect(content().contentType("application/json"))
                .andExpect(status().isOk());

        var end = Instant.now();

        log.info("Start of CompletableFuture {}", now);
        log.info("End of CompletableFuture {}", end);
        log.info("Time taken for CompletableFuture {}", Duration.between(now, end).getSeconds());
    }

    @Test
    void callable () throws Exception {
        var now = Instant.now();
        mockMvc.perform(get(url + "/callable"))
                .andExpect(status().isOk());

        var end = Instant.now();

        log.info("Start of Callable {}", now);
        log.info("End of Callable {}", end);
        log.info("Time taken for Callable {}", Duration.between(now, end).getSeconds());
    }

}
