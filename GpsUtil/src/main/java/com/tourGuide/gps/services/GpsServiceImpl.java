package com.tourGuide.gps.services;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class GpsServiceImpl implements GpsService {

    static final Logger logger = LogManager
            .getLogger(GpsServiceImpl.class);

    // gps util initialization
    GpsUtil gpsUtil;

    /**
     * Instance of gps util jar application
     * @param gpsUtil gps util service
     */
    @Autowired
    public void setGpsUtil(GpsUtil gpsUtil) {
        this.gpsUtil = gpsUtil;
    }

    /**
     * Get user location
     * @param userId user id
     * @return last user visited location
     */
    @Override
    public VisitedLocation getUserLocation(UUID userId) {
        return gpsUtil.getUserLocation(userId);
    }

    /**
     * Get list of attraction
     * @return list of attractions
     */
    @Override
    public List<Attraction> getAttractions() {
        return gpsUtil.getAttractions();
    };

}
