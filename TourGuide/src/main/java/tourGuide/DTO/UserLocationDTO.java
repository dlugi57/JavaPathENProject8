package tourGuide.DTO;

import lombok.Data;
import tourGuide.model.Location;

import java.util.UUID;

@Data
public class UserLocationDTO {
    private UUID userId;
    private Location location;
}
