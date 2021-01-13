package tourGuide.mapper;

import org.javamoney.moneta.Money;
import tourGuide.DTO.UserPreferencesDTO;
import tourGuide.user.UserPreferences;

import javax.money.CurrencyUnit;
import javax.money.Monetary;

public class UserPreferencesMapper {

    /**
     * Get user preferences from controller and transform them into right application object
     * @param userPreferencesDTO pure text preferences
     * @return user preferences object
     */
    public UserPreferences mapPreferences (UserPreferencesDTO userPreferencesDTO){

        UserPreferences userPreferences = new UserPreferences();
        // set new currency if not use by user default
        CurrencyUnit currency;
        if (userPreferencesDTO.getCurrency() != null) {
            currency = Monetary.getCurrency(userPreferencesDTO.getCurrency());
            userPreferences.setCurrency(Monetary.getCurrency(userPreferencesDTO.getCurrency()));
        } else {
            currency = userPreferences.getCurrency();
        }

        //attractionProximity
        if (userPreferencesDTO.getAttractionProximity() != null) {
            userPreferences.setAttractionProximity(userPreferencesDTO.getAttractionProximity());
        }

        //lowerPricePoint
        if (userPreferencesDTO.getLowerPricePoint() != null) {
            userPreferences.setLowerPricePoint(Money.of(
                    userPreferencesDTO.getLowerPricePoint(),
                    currency));
        }

        //highPricePoint
        if (userPreferencesDTO.getHighPricePoint() != null) {
            userPreferences.setHighPricePoint(Money.of(
                    userPreferencesDTO.getHighPricePoint(),
                    currency));
        }

        // tripDuration
        if (userPreferencesDTO.getTripDuration() != null) {
            userPreferences.setTripDuration(userPreferencesDTO.getTripDuration());
        }

        // ticketQuantity
        if (userPreferencesDTO.getTicketQuantity() != null) {
            userPreferences.setTicketQuantity(userPreferencesDTO.getTicketQuantity());
        }

        // numberOfAdults
        if (userPreferencesDTO.getNumberOfAdults() != null) {
            userPreferences.setNumberOfAdults(userPreferencesDTO.getNumberOfAdults());
        }

        // numberOfChildren
        if (userPreferencesDTO.getNumberOfChildren() != null) {
            userPreferences.setNumberOfChildren(userPreferencesDTO.getNumberOfChildren());
        }

        return userPreferences;
    }

}
