package tourGuide.service;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tourGuide.DTO.NearbyAttraction;
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

    public List<NearbyAttraction> getNearByAttractions(User user) {
        VisitedLocation visitedLocation = getUserLocation(user);
        List<NearbyAttraction> nearbyAttractions = new ArrayList<>();

        /*List result = list.stream().sorted((o1, o2)->o1.getItem().getValue().
                compareTo(o2.getItem().getValue())).
                collect(Collectors.toList());*/

        /*
        result = custom_obj_list.stream()
                        .sorted(Comparator.comparingDouble(X1::obj_function))  //X1 should be class name
                        .filter(x2 -> x2.obj_function() <= 1)
                        .collect(Collectors.toList());
         */

        List<Attraction> attractions = gpsUtil.getAttractions();
//Double.compare(p1.getY(), p2.gety());
        //Comparator<Attraction> reverseNameComparator =
        //   Comparator.comparingDouble(rewardsService.getDistance(h2,
        //  visitedLocation.location -> p.getY());


  /*      Comparator<Attraction> reverseNameComparator =
                (h1, h2) -> Double.compare(rewardsService.getDistance(h2,
                        visitedLocation.location) ,rewardsService.getDistance(h1,
                                visitedLocation.location));*/

        // TODO: 04/01/2021 is that limit 5 is on the right place ??
        List<Attraction> sortedList =
                attractions.stream().sorted((o1, o2) -> Double.compare(rewardsService.getDistance(o1,
                        visitedLocation.location), rewardsService.getDistance(o2,
                        visitedLocation.location))).limit(5).collect(Collectors.toList());

        sortedList.stream().forEach(attraction -> {
            NearbyAttraction nearbyAttraction = new NearbyAttraction();

            nearbyAttraction.setName(attraction.attractionName);
            // TODO: 04/01/2021 wtf with this extend of location in attraction
            nearbyAttraction.setAttractionLocation(new Location(attraction.latitude,
                    attraction.longitude));
            // TODO: 04/01/2021 the same for this
            nearbyAttraction.setUserLocation(visitedLocation.location);
            nearbyAttraction.setDistance(rewardsService.getDistance(attraction,
                    visitedLocation.location));

            nearbyAttraction.setRewardPoints(rewardsService.getRewardPoints(attraction, user));
            // TODO: 04/01/2021 how to limit for 5 users only
            nearbyAttractions.add(nearbyAttraction);
        });


   /*     for (Attraction attraction : gpsUtil.getAttractions()) {

            if (rewardsService.isWithinAttractionProximity(attraction, visitedLocation.location)) {
                nearbyAttractions.add(attraction);
            }
        }*/

        return nearbyAttractions;
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
