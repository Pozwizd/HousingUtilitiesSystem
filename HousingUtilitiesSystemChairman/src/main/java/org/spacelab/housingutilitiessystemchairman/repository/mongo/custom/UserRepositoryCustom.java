package org.spacelab.housingutilitiessystemchairman.repository.mongo.custom;
import org.spacelab.housingutilitiessystemchairman.entity.User;
import org.spacelab.housingutilitiessystemchairman.models.filters.user.UserRequestTable;
import org.springframework.data.domain.Page;
public interface UserRepositoryCustom {
    Page<User> findUsersWithFilters(UserRequestTable userRequestTable);
}
