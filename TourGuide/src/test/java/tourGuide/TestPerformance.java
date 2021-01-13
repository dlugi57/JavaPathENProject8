package tourGuide;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.Test;
import rewardCentral.RewardCentral;
import tourGuide.helper.InternalTestHelper;
import tourGuide.service.RewardsService;
import tourGuide.service.TourGuideService;
import tourGuide.user.User;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;

public class TestPerformance {

    /*
     * A note on performance improvements:
     *
     *     The number of users generated for the high volume tests can be easily adjusted via this method:
     *
     *     		InternalTestHelper.setInternalUserNumber(100000);
     *
     *
     *     These tests can be modified to suit new solutions, just as long as the performance metrics
     *     at the end of the tests remains consistent.
     *
     *     These are performance metrics that we are trying to hit:
     *
     *     highVolumeTrackLocation: 100,000 users within 15 minutes:
     *     		assertTrue(TimeUnit.MINUTES.toSeconds(15) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
     *
     *     highVolumeGetRewards: 100,000 users within 20 minutes:
     *          assertTrue(TimeUnit.MINUTES.toSeconds(20) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
     */

    //@Ignore
    @Test
    public void highVolumeTrackLocation() throws InterruptedException {
        GpsUtil gpsUtil = new GpsUtil();
        RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
        // Users should be incremented up to 100,000, and test finishes within 15 minutes
        InternalTestHelper.setInternalUserNumber(1000);
        TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);

        List<User> allUsers = new ArrayList<>();
        allUsers = tourGuideService.getAllUsers();

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        // TODO: 08/01/2021 can i just send more at once ?
		//allUsers.parallelStream().forEach(u->tourGuideService.trackUserLocation(u));


		tourGuideService.trackListOfUserLocation(allUsers);
/*		for(User user : allUsers) {
			tourGuideService.trackUserLocation(user);
		}*/
        stopWatch.stop();
        tourGuideService.tracker.stopTracking();

        System.out.println("highVolumeTrackLocation: Time Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()) + " seconds.");

        assertTrue(TimeUnit.MINUTES.toSeconds(15) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
    }

    //@Ignore
    @Test
    public void highVolumeGetRewards() throws ExecutionException, InterruptedException {
        GpsUtil gpsUtil = new GpsUtil();
        RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());

        // Users should be incremented up to 100,000, and test finishes within 20 minutes
        InternalTestHelper.setInternalUserNumber(100000);
        TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);
        List<CompletableFuture> futures = new ArrayList();
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        // TODO: 07/01/2021 you have to be kidding me is only that ??
        //tourGuideService.tracker.stopTracking();


        Attraction attraction = gpsUtil.getAttractions().get(0);
        List<User> allUsers = new ArrayList<>();
        allUsers = tourGuideService.getAllUsers();


        allUsers.forEach(u -> u.addToVisitedLocations(new VisitedLocation(u.getUserId(),
				attraction, new Date())));
        tourGuideService.tracker.stopTracking();
        allUsers.forEach(u -> futures.add(CompletableFuture.runAsync(() -> {
            rewardsService.calculateRewards(u);
        })));


        CompletableFuture<Void> combinedFuture = CompletableFuture.allOf(futures.toArray(new CompletableFuture[allUsers.size()]));
        combinedFuture.get();


/*
        allUsers.parallelStream().forEach(user -> {
            user.addToVisitedLocations(new VisitedLocation(user.getUserId(), attraction, new Date()));
            rewardsService.calculateRewards(user);
            assertTrue(user.getUserRewards().size() > 0);
        });*/

        //ExecutorService executorService = Executors.newFixedThreadPool(1000);

		/*for(User user : allUsers) {

			Runnable runnableTask = () -> {
				user.addToVisitedLocations(new VisitedLocation(user.getUserId(), attraction, new Date()));
				rewardsService.calculateRewards(user);
				assertTrue(user.getUserRewards().size() > 0);
			};

			executorService.execute(runnableTask);
		}
		executorService.shutdown();
		executorService.awaitTermination(25, TimeUnit.MINUTES);
		*/


        stopWatch.stop();
        //tourGuideService.tracker.stopTracking();

        System.out.println("highVolumeGetRewards: Time Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()) + " seconds.");
        assertTrue(TimeUnit.MINUTES.toSeconds(20) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));





    }

}
