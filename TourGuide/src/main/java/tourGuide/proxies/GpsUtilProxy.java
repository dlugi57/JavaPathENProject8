package tourGuide.proxies;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import tourGuide.model.Attraction;
import tourGuide.model.VisitedLocation;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "gps", url = "localhost:8081")
public interface GpsUtilProxy {

    @GetMapping("/gps/getUserLocation")
    VisitedLocation getUserLocation(@RequestParam("userId") final UUID userId);

    @GetMapping("/gps/getAttractions")
    List<Attraction> getAttractions();

}
