package org.spacelab.housingutilitiessystemchairman.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.spacelab.housingutilitiessystemchairman.entity.Admin;
import org.spacelab.housingutilitiessystemchairman.repository.mongo.AdminRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    public Admin createAdmin(String email, String password) {
        Admin admin = new Admin();
        admin.setEmail(email);
        admin.setPassword(passwordEncoder.encode(password));
        admin.setEnabled(true);
        Admin savedAdmin = adminRepository.save(admin);
        log.info("Администратор {} успешно создан", email);
        return savedAdmin;
    }
    public Optional<Admin> findByEmail(String email) {
        return Optional.ofNullable(adminRepository.findByEmail(email));
    }
    public Optional<Admin> findById(String id) {
        return adminRepository.findById(id);
    }
}
