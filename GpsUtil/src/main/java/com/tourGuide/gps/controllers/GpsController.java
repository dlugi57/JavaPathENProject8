package com.tourGuide.gps.controllers;

import com.tourGuide.gps.services.GpsService;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/gps")
public class GpsController {

    // gps service initialization
    GpsService gpsService;

    /**
     * Instance of gps service
     * @param gpsService gps service
     */
    @Autowired
    public GpsController(GpsService gpsService) {
        this.gpsService = gpsService;
    }

    /**
     * Get last user location
     * @param userId user id
     * @return last user location
     */
    @GetMapping("/getUserLocation")
    public VisitedLocation getUserLocation(@RequestParam final UUID userId) {
       return gpsService.getUserLocation(userId);
    }

    /**
     * Get list of attractions
     * @return list of attractions
     */
    @GetMapping("/getAttractions")
    public List<Attraction> getAttractions(){
        return gpsService.getAttractions();
    };

}
