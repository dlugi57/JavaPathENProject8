package com.tourGuide.reward.services;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rewardCentral.RewardCentral;

import java.util.UUID;

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

    /**
     * Get attraction reward points
     *
     * @param attractionId attraction id
     * @param userId user id
     * @return rewards points
     */
    @Override
    public int getAttractionRewardPoints(UUID attractionId, UUID userId) {
        logger.debug(rewardCentral.getAttractionRewardPoints(attractionId, userId));
        return rewardCentral.getAttractionRewardPoints(attractionId, userId);
    }
}
