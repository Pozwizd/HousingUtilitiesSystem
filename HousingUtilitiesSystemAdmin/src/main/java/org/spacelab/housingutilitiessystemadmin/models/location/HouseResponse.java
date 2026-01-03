package org.spacelab.housingutilitiessystemadmin.models.location;

import lombok.Data;
import org.spacelab.housingutilitiessystemadmin.models.chairman.ChairmanResponse;

@Data
public class HouseResponse {
    private String id;
    private String number;
    private String houseNumber;
    private StreetResponse street;
    private ChairmanResponse chairman;
    private String status;
}
