package org.spacelab.housingutilitiessystemchairman.models.location;

import lombok.Data;

import java.io.Serializable;
@Data
public class HouseResponseTable implements Serializable {
    private String id;
    private String houseNumber;
    private String streetName;
    private String cityName;
    private String chairmanFullName;
    private String status;
}
