package com.tourGuide.reward.controllers;

import com.tourGuide.reward.services.RewardCentralService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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


}
