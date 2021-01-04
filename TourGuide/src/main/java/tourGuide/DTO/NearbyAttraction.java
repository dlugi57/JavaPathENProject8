package tourGuide.DTO;
import gpsUtil.location.Location;
import lombok.Data;

@Data
public class NearbyAttraction {

    public String name;

    public Location attractionLocation;

    public Location userLocation;

    public double distance;

    public int rewardPoints;
}
