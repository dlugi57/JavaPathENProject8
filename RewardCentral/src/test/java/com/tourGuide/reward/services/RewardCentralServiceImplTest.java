package com.tourGuide.reward.services;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;
import rewardCentral.RewardCentral;

import javax.annotation.Resource;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
public class RewardCentralServiceImplTest {

    @MockBean
    RewardCentral rewardsCentral;

    @Autowired
    RewardCentralService service;

    @Test
    public void testGetAttractionRewardPoints() {
        UUID attractionId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        // GIVEN
        when(rewardsCentral.getAttractionRewardPoints(attractionId, userId))
                .thenReturn(100);

        // WHEN
        int result = service.getAttractionRewardPoints(attractionId,
                userId);

        // THEN
        assertThat(result).isGreaterThan(0);
    }
}