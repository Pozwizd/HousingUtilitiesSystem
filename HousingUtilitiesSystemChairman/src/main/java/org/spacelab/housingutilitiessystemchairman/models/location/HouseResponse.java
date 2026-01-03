package org.spacelab.housingutilitiessystemchairman.models.location;
import lombok.Data;
import org.spacelab.housingutilitiessystemchairman.models.chairman.ChairmanResponse;
@Data
public class HouseResponse {
    private String id;
    private String number;
    private String houseNumber;
    private StreetResponse street;
    private ChairmanResponse chairman;
    private String status;
}
