package org.spacelab.housingutilitiessystemadmin.models.filters.house;

import lombok.Data;

@Data
public class HouseRequestTable {
    
    private int page = 0;
    private int size = 10;
    
    private String houseNumber;
    private String streetName;
    private String cityName;
    private String chairmanFullName;
    private String status;
}

