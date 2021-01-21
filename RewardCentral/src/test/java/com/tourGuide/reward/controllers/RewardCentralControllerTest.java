package com.tourGuide.reward.controllers;

import com.tourGuide.reward.services.RewardCentralService;
import junit.framework.TestCase;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(RewardCentralController.class)
public class RewardCentralControllerTest{

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RewardCentralService service;

    @Test
    public void testGetAttractionRewardPoints() throws Exception {

        UUID userId = UUID.randomUUID();
        UUID attractionId = UUID.randomUUID();

        when(service.getAttractionRewardPoints(any(UUID.class),any(UUID.class))).thenReturn(123);

        this.mockMvc
                .perform(get("/reward/getAttractionRewardPoints")
                        .contentType("application/json")
                        .param("attractionId", attractionId.toString())
                        .param("userId", userId.toString()))
                .andExpect(status().isOk()).andDo(print());

    }
}