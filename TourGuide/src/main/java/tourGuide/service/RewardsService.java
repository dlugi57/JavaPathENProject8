package tourGuide.service;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rewardCentral.RewardCentral;
import tourGuide.proxies.RewardCentralProxy;
import tourGuide.user.User;
import tourGuide.user.UserReward;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class RewardsService {

    @Autowired
    RewardCentralProxy rewardCentralProxy;

    private static final double STATUTE_MILES_PER_NAUTICAL_MILE = 1.15077945;

    // proximity in miles
    private int defaultProximityBuffer = 10;
    private int proximityBuffer = defaultProximityBuffer;
    private int attractionProximityRange = 200;
    private final GpsUtil gpsUtil;
    private final RewardCentral rewardsCentral;

    /**
     * Initialization of services
     *
     * @param gpsUtil       gps service instantiation
     * @param rewardCentral reward central instantiation
     */
    public RewardsService(GpsUtil gpsUtil, RewardCentral rewardCentral) {
        this.gpsUtil = gpsUtil;
        this.rewardsCentral = rewardCentral;
    }
    

    /**
     * Set proximity buffer to know when user will get points of attraction
     *
     * @param proximityBuffer proximity distance of attraction
     */
    public void setProximityBuffer(int proximityBuffer) {
        this.proximityBuffer = proximityBuffer;
    }

    /**
     * Set default proximity attraction distance
     */
    public void setDefaultProximityBuffer() {
        proximityBuffer = defaultProximityBuffer;
    }

    /**
     * Calculate user rewards by attraction visited
     *
     * @param user user object
     * @return reward included in user object
     */
    public CompletableFuture calculateRewards(User user) {

        ExecutorService executorService = Executors.newFixedThreadPool(1000);

        return CompletableFuture.runAsync(() -> {

            List<Attraction> attractions = gpsUtil.getAttractions();
            // to prevent asynchronous array insertion
            CopyOnWriteArrayList<VisitedLocation> userLocations = new CopyOnWriteArrayList<>();

            userLocations.addAll(user.getVisitedLocations());

            userLocations.forEach(vl -> {
                attractions.stream()
                        // check if attraction is in the limited reward distance
                        .filter(a -> nearAttraction(vl, a))
                        .forEach(a -> {
                            // check if user have already this attraction
                            if (user.getUserRewards().stream().noneMatch(
                                    r -> r.attraction.attractionName.equals(a.attractionName))) {
                                user.addUserReward(new UserReward(vl, a, getRewardPoints(a, user)));
                            }
                        });
            });
        }, executorService);
    }

    /**
     * Check if location is in the limited distance from attraction
     *
     * @param attraction attraction
     * @param location   location
     * @return true when success
     */
    public boolean isWithinAttractionProximity(Attraction attraction, Location location) {
        return getDistance(attraction, location) > attractionProximityRange ? false : true;
    }

    /**
     * Check if user is in the limited distance from attraction
     *
     * @param attraction      attraction
     * @param visitedLocation user visited location
     * @return true when success
     */
    private boolean nearAttraction(VisitedLocation visitedLocation, Attraction attraction) {
        return getDistance(attraction, visitedLocation.location) > proximityBuffer ? false : true;
    }

    /**
     * Get reward points
     *
     * @param attraction attraction object
     * @param user       user object
     * @return reward points
     */
    public int getRewardPoints(Attraction attraction, User user) {

        //return rewardCentralProxy.getAttractionRewardPoints(attraction.attractionId, user.getUserId());

        return rewardsCentral.getAttractionRewardPoints(attraction.attractionId, user.getUserId());

    }

    /**
     * Calculate distance from one location to another
     *
     * @param loc1 first comparative location
     * @param loc2 second comparative location
     * @return distance in meters
     */
    public double getDistance(Location loc1, Location loc2) {
        double lat1 = Math.toRadians(loc1.latitude);
        double lon1 = Math.toRadians(loc1.longitude);
        double lat2 = Math.toRadians(loc2.latitude);
        double lon2 = Math.toRadians(loc2.longitude);

        double angle = Math.acos(Math.sin(lat1) * Math.sin(lat2)
                + Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon1 - lon2));

        double nauticalMiles = 60 * Math.toDegrees(angle);
        double statuteMiles = STATUTE_MILES_PER_NAUTICAL_MILE * nauticalMiles;
        return statuteMiles;
    }

}
