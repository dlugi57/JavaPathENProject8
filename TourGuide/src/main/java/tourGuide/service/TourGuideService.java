package tourGuide.service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tourGuide.DTO.NearbyAttractionDTO;
import tourGuide.DTO.UserLocationDTO;
import tourGuide.DTO.UserPreferencesDTO;
import tourGuide.helper.InternalTestHelper;
import tourGuide.mapper.UserPreferencesMapper;
import tourGuide.model.Attraction;
import tourGuide.model.Location;
import tourGuide.model.VisitedLocation;
import tourGuide.proxies.GpsUtilProxy;
import tourGuide.tracker.Tracker;
import tourGuide.user.User;
import tourGuide.user.UserPreferences;
import tourGuide.user.UserReward;
import tripPricer.Provider;
import tripPricer.TripPricer;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class TourGuideService {

    private final Logger logger = LoggerFactory.getLogger(TourGuideService.class);

    @Autowired
    RewardsService rewardsService;

    @Autowired
    GpsUtilProxy gpsUtilProxy;

    private final TripPricer tripPricer = new TripPricer();
    public final Tracker tracker;

    boolean testMode = false;

    /**
     * Service constructor
     */
    public TourGuideService() {
        // patch of wrong locale in application
        Locale.setDefault(Locale.US);

        //initialize tracker
        tracker = new Tracker(this);

        if (testMode) {
            logger.info("TestMode enabled");
            logger.debug("Initializing users");
            initializeInternalUsers();
            logger.debug("Finished initializing users");
        } else {
            tracker.startTracking();
        }

        addShutDownHook();
    }

    /**
     * Get user rewards
     *
     * @param user user object
     * @return user reward
     */
    public List<UserReward> getUserRewards(User user) {
        return user.getUserRewards();
    }

    /**
     * Get user location last location if not track user
     *
     * @param user user object
     * @return last visited location
     * @throws ExecutionException   execution exception when use completable future
     * @throws InterruptedException interrupted exception when use completable future
     */
    public VisitedLocation getUserLocation(User user) throws ExecutionException, InterruptedException {
        return (user.getVisitedLocations().size() > 0) ?
                user.getLastVisitedLocation() :
                trackUserLocation(user).get();
    }

    /**
     * Get a list of every users most recent location
     *
     * @return list of users recent locations
     */
    public Map<String, Location> getAllCurrentLocations() {

        List<User> usersList = getAllUsers();
        Map<String, Location> usersLocations = new HashMap<>();

        usersList.parallelStream().forEach(user -> {

            UserLocationDTO userLocationDTO = new UserLocationDTO();

            if (user.getVisitedLocations().size() > 0) {
                userLocationDTO.setLocation(user.getLastVisitedLocation().location);
                userLocationDTO.setUserId(user.getUserId());
                usersLocations.put(user.getUserId().toString(), user.getLastVisitedLocation().location);
            }
        });

        return usersLocations;
    }

    /**
     * Get user
     *
     * @param userName user name string
     * @return user
     */
    public User getUser(String userName) {
        return internalUserMap.get(userName);
    }

    /**
     * Get all users
     *
     * @return list of users
     */
    public List<User> getAllUsers() {
        return internalUserMap.values().stream().collect(Collectors.toList());
    }

    /**
     * Add user
     *
     * @param user user object with obligatory parameters
     */
    public void addUser(User user) {
        if (!internalUserMap.containsKey(user.getUserName())) {
            internalUserMap.put(user.getUserName(), user);
        }
    }

    /**
     * Update user preferences
     *
     * @param userName           user name
     * @param userPreferencesDTO user preferences DTO object
     * @return true if success
     */
    public boolean updateUserPreferences(String userName, UserPreferencesDTO userPreferencesDTO) {

        if (!internalUserMap.containsKey(userName)) {
            return false;
        }
        User user = internalUserMap.get(userName);

        UserPreferences userPreferences = new UserPreferencesMapper().mapPreferences(userPreferencesDTO);

        user.setUserPreferences(userPreferences);

        internalUserMap.put(userName, user);

        return true;
    }

    /**
     * Get trip deals
     *
     * @param user user object
     * @return list of providers
     */
    public List<Provider> getTripDeals(User user) {
        int cumulatativeRewardPoints = user.getUserRewards().stream().mapToInt(i -> i.getRewardPoints()).sum();
        List<Provider> providers = tripPricer.getPrice(tripPricerApiKey, user.getUserId(), user.getUserPreferences().getNumberOfAdults(),
                user.getUserPreferences().getNumberOfChildren(), user.getUserPreferences().getTripDuration(), cumulatativeRewardPoints);
        user.setTripDeals(providers);
        return providers;
    }

    /**
     * Track the last user position
     *
     * @param user user object
     * @return last visited location
     */
    public CompletableFuture<VisitedLocation> trackUserLocation(User user) {
        // set completable future supply methode to get asynchronous result from future
        return CompletableFuture.supplyAsync(() -> gpsUtilProxy.getUserLocation(user.getUserId())).thenApply(visitedLocation -> {
            user.addToVisitedLocations(visitedLocation);
            try {
                rewardsService.calculateRewards(user).get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            return visitedLocation;
        });
    }

    /**
     * Get the closest five tourist attractions to the user - no matter how far away they are.
     *
     * @param user object with location
     * @return Nearby Attraction list with all data needed
     */
    public List<NearbyAttractionDTO> getNearByAttractions(User user) throws ExecutionException, InterruptedException {

        VisitedLocation visitedLocation = getUserLocation(user);
        // dto result list
        List<NearbyAttractionDTO> nearbyAttractions = new ArrayList<>();

        List<Attraction> attractions = gpsUtilProxy.getAttractions();

        // get 5 closest attractions and populate DTO
        attractions.parallelStream().sorted(Comparator.comparingDouble(o -> rewardsService.getDistance(o,
                visitedLocation.location))).limit(5).forEach(attraction -> {
            // initialize nearby attraction
            NearbyAttractionDTO nearbyAttractionDTO = new NearbyAttractionDTO();

            nearbyAttractionDTO.setName(attraction.attractionName);
            nearbyAttractionDTO.setAttractionLocation(new Location(attraction.latitude,
                    attraction.longitude));
            nearbyAttractionDTO.setUserLocation(visitedLocation.location);
            nearbyAttractionDTO.setDistance(rewardsService.getDistance(attraction,
                    visitedLocation.location));
            nearbyAttractionDTO.setRewardPoints(rewardsService.getRewardPoints(attraction, user));

            nearbyAttractions.add(nearbyAttractionDTO);
        });

        // after using parallel stream list is disordered we sort it again
        List<NearbyAttractionDTO> nearbyAttractionsSorted =
                nearbyAttractions.parallelStream().sorted(Comparator.comparingDouble(a -> a.distance)).collect(Collectors.toList());

        return nearbyAttractionsSorted;
    }

    /**
     * stop users tracking service
     */
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
            user.addToVisitedLocations(new VisitedLocation(user.getUserId(),
                    new Location(generateRandomLatitude(), generateRandomLongitude()),
                    getRandomTime()));
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
