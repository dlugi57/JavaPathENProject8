package com.tourGuide.reward.services;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rewardCentral.RewardCentral;

@Service
public class RewardCentralServiceImpl implements RewardCentralService {

    static final Logger logger = LogManager
            .getLogger(RewardCentralServiceImpl.class);

    // gps util initialization
    RewardCentral rewardCentral;

    /**
     * Instance of reward central jar application
     *
     * @param rewardCentral reward central service
     */
    @Autowired
    public void setRewardCentral(RewardCentral rewardCentral) {
        this.rewardCentral = rewardCentral;
    }


}
