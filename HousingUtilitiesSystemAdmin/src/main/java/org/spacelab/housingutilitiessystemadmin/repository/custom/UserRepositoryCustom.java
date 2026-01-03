package org.spacelab.housingutilitiessystemadmin.repository.custom;

import org.spacelab.housingutilitiessystemadmin.entity.User;
import org.spacelab.housingutilitiessystemadmin.models.filters.user.UserRequestTable;
import org.springframework.data.domain.Page;

public interface UserRepositoryCustom {
    Page<User> findUsersWithFilters(UserRequestTable userRequestTable);
}
