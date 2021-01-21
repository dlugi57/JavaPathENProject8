package com.tourGuide.reward.controllers;

import com.tourGuide.reward.services.RewardCentralService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/reward")
public class RewardCentralController {

    static final Logger logger = LogManager
            .getLogger(RewardCentralController.class);

    // gps service initialization
    RewardCentralService rewardCentralService;

    /**
     * Instance of reward service
     *
     * @param rewardCentralService reward service
     */
    @Autowired
    public RewardCentralController(RewardCentralService rewardCentralService) {
        this.rewardCentralService = rewardCentralService;
    }

    /**
     * Get attraction reward points
     *
     * @param attractionId attraction id
     * @param userId user id
     * @return rewards points
     */
    @GetMapping("/getAttractionRewardPoints")
    public int getAttractionRewardPoints(@RequestParam UUID attractionId,@RequestParam  UUID userId) {
        logger.info("GET Integer -> getAttractionRewardPoints /**/ HttpStatus : " +
                HttpStatus.OK + " -> response" + rewardCentralService.getAttractionRewardPoints(attractionId, userId));
        return rewardCentralService.getAttractionRewardPoints(attractionId, userId);
    }

}
