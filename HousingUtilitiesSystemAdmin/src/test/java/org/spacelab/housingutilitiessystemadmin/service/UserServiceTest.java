package org.spacelab.housingutilitiessystemadmin.service;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.spacelab.housingutilitiessystemadmin.entity.User;
import org.spacelab.housingutilitiessystemadmin.entity.location.City;
import org.spacelab.housingutilitiessystemadmin.entity.location.House;
import org.spacelab.housingutilitiessystemadmin.entity.location.Street;
import org.spacelab.housingutilitiessystemadmin.exception.OperationException;
import org.spacelab.housingutilitiessystemadmin.mappers.UserMapper;
import org.spacelab.housingutilitiessystemadmin.models.PageResponse;
import org.spacelab.housingutilitiessystemadmin.models.filters.user.UserRequestTable;
import org.spacelab.housingutilitiessystemadmin.models.user.UserRequest;
import org.spacelab.housingutilitiessystemadmin.models.user.UserResponse;
import org.spacelab.housingutilitiessystemadmin.models.user.UserResponseTable;
import org.spacelab.housingutilitiessystemadmin.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private FileService fileService;

    @Mock
    private CityService cityService;

    @Mock
    private StreetService streetService;

    @Mock
    private HouseService houseService;

    @Mock
    private MultipartFile multipartFile;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UserResponse testUserResponse;
    private UserRequest testUserRequest;
    private ObjectId testUserId;

    @BeforeEach
    void setUp() {
        testUserId = new ObjectId();
        testUser = new User();
        testUser.setId(testUserId.toHexString());
        testUser.setEmail("user@test.com");

        testUserResponse = new UserResponse();
        testUserResponse.setId(testUserId.toHexString());
        testUserResponse.setEmail("user@test.com");

        testUserRequest = new UserRequest();
        testUserRequest.setEmail("user@test.com");
    }

    @Nested
    @DisplayName("Basic CRUD Tests")
    class BasicCrudTests {
        @Test
        @DisplayName("Should find by id")
        void findById_shouldReturnUser() {
            when(userRepository.findById(any(ObjectId.class))).thenReturn(Optional.of(testUser));
            Optional<User> result = userService.findById(testUserId);
            assertThat(result).isPresent();
        }

        @Test
        @DisplayName("Should return empty when not found")
        void findById_shouldReturnEmpty() {
            when(userRepository.findById(any(ObjectId.class))).thenReturn(Optional.empty());
            Optional<User> result = userService.findById(testUserId);
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should save user")
        void save_shouldSaveUser() {
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            User result = userService.save(testUser);
            assertThat(result).isNotNull();
            verify(userRepository).save(testUser);
        }

        @Test
        @DisplayName("Should find all users")
        void findAll_shouldReturnAllUsers() {
            when(userRepository.findAll()).thenReturn(Arrays.asList(testUser, new User()));
            List<User> result = userService.findAll();
            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("Should delete by id")
        void deleteById_shouldDeleteUser() {
            doNothing().when(userRepository).deleteById(any(ObjectId.class));
            userService.deleteById(testUserId);
            verify(userRepository).deleteById(testUserId);
        }

        @Test
        @DisplayName("Should save all users")
        void saveAll_shouldSaveAllUsers() {
            List<User> users = Arrays.asList(testUser, new User());
            when(userRepository.saveAll(users)).thenReturn(users);
            List<User> result = userService.saveAll(users);
            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("Should delete all")
        void deleteAll_shouldDeleteAllUsers() {
            doNothing().when(userRepository).deleteAll();
            userService.deleteAll();
            verify(userRepository).deleteAll();
        }
    }

    @Nested
    @DisplayName("getUsersTable Tests")
    class GetUsersTableTests {
        @Test
        @DisplayName("Should get users table")
        void getUsersTable_shouldReturnTable() {
            UserRequestTable requestTable = new UserRequestTable();
            requestTable.setPage(0);
            requestTable.setSize(10);
            Page<User> usersPage = new PageImpl<>(List.of(testUser));
            PageResponse<UserResponseTable> pageResponse = new PageResponse<>();

            when(userRepository.findUsersWithFilters(requestTable)).thenReturn(usersPage);
            when(userMapper.toPageResponse(usersPage)).thenReturn(pageResponse);

            PageResponse<UserResponseTable> result = userService.getUsersTable(requestTable);

            assertThat(result).isNotNull();
        }
    }

    @Nested
    @DisplayName("getUserById Tests")
    class GetUserByIdTests {
        @Test
        @DisplayName("Should return user response when found")
        void getUserById_shouldReturnUserResponse() {
            when(userRepository.findById(any(ObjectId.class))).thenReturn(Optional.of(testUser));
            when(userMapper.mapUserToResponse(testUser)).thenReturn(testUserResponse);
            UserResponse result = userService.getUserById(testUserId);
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Should throw exception when not found")
        void getUserById_shouldThrowException_whenNotFound() {
            when(userRepository.findById(any(ObjectId.class))).thenReturn(Optional.empty());
            assertThatThrownBy(() -> userService.getUserById(testUserId))
                    .isInstanceOf(OperationException.class)
                    .hasMessageContaining("не найден");
        }
    }

    @Nested
    @DisplayName("createUser Tests")
    class CreateUserTests {
        @Test
        @DisplayName("Should create user successfully")
        void createUser_shouldCreateSuccessfully() {
            when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
            when(userMapper.toEntity(testUserRequest)).thenReturn(testUser);
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(userMapper.mapUserToResponse(testUser)).thenReturn(testUserResponse);

            UserResponse result = userService.createUser(testUserRequest);

            assertThat(result).isNotNull();
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("Should throw exception when email already exists")
        void createUser_shouldThrowException_whenEmailExists() {
            when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));

            assertThatThrownBy(() -> userService.createUser(testUserRequest))
                    .isInstanceOf(OperationException.class)
                    .hasMessageContaining("уже существует");
        }

        @Test
        @DisplayName("Should create user with city")
        void createUser_withCity_shouldSetCity() {
            ObjectId cityId = new ObjectId();
            City city = new City();
            city.setId(cityId.toHexString());
            testUserRequest.setCityId(cityId);

            when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
            when(userMapper.toEntity(testUserRequest)).thenReturn(testUser);
            when(cityService.findById(cityId)).thenReturn(Optional.of(city));
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(userMapper.mapUserToResponse(testUser)).thenReturn(testUserResponse);

            userService.createUser(testUserRequest);

            verify(cityService).findById(cityId);
        }

        @Test
        @DisplayName("Should throw exception when city not found")
        void createUser_shouldThrowException_whenCityNotFound() {
            ObjectId cityId = new ObjectId();
            testUserRequest.setCityId(cityId);

            when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
            when(userMapper.toEntity(testUserRequest)).thenReturn(testUser);
            when(cityService.findById(cityId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.createUser(testUserRequest))
                    .isInstanceOf(OperationException.class)
                    .hasMessageContaining("Город");
        }

        @Test
        @DisplayName("Should create user with street")
        void createUser_withStreet_shouldSetStreet() {
            ObjectId streetId = new ObjectId();
            Street street = new Street();
            street.setId(streetId.toHexString());
            testUserRequest.setAddressId(streetId);

            when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
            when(userMapper.toEntity(testUserRequest)).thenReturn(testUser);
            when(streetService.findById(streetId)).thenReturn(Optional.of(street));
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(userMapper.mapUserToResponse(testUser)).thenReturn(testUserResponse);

            userService.createUser(testUserRequest);

            verify(streetService).findById(streetId);
        }

        @Test
        @DisplayName("Should throw exception when street not found")
        void createUser_shouldThrowException_whenStreetNotFound() {
            ObjectId streetId = new ObjectId();
            testUserRequest.setAddressId(streetId);

            when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
            when(userMapper.toEntity(testUserRequest)).thenReturn(testUser);
            when(streetService.findById(streetId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.createUser(testUserRequest))
                    .isInstanceOf(OperationException.class)
                    .hasMessageContaining("Улица");
        }

        @Test
        @DisplayName("Should create user with house")
        void createUser_withHouse_shouldSetHouse() {
            House house = new House();
            ObjectId houseObjectId = new ObjectId();
            house.setId(houseObjectId.toHexString());
            house.setHouseNumber("123");
            testUserRequest.setHouseId(houseObjectId);

            when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
            when(userMapper.toEntity(testUserRequest)).thenReturn(testUser);
            when(houseService.findById(houseObjectId.toHexString())).thenReturn(Optional.of(house));
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(userMapper.mapUserToResponse(testUser)).thenReturn(testUserResponse);

            userService.createUser(testUserRequest);

            verify(houseService).findById(houseObjectId.toHexString());
        }

        @Test
        @DisplayName("Should throw exception when house not found")
        void createUser_shouldThrowException_whenHouseNotFound() {
            ObjectId houseId = new ObjectId();
            testUserRequest.setHouseId(houseId);

            when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
            when(userMapper.toEntity(testUserRequest)).thenReturn(testUser);
            when(houseService.findById(houseId.toHexString())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.createUser(testUserRequest))
                    .isInstanceOf(OperationException.class)
                    .hasMessageContaining("Дом");
        }

        @Test
        @DisplayName("Should create user with status")
        void createUser_withStatus_shouldSetStatus() {
            testUserRequest.setStatus("ACTIVE");

            when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
            when(userMapper.toEntity(testUserRequest)).thenReturn(testUser);
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(userMapper.mapUserToResponse(testUser)).thenReturn(testUserResponse);

            userService.createUser(testUserRequest);

            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("Should throw exception when invalid status")
        void createUser_shouldThrowException_whenInvalidStatus() {
            testUserRequest.setStatus("INVALID_STATUS");

            when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
            when(userMapper.toEntity(testUserRequest)).thenReturn(testUser);

            assertThatThrownBy(() -> userService.createUser(testUserRequest))
                    .isInstanceOf(OperationException.class)
                    .hasMessageContaining("Неверный статус");
        }

        @Test
        @DisplayName("Should create user with photo")
        void createUser_withPhoto_shouldUploadPhoto() throws IOException {
            testUserRequest.setPhotoFile(multipartFile);
            when(multipartFile.isEmpty()).thenReturn(false);

            when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
            when(userMapper.toEntity(testUserRequest)).thenReturn(testUser);
            when(fileService.uploadFile(multipartFile)).thenReturn("uploads/photo.jpg");
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(userMapper.mapUserToResponse(testUser)).thenReturn(testUserResponse);

            userService.createUser(testUserRequest);

            verify(fileService).uploadFile(multipartFile);
        }

        @Test
        @DisplayName("Should throw exception when photo upload fails")
        void createUser_shouldThrowException_whenPhotoUploadFails() throws IOException {
            testUserRequest.setPhotoFile(multipartFile);
            when(multipartFile.isEmpty()).thenReturn(false);

            when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
            when(userMapper.toEntity(testUserRequest)).thenReturn(testUser);
            when(fileService.uploadFile(multipartFile)).thenThrow(new IOException("Upload failed"));

            assertThatThrownBy(() -> userService.createUser(testUserRequest))
                    .isInstanceOf(OperationException.class)
                    .hasMessageContaining("Ошибка при загрузке фото");
        }
    }

    @Nested
    @DisplayName("updateUser Tests")
    class UpdateUserTests {
        @Test
        @DisplayName("Should update user successfully")
        void updateUser_shouldUpdateSuccessfully() {
            when(userRepository.findById(any(ObjectId.class))).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(userMapper.mapUserToResponse(testUser)).thenReturn(testUserResponse);

            UserResponse result = userService.updateUser(testUserId, testUserRequest);

            assertThat(result).isNotNull();
            verify(userMapper).partialUpdate(testUserRequest, testUser);
        }

        @Test
        @DisplayName("Should throw exception when not found")
        void updateUser_shouldThrowException_whenNotFound() {
            when(userRepository.findById(any(ObjectId.class))).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.updateUser(testUserId, testUserRequest))
                    .isInstanceOf(OperationException.class)
                    .hasMessageContaining("не найден");
        }

        @Test
        @DisplayName("Should update user with new photo and delete old")
        void updateUser_withNewPhoto_shouldDeleteOldAndUploadNew() throws IOException {
            testUser.setPhoto("old-photo.jpg");
            testUserRequest.setPhotoFile(multipartFile);
            when(multipartFile.isEmpty()).thenReturn(false);

            when(userRepository.findById(any(ObjectId.class))).thenReturn(Optional.of(testUser));
            when(fileService.uploadFile(multipartFile)).thenReturn("new-photo.jpg");
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(userMapper.mapUserToResponse(testUser)).thenReturn(testUserResponse);

            userService.updateUser(testUserId, testUserRequest);

            verify(fileService).deleteFile("old-photo.jpg");
            verify(fileService).uploadFile(multipartFile);
        }

        @Test
        @DisplayName("Should throw exception when photo upload fails during update")
        void updateUser_shouldThrowException_whenPhotoUploadFails() throws IOException {
            testUserRequest.setPhotoFile(multipartFile);
            when(multipartFile.isEmpty()).thenReturn(false);

            when(userRepository.findById(any(ObjectId.class))).thenReturn(Optional.of(testUser));
            when(fileService.uploadFile(multipartFile)).thenThrow(new IOException("Upload failed"));

            assertThatThrownBy(() -> userService.updateUser(testUserId, testUserRequest))
                    .isInstanceOf(OperationException.class)
                    .hasMessageContaining("Ошибка при загрузке фото");
        }

        @Test
        @DisplayName("Should update user with city")
        void updateUser_withCity_shouldSetCity() {
            City city = new City();
            ObjectId cityId = new ObjectId();
            city.setId(cityId.toHexString());
            testUserRequest.setCityId(cityId);

            when(userRepository.findById(any(ObjectId.class))).thenReturn(Optional.of(testUser));
            when(cityService.findById(cityId)).thenReturn(Optional.of(city));
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(userMapper.mapUserToResponse(testUser)).thenReturn(testUserResponse);

            userService.updateUser(testUserId, testUserRequest);

            verify(cityService).findById(cityId);
        }

        @Test
        @DisplayName("Should throw exception when city not found during update")
        void updateUser_shouldThrowException_whenCityNotFound() {
            ObjectId cityId = new ObjectId();
            testUserRequest.setCityId(cityId);

            when(userRepository.findById(any(ObjectId.class))).thenReturn(Optional.of(testUser));
            when(cityService.findById(cityId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.updateUser(testUserId, testUserRequest))
                    .isInstanceOf(OperationException.class)
                    .hasMessageContaining("Город");
        }

        @Test
        @DisplayName("Should update user with street")
        void updateUser_withStreet_shouldSetStreet() {
            Street street = new Street();
            ObjectId streetId = new ObjectId();
            street.setId(streetId.toHexString());
            testUserRequest.setAddressId(streetId);

            when(userRepository.findById(any(ObjectId.class))).thenReturn(Optional.of(testUser));
            when(streetService.findById(streetId)).thenReturn(Optional.of(street));
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(userMapper.mapUserToResponse(testUser)).thenReturn(testUserResponse);

            userService.updateUser(testUserId, testUserRequest);

            verify(streetService).findById(streetId);
        }

        @Test
        @DisplayName("Should throw exception when street not found during update")
        void updateUser_shouldThrowException_whenStreetNotFound() {
            ObjectId streetId = new ObjectId();
            testUserRequest.setAddressId(streetId);

            when(userRepository.findById(any(ObjectId.class))).thenReturn(Optional.of(testUser));
            when(streetService.findById(streetId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.updateUser(testUserId, testUserRequest))
                    .isInstanceOf(OperationException.class)
                    .hasMessageContaining("Улица");
        }

        @Test
        @DisplayName("Should update user with house")
        void updateUser_withHouse_shouldSetHouse() {
            House house = new House();
            ObjectId houseId = new ObjectId();
            house.setId(houseId.toHexString());
            house.setHouseNumber("123");
            testUserRequest.setHouseId(houseId);

            when(userRepository.findById(any(ObjectId.class))).thenReturn(Optional.of(testUser));
            when(houseService.findById(houseId.toHexString())).thenReturn(Optional.of(house));
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(userMapper.mapUserToResponse(testUser)).thenReturn(testUserResponse);

            userService.updateUser(testUserId, testUserRequest);

            verify(houseService).findById(houseId.toHexString());
        }

        @Test
        @DisplayName("Should throw exception when house not found during update")
        void updateUser_shouldThrowException_whenHouseNotFound() {
            ObjectId houseId = new ObjectId();
            testUserRequest.setHouseId(houseId);

            when(userRepository.findById(any(ObjectId.class))).thenReturn(Optional.of(testUser));
            when(houseService.findById(houseId.toHexString())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.updateUser(testUserId, testUserRequest))
                    .isInstanceOf(OperationException.class)
                    .hasMessageContaining("Дом");
        }

        @Test
        @DisplayName("Should update user with valid status")
        void updateUser_withStatus_shouldSetStatus() {
            testUserRequest.setStatus("ACTIVE");

            when(userRepository.findById(any(ObjectId.class))).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(userMapper.mapUserToResponse(testUser)).thenReturn(testUserResponse);

            userService.updateUser(testUserId, testUserRequest);

            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("Should throw exception when invalid status during update")
        void updateUser_shouldThrowException_whenInvalidStatus() {
            testUserRequest.setStatus("INVALID_STATUS");

            when(userRepository.findById(any(ObjectId.class))).thenReturn(Optional.of(testUser));

            assertThatThrownBy(() -> userService.updateUser(testUserId, testUserRequest))
                    .isInstanceOf(OperationException.class)
                    .hasMessageContaining("Неверный статус");
        }

        @Test
        @DisplayName("Should update photo without old photo to delete")
        void updateUser_withPhotoNoOldPhoto_shouldJustUpload() throws IOException {
            testUser.setPhoto(null);
            testUserRequest.setPhotoFile(multipartFile);
            when(multipartFile.isEmpty()).thenReturn(false);

            when(userRepository.findById(any(ObjectId.class))).thenReturn(Optional.of(testUser));
            when(fileService.uploadFile(multipartFile)).thenReturn("new-photo.jpg");
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(userMapper.mapUserToResponse(testUser)).thenReturn(testUserResponse);

            userService.updateUser(testUserId, testUserRequest);

            verify(fileService, never()).deleteFile(anyString());
            verify(fileService).uploadFile(multipartFile);
        }

        @Test
        @DisplayName("Should update photo with empty old photo path")
        void updateUser_withPhotoEmptyOldPath_shouldJustUpload() throws IOException {
            testUser.setPhoto("");
            testUserRequest.setPhotoFile(multipartFile);
            when(multipartFile.isEmpty()).thenReturn(false);

            when(userRepository.findById(any(ObjectId.class))).thenReturn(Optional.of(testUser));
            when(fileService.uploadFile(multipartFile)).thenReturn("new-photo.jpg");
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(userMapper.mapUserToResponse(testUser)).thenReturn(testUserResponse);

            userService.updateUser(testUserId, testUserRequest);

            verify(fileService, never()).deleteFile(anyString());
            verify(fileService).uploadFile(multipartFile);
        }
    }

    @Nested
    @DisplayName("deleteUser Tests")
    class DeleteUserTests {
        @Test
        @DisplayName("Should delete user successfully")
        void deleteUser_shouldDeleteSuccessfully() {
            when(userRepository.existsById(any(ObjectId.class))).thenReturn(true);
            doNothing().when(userRepository).deleteById(any(ObjectId.class));

            boolean result = userService.deleteUser(testUserId);

            assertThat(result).isTrue();
            verify(userRepository).deleteById(testUserId);
        }

        @Test
        @DisplayName("Should throw exception when not found")
        void deleteUser_shouldThrowException_whenNotFound() {
            when(userRepository.existsById(any(ObjectId.class))).thenReturn(false);

            assertThatThrownBy(() -> userService.deleteUser(testUserId))
                    .isInstanceOf(OperationException.class)
                    .hasMessageContaining("не найден");
        }
    }

    @Nested
    @DisplayName("getUsersByHouseId Tests")
    class GetUsersByHouseIdTests {
        @Test
        @DisplayName("Should get users by house id")
        void getUsersByHouseId_shouldReturnUsers() {
            House house = new House();
            house.setId(new ObjectId().toHexString());

            when(houseService.findById("house-id")).thenReturn(Optional.of(house));
            when(userRepository.findByHouse(house)).thenReturn(List.of(testUser));
            when(userMapper.mapUserToResponse(testUser)).thenReturn(testUserResponse);

            List<UserResponse> result = userService.getUsersByHouseId("house-id");

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Should throw exception when house not found")
        void getUsersByHouseId_shouldThrowException_whenHouseNotFound() {
            when(houseService.findById("nonexistent")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.getUsersByHouseId("nonexistent"))
                    .isInstanceOf(OperationException.class)
                    .hasMessageContaining("не найден");
        }
    }

    @Nested
    @DisplayName("getUsersByHouseIdPaginated Tests")
    class GetUsersByHouseIdPaginatedTests {
        @Test
        @DisplayName("Should get users by house id paginated")
        void getUsersByHouseIdPaginated_shouldReturnPage() {
            House house = new House();
            house.setId(new ObjectId().toHexString());
            Page<User> usersPage = new PageImpl<>(List.of(testUser), PageRequest.of(0, 10), 1);

            when(houseService.findById("house-id")).thenReturn(Optional.of(house));
            when(userRepository.findByHouse(eq(house), any())).thenReturn(usersPage);
            when(userMapper.mapUserToResponse(testUser)).thenReturn(testUserResponse);

            Page<UserResponse> result = userService.getUsersByHouseIdPaginated("house-id", 0, 10);

            assertThat(result).isNotNull();
            assertThat(result.getTotalElements()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should throw exception when house not found for paginated")
        void getUsersByHouseIdPaginated_shouldThrowException_whenHouseNotFound() {
            when(houseService.findById("nonexistent")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.getUsersByHouseIdPaginated("nonexistent", 0, 10))
                    .isInstanceOf(OperationException.class)
                    .hasMessageContaining("не найден");
        }
    }
}
