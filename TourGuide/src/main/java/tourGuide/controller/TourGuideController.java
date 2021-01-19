package tourGuide.controller;

import com.jsoniter.output.JsonStream;
import gpsUtil.location.VisitedLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import tourGuide.DTO.UserPreferencesDTO;
import tourGuide.service.TourGuideService;
import tourGuide.user.User;
import tripPricer.Provider;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
public class TourGuideController {

    static final Logger logger = LogManager
            .getLogger(TourGuideController.class);

    @Autowired
    TourGuideService tourGuideService;

    /**
     * Home page
     *
     * @return greetings
     */
    @RequestMapping("/")
    public String index() {
        return "Greetings from TourGuide!";
    }

    /**
     * Get user location
     *
     * @param userName user name
     * @return json with actual user location
     */
    @RequestMapping("/getLocation")
    public String getLocation(@RequestParam String userName) throws ExecutionException, InterruptedException {
        VisitedLocation visitedLocation = tourGuideService.getUserLocation(getUser(userName));
        return JsonStream.serialize(visitedLocation.location);
    }

    /**
     * Get the closest five tourist attractions to the user - no matter how far away they are.
     *
     * @param userName user name
     * @return json with 5 closest attractions
     */
    @RequestMapping("/getNearbyAttractions")
    public String getNearbyAttractions(@RequestParam String userName) throws ExecutionException, InterruptedException {
        User user = getUser(userName);
        return JsonStream.serialize(tourGuideService.getNearByAttractions(user));
    }

    @RequestMapping("/getRewards")
    public String getRewards(@RequestParam String userName) {
        return JsonStream.serialize(tourGuideService.getUserRewards(getUser(userName)));
    }

    /**
     * Get a list of every user's most recent location
     *
     * @return list of users recent locations
     */
    @RequestMapping("/getAllCurrentLocations")
    public String getAllCurrentLocations() {
        return JsonStream.serialize(tourGuideService.getAllCurrentLocations());
    }

    @RequestMapping("/getTripDeals")
    public String getTripDeals(@RequestParam String userName) {
        List<Provider> providers = tourGuideService.getTripDeals(getUser(userName));
        return JsonStream.serialize(providers);
    }

    /**
     * Get user
     *
     * @param userName user name
     * @return user
     */
    @RequestMapping("/getUser")
    private User getUser(String userName) {
        return tourGuideService.getUser(userName);
    }

    /**
     * Update user preferences
     *
     * @param userName        user name
     * @param userPreferences user preferences
     */
    @PutMapping("/updateUserPreferences")
    @ResponseStatus(HttpStatus.CREATED)
    private void updateUserPreferences(@RequestParam String userName,
                                       @RequestBody UserPreferencesDTO userPreferences) {
        // if user already exist send status and error message
        if (!tourGuideService.updateUserPreferences(userName, userPreferences)) {
            logger.error("PUT userPreferences -> " +
                    "updateUserPreferences /**/ HttpStatus : " + HttpStatus.CONFLICT + " /**/ Message : " +
                    " This user don't exist");

            throw new ResponseStatusException(HttpStatus.CONFLICT, "This user don't exist");
        }

        logger.info("PUT userPreferences -> updateUserPreferences /**/ HttpStatus : " + HttpStatus.CREATED);
    }

}