package org.spacelab.housingutilitiessystemadmin.models.location;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class HouseRequest {
    
    private String id;
    
    @NotBlank(message = "House number is required")
    @Size(min = 1, max = 20, message = "House number must be between 1 and 20 characters")
    private String houseNumber;
    
    @NotBlank(message = "Street ID is required")
    private String streetId;
    
    private String chairmanId;
    
    @NotBlank(message = "Status is required")
    private String status;
}

