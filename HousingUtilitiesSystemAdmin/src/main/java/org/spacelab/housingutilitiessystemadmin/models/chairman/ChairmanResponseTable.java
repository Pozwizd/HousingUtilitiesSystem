package org.spacelab.housingutilitiessystemadmin.models.chairman;

import lombok.Data;
import org.spacelab.housingutilitiessystemadmin.entity.Chairman;

import java.io.Serializable;

/**
 * ChairmanResponseTable for {@link Chairman}
 */
@Data
public class ChairmanResponseTable implements Serializable {
    private String id;
    private String fullName;
    private String phone;
    private String email;
    private String login;
    private String status;
}

