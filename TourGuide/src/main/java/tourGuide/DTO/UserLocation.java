package tourGuide.DTO;

import gpsUtil.location.Location;
import lombok.Data;

import java.util.UUID;

@Data
public class UserLocation {
    private UUID userId;
    private Location location;
}
