package com.tourGuide.gps.services;

import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;

import java.util.List;
import java.util.UUID;

public interface GpsService {
    VisitedLocation getUserLocation(UUID userId);

    List<Attraction> getAttractions();
}
