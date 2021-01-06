package tourGuide.DTO;

import lombok.Data;

@Data
public class UserPreferencesDTO {

    private Integer attractionProximity;
    private String currency;
    private Integer lowerPricePoint;
    private Integer highPricePoint;
    private Integer tripDuration;
    private Integer ticketQuantity;
    private Integer numberOfAdults;
    private Integer numberOfChildren;

}
