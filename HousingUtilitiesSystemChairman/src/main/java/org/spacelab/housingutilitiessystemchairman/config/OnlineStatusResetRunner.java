package org.spacelab.housingutilitiessystemchairman.config;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.spacelab.housingutilitiessystemchairman.repository.mongo.ChairmanRepository;
import org.spacelab.housingutilitiessystemchairman.repository.mongo.UserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
@Component
@RequiredArgsConstructor
@Slf4j
public class OnlineStatusResetRunner implements ApplicationRunner {
    private final UserRepository userRepository;
    private final ChairmanRepository chairmanRepository;
    @Override
    public void run(ApplicationArguments args) {
        log.info("🔄 Resetting all users and chairmen online status to OFFLINE...");
        userRepository.findAll().forEach(user -> {
            if (user.isOnline()) {
                user.setOnline(false);
                userRepository.save(user);
                log.debug("Reset user {} to offline", user.getEmail());
            }
        });
        chairmanRepository.findAll().forEach(chairman -> {
            if (chairman.isOnline()) {
                chairman.setOnline(false);
                chairmanRepository.save(chairman);
                log.debug("Reset chairman {} to offline", chairman.getLogin());
            }
        });
        log.info("✅ All users and chairmen online status reset completed");
    }
}
