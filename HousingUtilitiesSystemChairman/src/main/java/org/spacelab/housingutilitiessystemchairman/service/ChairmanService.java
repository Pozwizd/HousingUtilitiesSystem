package org.spacelab.housingutilitiessystemchairman.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.spacelab.housingutilitiessystemchairman.entity.Chairman;
import org.spacelab.housingutilitiessystemchairman.repository.mongo.ChairmanRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
@Service
@RequiredArgsConstructor
@Slf4j
public class ChairmanService {
    private final ChairmanRepository chairmanRepository;
    private final PasswordEncoder passwordEncoder;
    public Chairman createChairman(String email, String password) {
        Chairman chairman = new Chairman();
        chairman.setEmail(email);
        chairman.setLogin(email);
        chairman.setPassword(passwordEncoder.encode(password));
        chairman.setEnabled(true);
        return chairmanRepository.save(chairman);
    }
    public Chairman createChairman(String email, String password, String firstName, String lastName) {
        Chairman chairman = new Chairman();
        chairman.setEmail(email);
        chairman.setLogin(email);
        chairman.setFirstName(firstName);
        chairman.setLastName(lastName);
        chairman.setPassword(passwordEncoder.encode(password));
        chairman.setEnabled(true);
        return chairmanRepository.save(chairman);
    }
    public Optional<Chairman> findByEmail(String email) {
        return chairmanRepository.findByEmail(email);
    }
    public Optional<Chairman> findByLogin(String login) {
        return chairmanRepository.findByLogin(login);
    }
    public Optional<Chairman> findById(String id) {
        return chairmanRepository.findById(id);
    }
    public boolean checkPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
    public Chairman save(Chairman chairman) {
        return chairmanRepository.save(chairman);
    }
    public Iterable<Chairman> saveAll(Iterable<Chairman> chairmen) {
        return chairmanRepository.saveAll(chairmen);
    }
    public void deleteAll() {
        chairmanRepository.deleteAll();
    }
    public void deleteById(String id) {
        chairmanRepository.deleteById(id);
    }
    public boolean existsById(String id) {
        return chairmanRepository.existsById(id);
    }
    public void updateOnlineStatus(String login, boolean isOnline) {
        log.info("Updating online status for user: {} to {}", login, isOnline);
        chairmanRepository.findByLogin(login).ifPresentOrElse(chairman -> {
            chairman.setOnline(isOnline);
            chairmanRepository.save(chairman);
            log.info("Updated online status for chairman: {}", chairman.getId());
        }, () -> log.warn("Chairman not found with login: {}", login));
    }
}
