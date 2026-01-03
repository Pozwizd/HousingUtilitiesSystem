package org.spacelab.housingutilitiessystemuser.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.spacelab.housingutilitiessystemuser.repository.UserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
@Slf4j
public class OnlineStatusResetRunner implements ApplicationRunner {

    private final UserRepository userRepository;

    @Override
    public void run(ApplicationArguments args) {
        log.info("🔄 Resetting all users online status to OFFLINE...");

        userRepository.findAll().forEach(user -> {
            if (user.isOnline()) {
                user.setOnline(false);
                userRepository.save(user);
                log.debug("Reset user {} to offline", user.getEmail());
            }
        });

        log.info("✅ All users online status reset completed");
    }
}
