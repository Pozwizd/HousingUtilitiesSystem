package org.spacelab.housingutilitiessystemadmin.config;

import lombok.RequiredArgsConstructor;
import net.datafaker.Faker;
import org.spacelab.housingutilitiessystemadmin.entity.*;
import org.spacelab.housingutilitiessystemadmin.entity.location.*;
import org.spacelab.housingutilitiessystemadmin.repository.ChairmanRepository;
import org.spacelab.housingutilitiessystemadmin.repository.UserRepository;
import org.spacelab.housingutilitiessystemadmin.service.*;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * DataLoader - загружает базовые тестовые данные для Admin модуля.
 * Базовые данные: Admin, адресная структура (Region → City → Street → House),
 * Chairman, Users.
 *
 * Логика проверок:
 * - Точные данные (Admin, Chairman): проверка по email, если существует - пропускаем
 * - Случайные данные (Users): если count > 20 - пропускаем
 */
// DISABLED: Компонент отключен чтобы база данных не очищалась при запуске приложения
@Component
@RequiredArgsConstructor
public class DataLoader {

    private static final int MAX_RANDOM_RECORDS = 20;

    private final Faker faker;

    private final AdminService adminService;
    private final UserService userService;
    private final RegionService regionService;
    private final ChairmanService chairmanService;
    private final ChairmanRepository chairmanRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MongoTemplate mongoTemplate;

    @EventListener(ApplicationReadyEvent.class)
    public void loadData() {
        System.out.println("=== Admin DataLoader: Начало загрузки данных ===");

        // 1. Создаем админа (проверка по email)
        loadAdmin();

        // 2. Создаем адресную структуру
        Region region = loadRegion();
        City city = loadCity(region);
        Street street = loadStreet(city);
        House house = loadHouse(street);

        // 3. Создаем председателя (проверка по email)
        Chairman chairman = loadChairman(house);

        // 4. Создаем тестовых пользователей (проверка на количество)
        List<User> newUsers = loadUsers(house);

        // 5. Обновляем дом с жителями и председателем
        if (house != null) {
            // Добавляем новых пользователей к существующим (не перезаписываем)
            List<User> currentResidents = house.getResidents() != null ? new ArrayList<>(house.getResidents()) : new ArrayList<>();
            for (User newUser : newUsers) {
                if (newUser != null && currentResidents.stream().noneMatch(u -> u.getId().equals(newUser.getId()))) {
                    currentResidents.add(newUser);
                }
            }
            house.setResidents(currentResidents);
            
            if (chairman != null) {
                house.setChairman(chairman);
            }
            mongoTemplate.save(house);
        }

        System.out.println("=== Admin DataLoader: Загрузка завершена! ===");
    }

    // ============= ADMIN =============

    private void loadAdmin() {
        String adminEmail = "admin@gmail.com";
        // Проверяем существование админа (findByEmail возвращает Admin, не Optional)
        Admin existingAdmin = adminService.findByEmail(adminEmail);
        if (existingAdmin != null) {
            System.out.println("Admin уже существует: " + adminEmail + " - пропускаем");
            return;
        }
        adminService.createAdmin(adminEmail, adminEmail);
        System.out.println("Создан Admin: " + adminEmail);
    }

    // ============= АДРЕСНАЯ СТРУКТУРА =============

    private Region loadRegion() {
        String regionName = "Київська область";
        List<Region> existingRegions = regionService.findAll();
        for (Region r : existingRegions) {
            if (regionName.equals(r.getName())) {
                System.out.println("Region уже существует: " + regionName + " - пропускаем");
                return r;
            }
        }

        Region region = new Region();
        region.setName(regionName);
        region.setCities(new ArrayList<>());
        Region saved = mongoTemplate.save(region);
        System.out.println("Создан Region: " + regionName);
        return saved;
    }

    private City loadCity(Region region) {
        String cityName = "Київ";
        if (region.getCities() != null) {
            for (City c : region.getCities()) {
                if (cityName.equals(c.getName())) {
                    System.out.println("City уже существует: " + cityName + " - пропускаем");
                    return c;
                }
            }
        }

        City city = new City();
        city.setName(cityName);
        city.setRegion(region);
        city.setStreets(new ArrayList<>());
        City saved = mongoTemplate.save(city);

        if (region.getCities() == null) {
            region.setCities(new ArrayList<>());
        }
        region.getCities().add(saved);
        mongoTemplate.save(region);

        System.out.println("Создан City: " + cityName);
        return saved;
    }

    private Street loadStreet(City city) {
        String streetName = "вул. Хрещатик";
        if (city.getStreets() != null) {
            for (Street s : city.getStreets()) {
                if (streetName.equals(s.getName())) {
                    System.out.println("Street уже существует: " + streetName + " - пропускаем");
                    return s;
                }
            }
        }

        Street street = new Street();
        street.setName(streetName);
        street.setCity(city);
        street.setHouses(new ArrayList<>());
        Street saved = mongoTemplate.save(street);

        if (city.getStreets() == null) {
            city.setStreets(new ArrayList<>());
        }
        city.getStreets().add(saved);
        mongoTemplate.save(city);

        System.out.println("Создан Street: " + streetName);
        return saved;
    }

    private House loadHouse(Street street) {
        String houseNumber = "15";
        if (street.getHouses() != null) {
            for (House h : street.getHouses()) {
                if (houseNumber.equals(h.getHouseNumber())) {
                    System.out.println("House уже существует: " + houseNumber + " - пропускаем");
                    return h;
                }
            }
        }

        House house = new House();
        house.setHouseNumber(houseNumber);
        house.setStreet(street);
        house.setStatus(Status.ACTIVE);
        house.setResidents(new ArrayList<>());
        House saved = mongoTemplate.save(house);

        if (street.getHouses() == null) {
            street.setHouses(new ArrayList<>());
        }
        street.getHouses().add(saved);
        mongoTemplate.save(street);

        System.out.println("Создан House: " + houseNumber);
        return saved;
    }

    // ============= ПРЕДСЕДАТЕЛЬ =============

    private Chairman loadChairman(House house) {
        String chairmanEmail = "chairmen@gmail.com";

        // Проверяем существование председателя по email (используем repository)
        Optional<Chairman> existing = chairmanRepository.findByEmail(chairmanEmail);
        if (existing.isPresent()) {
            System.out.println("Chairman уже существует: " + chairmanEmail + " - пропускаем");
            return existing.get();
        }

        Chairman chairman = Chairman.builder()
                .id(UUID.randomUUID().toString())
                .lastName("Петренко")
                .firstName("Іван")
                .middleName("Сергійович")
                .phone("+380501234567")
                .login("Chairman")
                .email(chairmanEmail)
                .password(passwordEncoder.encode(chairmanEmail))
                .status(Status.ACTIVE)
                .photo("uploads/avatar.jpg")
                .house(house)
                .enabled(true)
                .role(Role.USER)
                .build();

        Chairman saved = mongoTemplate.save(chairman);
        System.out.println("Создан Chairman: " + chairmanEmail);
        return saved;
    }

    // ============= ПОЛЬЗОВАТЕЛИ =============

    private List<User> loadUsers(House house) {
        List<User> existingUsers = userService.findAll();

        // Если пользователей больше MAX_RANDOM_RECORDS - не добавляем новых
        if (existingUsers.size() >= MAX_RANDOM_RECORDS) {
            System.out.println("Users: количество записей (" + existingUsers.size() + ") >= " + MAX_RANDOM_RECORDS + " - пропускаем");
            return existingUsers;
        }

        List<User> users = new ArrayList<>();

        // Создаем конкретных тестовых пользователей (проверка по email)
        users.add(createUserIfNotExists("user@gmail.com", "Олександр", "Коваленко", "Петрович",
                "+380991234567", "10", 56.0, house));
        users.add(createUserIfNotExists("user2@gmail.com", "Марія", "Шевченко", "Іванівна",
                "+380992345678", "11", 48.5, house));
        users.add(createUserIfNotExists("user3@gmail.com", "Андрій", "Бондаренко", "Олегович",
                "+380993456789", "15", 72.3, house));
        users.add(createUserIfNotExists("user4@gmail.com", "Наталія", "Мельник", "Василівна",
                "+380994567890", "22", 65.0, house));
        users.add(createUserIfNotExists("user5@gmail.com", "Віталій", "Лисенко", "Андрійович",
                "+380995678901", "33", 88.2, house));
        users.add(createUserIfNotExists("user6@gmail.com", "Олена", "Ткаченко", "Миколаївна",
                "+380996789012", "45", 42.5, house));
        users.add(createUserIfNotExists("user7@gmail.com", "Дмитро", "Кравченко", "Ігорович",
                "+380997890123", "56", 95.0, house));
        users.add(createUserIfNotExists("user8@gmail.com", "Світлана", "Савченко", "Олексіївна",
                "+380998901234", "67", 55.5, house));

        // Убираем null значения (если пользователь уже существовал)
        users.removeIf(Objects::isNull);

        System.out.println("Создано новых пользователей: " + users.size());
        return users;
    }

    private User createUserIfNotExists(String email, String firstName, String lastName, String middleName,
                                        String phone, String apartment, Double area, House house) {
        // Проверяем существование по email (используем repository)
        Optional<User> existing = userRepository.findByEmail(email);
        if (existing.isPresent()) {
            System.out.println("  User уже существует: " + email + " - пропускаем");
            return null;
        }

        User user = new User();
        user.setEmail(email);
        user.setLogin(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setMiddleName(middleName);
        user.setPhone(phone);
        user.setApartmentNumber(apartment);
        user.setApartmentArea(area);
        user.setAccountNumber(generateAccountNumber());
        user.setStatus(Status.ACTIVE);
        user.setPassword(passwordEncoder.encode(email));
        user.setPhoto("uploads/avatar.jpg");
        user.setHouse(house);
        user.setStreet(house.getStreet());
        user.setCity(house.getStreet().getCity());

        User saved = mongoTemplate.save(user);
        System.out.println("  Создан User: " + email);
        return saved;
    }

    private String generateAccountNumber() {
        return String.format("%04d-%04d-%04d-%04d",
                faker.number().numberBetween(0, 9999),
                faker.number().numberBetween(0, 9999),
                faker.number().numberBetween(0, 9999),
                faker.number().numberBetween(0, 9999));
    }
}
