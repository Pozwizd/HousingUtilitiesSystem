package org.spacelab.housingutilitiessystemchairman.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.spacelab.housingutilitiessystemchairman.entity.User;
import org.spacelab.housingutilitiessystemchairman.entity.location.City;
import org.spacelab.housingutilitiessystemchairman.entity.location.House;
import org.spacelab.housingutilitiessystemchairman.entity.location.Status;
import org.spacelab.housingutilitiessystemchairman.entity.location.Street;
import org.spacelab.housingutilitiessystemchairman.exception.OperationException;
import org.spacelab.housingutilitiessystemchairman.mappers.UserMapper;
import org.spacelab.housingutilitiessystemchairman.models.PageResponse;
import org.spacelab.housingutilitiessystemchairman.models.filters.user.UserRequestTable;
import org.spacelab.housingutilitiessystemchairman.models.user.UserRequest;
import org.spacelab.housingutilitiessystemchairman.models.user.UserResponse;
import org.spacelab.housingutilitiessystemchairman.models.user.UserResponseTable;
import org.spacelab.housingutilitiessystemchairman.repository.mongo.UserRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final FileService fileService;
    private final CityService cityService;
    private final StreetService streetService;
    private final HouseService houseService;
    public PageResponse<UserResponseTable> getUsersTable(UserRequestTable userRequestTable) {
        Page<User> users = getUser(userRequestTable);
        PageResponse<UserResponseTable> userResponses = userMapper.toPageResponse(users);
        log.info("Получена страница пользователей: страница {}, размер {}", userRequestTable.getPage(), userRequestTable.getSize());
        return userResponses;
    }
    public Page<User> getUser(UserRequestTable userRequestTable) {
        return userRepository.findUsersWithFilters(userRequestTable);
    }
    public Optional<User> findById(ObjectId id) {
        return userRepository.findById(id);
    }
    public User save(User user) {
        return userRepository.save(user);
    }
    public List<User> findAll() {
        return userRepository.findAll();
    }
    public void deleteById(ObjectId id) {
        userRepository.deleteById(id);
    }
    public List<User> saveAll(List<User> users) {
        return userRepository.saveAll(users);
    }
    public void deleteAll() {
        userRepository.deleteAll();
    }
    @Cacheable(value = "users", key = "#id.toHexString()")
    public UserResponse getUserById(ObjectId id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new OperationException("получении пользователя",
                        "Пользователь с ID " + id + " не найден", HttpStatus.NOT_FOUND));
        log.info("Пользователь с ID {} успешно получен из БД", id);
        return userMapper.mapUserToResponse(user);
    }
    @CacheEvict(value = "users", allEntries = true)
    public UserResponse createUser(UserRequest userRequest) {
        Optional<User> existingUser = userRepository.findByEmail(userRequest.getEmail());
        if (existingUser.isPresent()) {
            throw new OperationException("создании пользователя",
                    "Пользователь с email " + userRequest.getEmail() + " уже существует", HttpStatus.CONFLICT);
        }
        User user = userMapper.toEntity(userRequest);
        if (userRequest.getCityId() != null) {
            City city = cityService.findById(userRequest.getCityId())
                    .orElseThrow(() -> new OperationException("получении города",
                            "Город с ID " + userRequest.getCityId() + " не найден", HttpStatus.NOT_FOUND));
            user.setCity(city);
        }
        if (userRequest.getAddressId() != null) {
            Street street = streetService.findById(userRequest.getAddressId())
                    .orElseThrow(() -> new OperationException("получении улицы",
                            "Улица с ID " + userRequest.getAddressId() + " не найдена", HttpStatus.NOT_FOUND));
            user.setStreet(street);
        }
        if (userRequest.getHouseId() != null) {
            var house = houseService.findById(userRequest.getHouseId().toHexString())
                    .orElseThrow(() -> new OperationException("получении дома",
                            "Дом с ID " + userRequest.getHouseId() + " не найден", HttpStatus.NOT_FOUND));
            user.setHouse(house);
            user.setHouseNumber(house.getHouseNumber());
        }
        if (userRequest.getStatus() != null) {
            try {
                user.setStatus(Status.valueOf(userRequest.getStatus()));
            } catch (IllegalArgumentException e) {
                throw new OperationException("создании пользователя",
                        "Неверный статус: " + userRequest.getStatus(), HttpStatus.BAD_REQUEST);
            }
        }
        if (userRequest.getPhotoFile() != null && !userRequest.getPhotoFile().isEmpty()) {
            try {
                String photoPath = fileService.uploadFile(userRequest.getPhotoFile());
                user.setPhoto(photoPath);
                log.info("Фото для пользователя успешно загружено: {}", photoPath);
            } catch (Exception e) {
                log.error("Ошибка при загрузке фото пользователя", e);
                throw new OperationException("создании пользователя",
                        "Ошибка при загрузке фото: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        User savedUser = userRepository.save(user);
        log.info("Пользователь с ID {} успешно создан", savedUser.getId());
        return userMapper.mapUserToResponse(savedUser);
    }
    @CachePut(value = "users", key = "#id.toHexString()")
    public UserResponse updateUser(ObjectId id, UserRequest userRequest) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new OperationException("обновлении пользователя",
                        "Пользователь с ID " + id + " не найден"));
        userMapper.partialUpdate(userRequest, user);
        if (userRequest.getCityId() != null) {
            City city = cityService.findById(userRequest.getCityId())
                    .orElseThrow(() -> new OperationException("получении города",
                            "Город с ID " + userRequest.getCityId() + " не найден", HttpStatus.NOT_FOUND));
            user.setCity(city);
        }
        if (userRequest.getAddressId() != null) {
            Street street = streetService.findById(userRequest.getAddressId())
                    .orElseThrow(() -> new OperationException("получении улицы",
                            "Улица с ID " + userRequest.getAddressId() + " не найдена", HttpStatus.NOT_FOUND));
            user.setStreet(street);
        }
        if (userRequest.getHouseId() != null) {
            var house = houseService.findById(userRequest.getHouseId().toHexString())
                    .orElseThrow(() -> new OperationException("получении дома",
                            "Дом с ID " + userRequest.getHouseId() + " не найден", HttpStatus.NOT_FOUND));
            user.setHouse(house);
            user.setHouseNumber(house.getHouseNumber());
        }
        if (userRequest.getStatus() != null) {
            try {
                user.setStatus(Status.valueOf(userRequest.getStatus()));
            } catch (IllegalArgumentException e) {
                throw new OperationException("обновлении пользователя",
                        "Неверный статус: " + userRequest.getStatus(), HttpStatus.BAD_REQUEST);
            }
        }
        if (userRequest.getPhotoFile() != null && !userRequest.getPhotoFile().isEmpty()) {
            try {
                if (user.getPhoto() != null && !user.getPhoto().isEmpty()) {
                    fileService.deleteFile(user.getPhoto());
                }
                String photoPath = fileService.uploadFile(userRequest.getPhotoFile());
                user.setPhoto(photoPath);
                log.info("Фото для пользователя с ID {} успешно обновлено: {}", id, photoPath);
            } catch (Exception e) {
                log.error("Ошибка при обновлении фото пользователя с ID {}", id, e);
                throw new OperationException("обновлении пользователя",
                        "Ошибка при загрузке фото: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        User updatedUser = userRepository.save(user);
        log.info("Пользователь с ID {} успешно обновлен", updatedUser.getId());
        return userMapper.mapUserToResponse(updatedUser);
    }
    @CacheEvict(value = "users", key = "#id.toHexString()")
    public boolean deleteUser(ObjectId id) {
        if (!userRepository.existsById(id)) {
            throw new OperationException("удалении пользователя",
                    "Пользователь с ID " + id + " не найден");
        }
        userRepository.deleteById(id);
        log.info("Пользователь с ID {} успешно удален", id);
        return true;
    }
    public List<UserResponse> getUsersByHouseId(String houseId) {
        House house = houseService.findById(houseId)
                .orElseThrow(() -> new OperationException("получении пользователей",
                        "Дом с ID " + houseId + " не найден", HttpStatus.NOT_FOUND));
        List<User> users = userRepository.findByHouse(house);
        log.info("Найдено {} пользователей для дома с ID {}", users.size(), houseId);
        return users.stream()
                .map(userMapper::mapUserToResponse)
                .toList();
    }
    public Page<UserResponse> getUsersByHouseIdPaginated(String houseId, int page, int size) {
        House house = houseService.findById(houseId)
                .orElseThrow(() -> new OperationException("получении пользователей",
                        "Дом с ID " + houseId + " не найден", HttpStatus.NOT_FOUND));
        org.springframework.data.domain.Pageable pageable =
                org.springframework.data.domain.PageRequest.of(page, size);
        Page<User> users = userRepository.findByHouse(house, pageable);
        log.info("Найдено {} пользователей для дома с ID {} (страница {}, размер {})",
                users.getNumberOfElements(), houseId, page, size);
        return users.map(userMapper::mapUserToResponse);
    }
}
