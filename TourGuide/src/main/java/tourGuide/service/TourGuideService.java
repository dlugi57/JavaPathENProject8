package tourGuide.service;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tourGuide.DTO.NearbyAttraction;
import tourGuide.DTO.UserLocation;
import tourGuide.helper.InternalTestHelper;
import tourGuide.tracker.Tracker;
import tourGuide.user.User;
import tourGuide.user.UserReward;
import tripPricer.Provider;
import tripPricer.TripPricer;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class TourGuideService {
    private Logger logger = LoggerFactory.getLogger(TourGuideService.class);
    private final GpsUtil gpsUtil;
    private final RewardsService rewardsService;
    private final TripPricer tripPricer = new TripPricer();
    public final Tracker tracker;
    boolean testMode = true;

    public TourGuideService(GpsUtil gpsUtil, RewardsService rewardsService) {
        Locale.setDefault(Locale.US);
        this.gpsUtil = gpsUtil;
        this.rewardsService = rewardsService;

        if (testMode) {
            logger.info("TestMode enabled");
            logger.debug("Initializing users");
            initializeInternalUsers();
            logger.debug("Finished initializing users");
        }
        tracker = new Tracker(this);
        addShutDownHook();
    }

    public List<UserReward> getUserRewards(User user) {
        return user.getUserRewards();
    }

    public VisitedLocation getUserLocation(User user) {
        VisitedLocation visitedLocation = (user.getVisitedLocations().size() > 0) ?
                user.getLastVisitedLocation() :
                trackUserLocation(user);
        return visitedLocation;
    }

    /**
     * Get a list of every user's most recent location
     *
     * @return list of users recent locations
     */
    public Map<String, Location> getAllCurrentLocations() {

        List<User> usersList = getAllUsers();

        Map<String, Location> usersLocations = new HashMap<>();

        usersList.parallelStream().forEach(user -> {

            UserLocation userLocation = new UserLocation();

            if (user.getVisitedLocations().size() > 0) {
                userLocation.setLocation(user.getLastVisitedLocation().location);
                userLocation.setUserId(user.getUserId());

                usersLocations.put(user.getUserId().toString(), user.getLastVisitedLocation().location);
            }
        });

        return usersLocations;
    }


    public User getUser(String userName) {
        return internalUserMap.get(userName);
    }

    public List<User> getAllUsers() {
        return internalUserMap.values().stream().collect(Collectors.toList());
    }

    public void addUser(User user) {
        if (!internalUserMap.containsKey(user.getUserName())) {
            internalUserMap.put(user.getUserName(), user);
        }
    }

    // TODO: 05/01/2021 create endpoint which allows us to change user preferences  
    public List<Provider> getTripDeals(User user) {
        int cumulatativeRewardPoints = user.getUserRewards().stream().mapToInt(i -> i.getRewardPoints()).sum();
        List<Provider> providers = tripPricer.getPrice(tripPricerApiKey, user.getUserId(), user.getUserPreferences().getNumberOfAdults(),
                user.getUserPreferences().getNumberOfChildren(), user.getUserPreferences().getTripDuration(), cumulatativeRewardPoints);
        user.setTripDeals(providers);
        return providers;
    }

    public VisitedLocation trackUserLocation(User user) {
        VisitedLocation visitedLocation = gpsUtil.getUserLocation(user.getUserId());
        user.addToVisitedLocations(visitedLocation);
        rewardsService.calculateRewards(user);
        return visitedLocation;
    }

    /**
     * Get the closest five tourist attractions to the user - no matter how far away they are.
     *
     * @param user object with location
     * @return Nearby Attraction list with all data needed
     */
    public List<NearbyAttraction> getNearByAttractions(User user) {

        VisitedLocation visitedLocation = getUserLocation(user);
        // dto result list
        List<NearbyAttraction> nearbyAttractions = new ArrayList<>();

        List<Attraction> attractions = gpsUtil.getAttractions();

        // get 5 closest attractions and populate DTO
        attractions.parallelStream().sorted(Comparator.comparingDouble(o -> rewardsService.getDistance(o,
                visitedLocation.location))).limit(5).forEach(attraction -> {
            // initialize nearby attraction
            NearbyAttraction nearbyAttraction = new NearbyAttraction();

            nearbyAttraction.setName(attraction.attractionName);
            nearbyAttraction.setAttractionLocation(new Location(attraction.latitude,
                    attraction.longitude));
            nearbyAttraction.setUserLocation(visitedLocation.location);
            nearbyAttraction.setDistance(rewardsService.getDistance(attraction,
                    visitedLocation.location));
            nearbyAttraction.setRewardPoints(rewardsService.getRewardPoints(attraction, user));

            nearbyAttractions.add(nearbyAttraction);
        });

        // after using parallel stream list is disordered i needed to sort it again
        List<NearbyAttraction> nearbyAttractionsSorted =
                nearbyAttractions.parallelStream().sorted(Comparator.comparingDouble(a -> a.distance)).collect(Collectors.toList());

        return nearbyAttractionsSorted;
    }

    private void addShutDownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                tracker.stopTracking();
            }
        });
    }

    /**********************************************************************************
     *
     * Methods Below: For Internal Testing
     *
     **********************************************************************************/
    private static final String tripPricerApiKey = "test-server-api-key";
    // Database connection will be used for external users, but for testing purposes internal users are provided and stored in memory
    private final Map<String, User> internalUserMap = new HashMap<>();

    private void initializeInternalUsers() {
        IntStream.range(0, InternalTestHelper.getInternalUserNumber()).forEach(i -> {
            String userName = "internalUser" + i;
            String phone = "000";
            String email = userName + "@tourGuide.com";
            User user = new User(UUID.randomUUID(), userName, phone, email);
            generateUserLocationHistory(user);

            //DEBUG
             /*
            UserReward userReward = new UserReward(new VisitedLocation(UUID.randomUUID(),
                    new Location(generateRandomLatitude(), generateRandomLongitude()),
                    getRandomTime()), new Attraction("name" + i, "city" + i, "state" + i,
                    generateRandomLatitude(), generateRandomLongitude()), 666);

            user.addUserReward(userReward);*/

            internalUserMap.put(userName, user);
        });
        logger.debug("Created " + InternalTestHelper.getInternalUserNumber() + " internal test users.");
    }

    private void generateUserLocationHistory(User user) {
        IntStream.range(0, 3).forEach(i -> {
            user.addToVisitedLocations(new VisitedLocation(user.getUserId(), new Location(generateRandomLatitude(), generateRandomLongitude()), getRandomTime()));
        });
    }

    private double generateRandomLongitude() {
        double leftLimit = -180;
        double rightLimit = 180;
        return leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
    }

    private double generateRandomLatitude() {
        double leftLimit = -85.05112878;
        double rightLimit = 85.05112878;
        return leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
    }

    private Date getRandomTime() {
        LocalDateTime localDateTime = LocalDateTime.now().minusDays(new Random().nextInt(30));
        return Date.from(localDateTime.toInstant(ZoneOffset.UTC));
    }

}
