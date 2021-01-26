package tourGuide.services;


import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import rewardCentral.RewardCentral;
import tourGuide.DTO.NearbyAttractionDTO;
import tourGuide.DTO.UserPreferencesDTO;
import tourGuide.helper.InternalTestHelper;
import tourGuide.model.Location;
import tourGuide.model.VisitedLocation;
import tourGuide.proxies.GpsUtilProxy;
import tourGuide.service.RewardsService;
import tourGuide.service.TourGuideService;
import tourGuide.user.User;
import tripPricer.Provider;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestTourGuideService {

    @Autowired
    GpsUtilProxy gpsUtilProxy;

    @Autowired
    TourGuideService tourGuideService;

    @Test
    public void getUserLocation() throws ExecutionException, InterruptedException {
        InternalTestHelper.setInternalUserNumber(0);
        //TourGuideService tourGuideService = new TourGuideService();

        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        VisitedLocation visitedLocation = tourGuideService.trackUserLocation(user).get();
        tourGuideService.tracker.stopTracking();
        assertTrue(visitedLocation.userId.equals(user.getUserId()));
    }

    @Test
    public void addUser() {
        InternalTestHelper.setInternalUserNumber(0);
        //TourGuideService tourGuideService = new TourGuideService();

        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        User user2 = new User(UUID.randomUUID(), "jon2", "000", "jon2@tourGuide.com");

        tourGuideService.addUser(user);
        tourGuideService.addUser(user2);

        User retrivedUser = tourGuideService.getUser(user.getUserName());
        User retrivedUser2 = tourGuideService.getUser(user2.getUserName());

        tourGuideService.tracker.stopTracking();

        assertEquals(user, retrivedUser);
        assertEquals(user2, retrivedUser2);
    }

    @Test
    public void getAllUsers() {
        InternalTestHelper.setInternalUserNumber(0);
        //TourGuideService tourGuideService = new TourGuideService();

        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        User user2 = new User(UUID.randomUUID(), "jon2", "000", "jon2@tourGuide.com");

        tourGuideService.addUser(user);
        tourGuideService.addUser(user2);

        List<User> allUsers = tourGuideService.getAllUsers();

        tourGuideService.tracker.stopTracking();

        assertTrue(allUsers.contains(user));
        assertTrue(allUsers.contains(user2));
    }

    @Test
    public void trackUser() throws ExecutionException, InterruptedException {
        InternalTestHelper.setInternalUserNumber(0);
        //TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);

        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        VisitedLocation visitedLocation = tourGuideService.trackUserLocation(user).get();

        tourGuideService.tracker.stopTracking();

        assertEquals(user.getUserId(), visitedLocation.userId);
    }

    @Test
    public void getNearbyAttractions() throws ExecutionException, InterruptedException {
        InternalTestHelper.setInternalUserNumber(0);
        //TourGuideService tourGuideService = new TourGuideService();

        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        VisitedLocation visitedLocation = tourGuideService.trackUserLocation(user).get();

        List<NearbyAttractionDTO> attractions = tourGuideService.getNearByAttractions(user);

        tourGuideService.tracker.stopTracking();

        assertEquals(5, attractions.size());
    }

    @Test
    public void getTripDeals() {
        InternalTestHelper.setInternalUserNumber(0);
        //TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);

        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");

        List<Provider> providers = tourGuideService.getTripDeals(user);

        tourGuideService.tracker.stopTracking();

        assertEquals(5, providers.size());
    }

    @Test
    public void getAllCurrentLocations() {
        InternalTestHelper.setInternalUserNumber(0);

        // GIVEN
        Location location = new Location(
                ThreadLocalRandom.current().nextDouble(-85.05112878D,
                        85.05112878D),
                ThreadLocalRandom.current().nextDouble(-180.0D, 180.0D));
        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        User user2 = new User(UUID.randomUUID(), "jon2", "222",
                "jon2@tourGuide.com");
        //TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);
        tourGuideService.addUser(user);
        tourGuideService.addUser(user2);
        user.addToVisitedLocations(
                new VisitedLocation(user.getUserId(), location, new Date()));
        user2.addToVisitedLocations(
                new VisitedLocation(user2.getUserId(), location, new Date()));

        // WHEN
        Map<String, Location> result = tourGuideService.getAllCurrentLocations();

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(2);
    }

    @Test
    public void updateUserPreferences() {
        InternalTestHelper.setInternalUserNumber(0);
        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        //TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);

        tourGuideService.addUser(user);

        UserPreferencesDTO userPreferencesDto = new UserPreferencesDTO();
        userPreferencesDto.setTripDuration(3);
        userPreferencesDto.setTicketQuantity(7);
        userPreferencesDto.setNumberOfAdults(2);
        userPreferencesDto.setNumberOfChildren(5);
        userPreferencesDto.setCurrency("EUR");
        userPreferencesDto.setHighPricePoint(1000);
        userPreferencesDto.setLowerPricePoint(100);
        userPreferencesDto.setAttractionProximity(100);


        // UserPreferences userPreferences =
        //    new UserPreferencesMapper().mapPreferences(userPreferencesDto);


        // WHEN
        boolean result = tourGuideService.updateUserPreferences("jon",
                userPreferencesDto);

        // THEN
        assertThat(result).isTrue();
        assertThat(user.getUserPreferences().getTripDuration()).isEqualTo(3);
        assertThat(user.getUserPreferences().getTicketQuantity()).isEqualTo(7);
        assertThat(user.getUserPreferences().getNumberOfAdults()).isEqualTo(2);
        assertThat(user.getUserPreferences().getNumberOfChildren())
                .isEqualTo(5);
    }

}
