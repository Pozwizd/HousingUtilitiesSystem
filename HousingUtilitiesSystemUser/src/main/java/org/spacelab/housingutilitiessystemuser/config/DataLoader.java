package org.spacelab.housingutilitiessystemuser.config;

import lombok.RequiredArgsConstructor;
import org.spacelab.housingutilitiessystemuser.repository.BillRepository;
import org.spacelab.housingutilitiessystemuser.repository.VoteRepository;
import org.spacelab.housingutilitiessystemuser.repository.ContactSectionRepository;
import org.spacelab.housingutilitiessystemuser.repository.UserRepository;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;



@Component
@RequiredArgsConstructor
public class DataLoader {

    private final UserRepository userRepository;
    private final BillRepository billRepository;
    private final VoteRepository voteRepository;
    private final ContactSectionRepository contactSectionRepository;

    @EventListener(ApplicationReadyEvent.class)
    public void loadData() {
        System.out.println("=== User DataLoader: Проверка данных ===");

        
        long userCount = userRepository.count();
        if (userCount == 0) {
            System.out.println("ВНИМАНИЕ: Users не найдены! Запустите Admin DataLoader");
        } else {
            System.out.println("Users: " + userCount + " записей");
        }

        
        long billCount = billRepository.count();
        if (billCount == 0) {
            System.out.println("ВНИМАНИЕ: Bills не найдены! Запустите Chairman DataLoader");
        } else {
            System.out.println("Bills: " + billCount + " записей");
        }

        
        long voteCount = voteRepository.count();
        if (voteCount == 0) {
            System.out.println("ВНИМАНИЕ: Votes не найдены! Запустите Chairman DataLoader");
        } else {
            System.out.println("Votes: " + voteCount + " записей");
        }

        
        long contactSectionCount = contactSectionRepository.count();
        if (contactSectionCount == 0) {
            System.out.println("ВНИМАНИЕ: ContactSections не найдены! Запустите Chairman DataLoader");
        } else {
            System.out.println("ContactSections: " + contactSectionCount + " записей");
        }

        System.out.println("=== User DataLoader: Проверка завершена ===");
    }
}
