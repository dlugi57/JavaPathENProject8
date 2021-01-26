package tourGuide.DTO;
import lombok.Data;
import tourGuide.model.Location;

@Data
public class NearbyAttractionDTO {

    public String name;

    public Location attractionLocation;

    public Location userLocation;

    public double distance;

    public int rewardPoints;
}
