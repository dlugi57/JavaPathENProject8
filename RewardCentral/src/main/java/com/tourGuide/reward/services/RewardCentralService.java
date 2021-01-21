package com.tourGuide.reward.services;

import java.util.UUID;

public interface RewardCentralService {

    int getAttractionRewardPoints(UUID attractionId, UUID userId);
}
