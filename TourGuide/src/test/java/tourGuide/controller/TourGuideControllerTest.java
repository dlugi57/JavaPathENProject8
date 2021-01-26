package tourGuide.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import tourGuide.DTO.NearbyAttractionDTO;
import tourGuide.DTO.UserPreferencesDTO;
import tourGuide.helper.InternalTestHelper;
import tourGuide.model.Attraction;
import tourGuide.model.Location;
import tourGuide.model.VisitedLocation;
import tourGuide.proxies.GpsUtilProxy;
import tourGuide.proxies.RewardCentralProxy;
import tourGuide.service.RewardsService;
import tourGuide.service.TourGuideService;
import tourGuide.user.User;
import tourGuide.user.UserReward;
import tripPricer.Provider;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(TourGuideController.class)
@AutoConfigureMockMvc(addFilters = false)
public class TourGuideControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    RewardCentralProxy rewardCentralProxy;

    @MockBean
    RewardsService rewardsService;

    @MockBean
    GpsUtilProxy gpsUtilProxy;

    @MockBean
    private TourGuideService tourGuideService;

    ObjectMapper mapper = new ObjectMapper();


    @BeforeEach
    void init() {
        InternalTestHelper.setInternalUserNumber(1);
        tourGuideService.tracker.stopTracking();
    }

    @Test
    public void index() throws Exception {
        mockMvc.perform(get("/"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void testGetLocation() throws Exception {
        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        Location locationMock = new Location(1.0d, 1.0d);
        Date date = new Date();
        date.setTime(System.currentTimeMillis());
        VisitedLocation visitedLocationMock = new VisitedLocation(user.getUserId(), locationMock, date);

        when(tourGuideService.getUserLocation(tourGuideService.getUser(anyString()))).thenReturn(visitedLocationMock);
        this.mockMvc.perform(get("/getLocation")
                .param("userName", user.getUserName()))
                .andDo(print())
                .andExpect(status().isOk());
    }

    public static NearbyAttractionDTO nearbyAttractionDTO = new NearbyAttractionDTO();


    @Test
    public void testGetNearbyAttractions() throws Exception {
        nearbyAttractionDTO.setAttractionLocation(new Location(1.0d, 1.0d));
        nearbyAttractionDTO.setName("name");
        nearbyAttractionDTO.setDistance(10.0);
        nearbyAttractionDTO.setRewardPoints(100);
        nearbyAttractionDTO.setUserLocation(new Location(1.0d, 1.0d));

        List<NearbyAttractionDTO> nearbyAttractions = new ArrayList<>();

        nearbyAttractions.add(nearbyAttractionDTO);
        nearbyAttractions.add(nearbyAttractionDTO);
        nearbyAttractions.add(nearbyAttractionDTO);


        User user = new User(UUID.randomUUID(), "internalUser1", "000",
                "jon@tourGuide.com");
        when(tourGuideService.getNearByAttractions(any(User.class))).thenReturn(nearbyAttractions);

        this.mockMvc
                .perform(get("/getNearbyAttractions")
                        .contentType("application/json")
                        .param("userName", user.getUserName()))
                .andExpect(status().isOk()).andDo(print());
    }

    public static LocalDateTime localDateTime = LocalDateTime.now().minusDays(new Random().nextInt(30));
    public static Date date = Date.from(localDateTime.toInstant(ZoneOffset.UTC));
    public static UserReward userReward = new UserReward(new VisitedLocation(UUID.randomUUID(),
            new Location(1.0d, 1.0d),
            date), new Attraction("name", "city", "state",
            1.0d, 1.0d), 666);

    public static List<UserReward> userRewards = new ArrayList<>();

    static {
        userRewards.add(userReward);
        userRewards.add(userReward);
        userRewards.add(userReward);
    }

    @Test
    public void testGetRewards() throws Exception {
        User user = new User(UUID.randomUUID(), "internalUser1", "000",
                "jon@tourGuide.com");
        when(tourGuideService.getUserRewards(any(User.class))).thenReturn(userRewards);

        this.mockMvc
                .perform(get("/getRewards")
                        .contentType("application/json")
                        .param("userName", user.getUserName()))
                .andExpect(status().isOk()).andDo(print());

    }

    public static Location location = new Location(1.0d, 1.0d);

    public static Map<String, Location> locations = new HashMap<>();

    static {
        locations.put("provider", location);
        locations.put("provider1", location);
        locations.put("provider2", location);
    }


    @Test
    public void testGetAllCurrentLocations() throws Exception {

        when(tourGuideService.getAllCurrentLocations()).thenReturn(locations);

        this.mockMvc
                .perform(get("/getAllCurrentLocations")
                        .contentType("application/json"))
                .andExpect(status().isOk()).andDo(print());
    }

    public static Provider provider = new Provider(UUID.randomUUID(), "name", 10.0);

    public static List<Provider> providers = new ArrayList<>();

    static {
        providers.add(provider);
        providers.add(provider);
        providers.add(provider);
    }

    @Test
    public void testGetTripDeals() throws Exception {
        User user = new User(UUID.randomUUID(), "internalUser1", "000",
                "jon@tourGuide.com");
        when(tourGuideService.getTripDeals(any(User.class))).thenReturn(providers);

        this.mockMvc
                .perform(get("/getTripDeals")
                        .contentType("application/json")
                        .param("userName", user.getUserName()))
                .andExpect(status().isOk()).andDo(print());
    }


    @Test
    public void updateUserPreferences() throws Exception {

        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");

        UserPreferencesDTO userPreferencesDTO = new UserPreferencesDTO();

        userPreferencesDTO.setAttractionProximity(1);
        userPreferencesDTO.setCurrency("USD");
        userPreferencesDTO.setLowerPricePoint(2);
        userPreferencesDTO.setHighPricePoint(3);
        userPreferencesDTO.setTripDuration(4);
        userPreferencesDTO.setTicketQuantity(5);
        userPreferencesDTO.setNumberOfAdults(6);

        when(tourGuideService.updateUserPreferences(anyString(), any(UserPreferencesDTO.class))).thenReturn(true);
        this.mockMvc.perform(put("/updateUserPreferences")
                .param("userName", user.getUserName())
                .content((mapper.writeValueAsString(userPreferencesDTO)))
                .contentType("application/json"))

                .andDo(print())
                .andExpect(status().isCreated());
    }

    @Test
    public void updateUserPreferences_Invalid() throws Exception {
        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");

        Mockito.when(tourGuideService.updateUserPreferences(anyString(), any(UserPreferencesDTO.class))).thenReturn(false);
        this.mockMvc.perform(put("/updateUserPreferences")
                .param("userName", user.getUserName()))
                .andDo(print())
                .andExpect(status().is4xxClientError());
    }
}