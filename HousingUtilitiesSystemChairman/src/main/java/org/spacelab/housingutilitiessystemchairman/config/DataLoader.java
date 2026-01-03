package org.spacelab.housingutilitiessystemchairman.config;

import lombok.RequiredArgsConstructor;
import org.spacelab.housingutilitiessystemchairman.entity.*;
import org.spacelab.housingutilitiessystemchairman.repository.mongo.BillRepository;
import org.spacelab.housingutilitiessystemchairman.repository.mongo.FeedbackRequestRepository;
import org.spacelab.housingutilitiessystemchairman.repository.mongo.ReceiptRepository;
import org.spacelab.housingutilitiessystemchairman.repository.mongo.VoteRecordRepository;
import org.spacelab.housingutilitiessystemchairman.service.*;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
@Component
@RequiredArgsConstructor
public class DataLoader {
    private static final int MAX_RANDOM_RECORDS = 20;
    private final UserService userService;
    private final ContactService contactService;
    private final ContactSectionService contactSectionService;
    private final BillService billService;
    private final VoteService voteService;
    private final BillRepository billRepository;
    private final ReceiptRepository receiptRepository;
    private final VoteRecordRepository voteRecordRepository;
    private final FeedbackRequestRepository feedbackRequestRepository;
    private final MongoTemplate mongoTemplate;
    private List<User> houseResidents = new ArrayList<>();
    @EventListener(ApplicationReadyEvent.class)
    public void loadData() {
        System.out.println("=== Chairman DataLoader: Начало загрузки данных ===");
        houseResidents = userService.findAll();
        if (houseResidents.isEmpty()) {
            System.out.println("ВНИМАНИЕ: Пользователи не найдены! Запустите сначала Admin DataLoader");
            return;
        }
        loadBillsAndReceipts();
        List<Contact> contacts = loadContacts();
        loadContactSections(contacts);
        loadVotes();
        loadFeedbackRequests();
        System.out.println("=== Chairman DataLoader: Загрузка завершена! ===");
    }

    private void loadBillsAndReceipts() {
        long billCount = billRepository.count();
        if (billCount >= MAX_RANDOM_RECORDS) {
            System.out.println("Bills: количество записей (" + billCount + ") >= " + MAX_RANDOM_RECORDS + " - пропускаем");
            return;
        }
        List<Bill> existingBills = billRepository.findAll();
        Set<String> existingBillNumbers = new HashSet<>();
        for (Bill b : existingBills) {
            if (b.getBillNumber() != null) {
                existingBillNumbers.add(b.getBillNumber());
            }
        }
        int createdCount = 0;
        for (int i = 0; i < 12; i++) {
            String billNumber = "BILL-" + (2024 * 100 + i + 1);
            if (existingBillNumbers.contains(billNumber)) {
                System.out.println("  Bill уже существует: " + billNumber + " - пропускаем");
                continue;
            }
            Bill bill = new Bill();
            bill.setId(UUID.randomUUID().toString());
            bill.setBillNumber(billNumber);
            bill.setDate(LocalDate.now().minusMonths(i));
            bill.setReceipt(new ArrayList<>());
            Bill savedBill = mongoTemplate.save(bill);
            List<Receipt> receipts = createReceiptsForBill(savedBill, 2 + (i % 2));
            savedBill.setReceipt(receipts);
            mongoTemplate.save(savedBill);
            createdCount++;
        }
        System.out.println("Создано новых Bills: " + createdCount);
    }

    private List<Receipt> createReceiptsForBill(Bill bill, int count) {
        List<Receipt> receipts = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Receipt receipt = Receipt.builder()
                    .id(UUID.randomUUID().toString())
                    .bill(bill)
                    .localDate(bill.getDate().plusDays(i * 5L + 1))
                    .build();
            receipts.add(mongoTemplate.save(receipt));
        }
        System.out.println("  Создано Receipts для Bill " + bill.getBillNumber() + ": " + receipts.size());
        return receipts;
    }

    private List<Contact> loadContacts() {
        long contactCount = contactService.findAll().size();
        if (contactCount >= MAX_RANDOM_RECORDS) {
            System.out.println("Contacts: количество записей (" + contactCount + ") >= " + MAX_RANDOM_RECORDS + " - пропускаем");
            return contactService.findAll();
        }
        List<Contact> contacts = new ArrayList<>();
        contacts.add(createContactIfNotExists("Петренко Іван Сергійович", "chairman", "Голова ОСББ", "+380501234567"));
        contacts.add(createContactIfNotExists("Сидорчук Олена Миколаївна", "employee", "Бухгалтер", "+380502345678"));
        contacts.add(createContactIfNotExists("Козак Михайло Васильович", "employee", "Технік", "+380503456789"));
        contacts.add(createContactIfNotExists("Левченко Анна Петрівна", "employee", "Секретар", "+380504567890"));
        contacts.add(createContactIfNotExists("Романенко Сергій Олександрович", "employee", "Сантехнік", "+380505678901"));
        contacts.add(createContactIfNotExists("Шевчук Віктор Ігорович", "employee", "Електрик", "+380506789012"));
        contacts.add(createContactIfNotExists("Кузьменко Дмитро Анатолійович", "employee", "Двірник", "+380507890123"));
        contacts.removeIf(Objects::isNull);
        System.out.println("Создано новых Contacts: " + contacts.size());
        return contacts.isEmpty() ? contactService.findAll() : contacts;
    }

    private Contact createContactIfNotExists(String fullName, String type, String role, String phone) {
        List<Contact> existing = contactService.findAll();
        for (Contact c : existing) {
            if (fullName.equals(c.getFullName())) {
                System.out.println("  Contact уже существует: " + fullName + " - пропускаем");
                return null;
            }
        }
        Contact contact = new Contact();
        contact.setId(UUID.randomUUID().toString());
        contact.setFullName(fullName);
        contact.setContactType(type);
        contact.setRole(role);
        contact.setPhone(phone);
        contact.setDescription("Контактна особа ОСББ");
        Contact saved = mongoTemplate.save(contact);
        System.out.println("  Создан Contact: " + fullName);
        return saved;
    }

    private void loadContactSections(List<Contact> newContacts) {
        long sectionCount = contactSectionService.findAll().size();
        if (sectionCount >= 5) {
            System.out.println("ContactSections: количество записей (" + sectionCount + ") >= 5 - пропускаем");
            return;
        }
        List<Contact> allContacts = contactService.findAll();
        if (allContacts.isEmpty()) {
            System.out.println("ContactSections: нет контактов для секций - пропускаем");
            return;
        }
        List<ContactSection> sections = new ArrayList<>();
        int managementEnd = Math.min(4, allContacts.size());
        ContactSection management = createContactSectionIfNotExists(
                "Керівництво та адміністрація",
                "Контактна інформація керівництва ОСББ",
                new ArrayList<>(allContacts.subList(0, managementEnd))
        );
        if (management != null) sections.add(management);
        if (allContacts.size() > 4) {
            ContactSection technical = createContactSectionIfNotExists(
                    "Технічна підтримка",
                    "Контакти для вирішення технічних питань",
                    new ArrayList<>(allContacts.subList(4, allContacts.size()))
            );
            if (technical != null) sections.add(technical);
        }
        System.out.println("Создано ContactSections: " + sections.size());
    }

    private ContactSection createContactSectionIfNotExists(String title, String content, List<Contact> contacts) {
        List<ContactSection> existing = contactSectionService.findAll();
        for (ContactSection cs : existing) {
            if (title.equals(cs.getTitle())) {
                System.out.println("  ContactSection уже существует: " + title + " - пропускаем");
                return null;
            }
        }
        ContactSection section = new ContactSection();
        section.setId(UUID.randomUUID().toString());
        section.setTitle(title);
        section.setContent(content);
        section.setContacts(contacts);
        ContactSection saved = mongoTemplate.save(section);
        System.out.println("  Создана ContactSection: " + title);
        return saved;
    }

    private void loadVotes() {
        long voteCount = voteService.findAll().size();
        if (voteCount >= MAX_RANDOM_RECORDS) {
            System.out.println("Votes: количество записей (" + voteCount + ") >= " + MAX_RANDOM_RECORDS + " - пропускаем");
            return;
        }
        int created = 0;
        if (createVoteIfNotExists("Встановлення відеоспостереження в під'їзді",
                "Пропонується встановлення системи відеоспостереження для підвищення безпеки мешканців.",
                -5, 20, true, new int[]{0, 1, 2, 3, 4}, new int[]{5, 6}, new int[]{7})) created++;
        if (createVoteIfNotExists("Ремонт даху будинку",
                "Необхідне проведення ремонтних робіт для усунення протікань.",
                -2, 25, true, new int[]{0, 2}, new int[]{}, new int[]{1})) created++;
        if (createVoteIfNotExists("Благоустрій прибудинкової території",
                "Встановлення лавочок, озеленення двору.",
                -10, 15, true, new int[]{0, 1, 2, 3, 4, 5, 6, 7}, new int[]{}, new int[]{})) created++;
        if (createVoteIfNotExists("Заміна вікон у під'їзді №1",
                "Заміна старих вікон на сучасні енергозберігаючі склопакети.",
                -45, -15, false, new int[]{0, 1, 2, 3, 4, 5}, new int[]{6}, new int[]{7})) created++;
        if (createVoteIfNotExists("Встановлення шлагбаума на в'їзді",
                "Встановлення шлагбаума для обмеження в'їзду сторонніх автомобілів.",
                -60, -30, false, new int[]{0, 1}, new int[]{2, 3, 4, 5, 6}, new int[]{7})) created++;
        if (createVoteIfNotExists("Встановлення домофону в під'їзді",
                "Заміна застарілого домофону на сучасну систему.",
                -90, -60, false, new int[]{0, 1, 2, 3, 4, 5, 6, 7}, new int[]{}, new int[]{})) created++;
        if (createVoteIfNotExists("Встановлення дитячого майданчика",
                "Облаштування сучасного дитячого майданчика у дворі будинку.",
                -3, 27, true, new int[]{0, 1, 2, 3}, new int[]{4, 5, 6}, new int[]{7})) created++;
        if (createVoteIfNotExists("Ремонт ліфта в під'їзді №1",
                "Капітальний ремонт та модернізація ліфтового обладнання.",
                -120, -90, false, new int[]{0, 1, 2, 3, 4}, new int[]{5, 6, 7}, new int[]{})) created++;
        if (createVoteIfNotExists("Утеплення фасаду будівлі",
                "Проведення робіт з утеплення зовнішніх стін будинку.",
                -1, 29, true, new int[]{0}, new int[]{}, new int[]{})) created++;
        if (createVoteIfNotExists("Реконструкція підвалу",
                "Повна реконструкція підвального приміщення будинку.",
                -180, -150, false, new int[]{}, new int[]{}, new int[]{})) created++;
        System.out.println("Создано новых Votes: " + created);
    }

    private boolean createVoteIfNotExists(String title, String description,
                                          int startDaysOffset, int endDaysOffset,
                                          boolean isActive,
                                          int[] forUsers, int[] againstUsers, int[] abstainUsers) {
        List<Vote> existingVotes = voteService.findAll();
        for (Vote v : existingVotes) {
            if (title.equals(v.getTitle())) {
                System.out.println("  Vote уже существует: " + title.substring(0, Math.min(30, title.length())) + "... - пропускаем");
                return false;
            }
        }
        Vote vote = new Vote();
        vote.setId(UUID.randomUUID().toString());
        vote.setTitle(title);
        vote.setDescription(description);
        Calendar startCal = Calendar.getInstance();
        startCal.add(Calendar.DAY_OF_MONTH, startDaysOffset);
        vote.setStartTime(startCal.getTime());
        Calendar endCal = Calendar.getInstance();
        endCal.add(Calendar.DAY_OF_MONTH, endDaysOffset);
        vote.setEndTime(endCal.getTime());
        double totalArea = houseResidents.stream()
                .mapToDouble(u -> u.getApartmentArea() != null ? u.getApartmentArea() : 0)
                .sum();
        vote.setQuorumArea(totalArea * 0.5);
        vote.setStatus(isActive ? "Активне" : "Закрито");
        vote.setForVotesCount(forUsers.length);
        vote.setAgainstVotesCount(againstUsers.length);
        vote.setAbstentionsCount(abstainUsers.length);
        Vote savedVote = mongoTemplate.save(vote);
        createVoteRecords(savedVote, forUsers, "FOR", startDaysOffset);
        createVoteRecords(savedVote, againstUsers, "AGAINST", startDaysOffset);
        createVoteRecords(savedVote, abstainUsers, "ABSTENTION", startDaysOffset);
        System.out.println("  Создан Vote: " + title.substring(0, Math.min(30, title.length())) + "...");
        return true;
    }

    private void createVoteRecords(Vote vote, int[] userIndexes, String voteType, int startOffset) {
        Random random = new Random();
        for (int index : userIndexes) {
            if (index >= 0 && index < houseResidents.size()) {
                User user = houseResidents.get(index);
                VoteRecord record = new VoteRecord();
                record.setId(UUID.randomUUID().toString());
                record.setVote(vote);
                record.setUser(user);
                record.setVoteType(voteType);
                Calendar voteTimeCal = Calendar.getInstance();
                voteTimeCal.add(Calendar.DAY_OF_MONTH, startOffset);
                voteTimeCal.add(Calendar.HOUR_OF_DAY, random.nextInt(24 * 5));
                record.setVoteTime(voteTimeCal.getTime());
                voteRecordRepository.save(record);
            }
        }
    }

    private void loadFeedbackRequests() {
        long feedbackCount = feedbackRequestRepository.count();
        if (feedbackCount >= MAX_RANDOM_RECORDS) {
            System.out.println("FeedbackRequests: количество записей (" + feedbackCount + ") >= " + MAX_RANDOM_RECORDS + " - пропускаем");
            return;
        }
        List<FeedbackRequest> requests = new ArrayList<>();
        requests.add(createFeedbackRequestIfNotExists(0, "Установка видеонаблюдения в подъезде №3",
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nulla quam velit, vulputate eu pharetra nec, mattis ac neque. " +
                        "Duis vulputate commodo lectus, ac blandit elit tincidunt id. Sed rhoncus, tortor sed eleifend tristique, tortor mauris molestie elit, " +
                        "et lacinia ipsum quam nec dui. Quisque nec mauris sit amet elit iaculis pretium sit amet quis magna.",
                5));
        requests.add(createFeedbackRequestIfNotExists(1, "Проблема с освещением в подъезде",
                "Добрый день! На лестничной клетке между 3 и 4 этажом уже вторую неделю не работает освещение. " +
                        "Просьба направить электрика для устранения неисправности.",
                3));
        requests.add(createFeedbackRequestIfNotExists(2, "Замена окон в подъезде №2",
                "Уважаемый председатель! Предлагаю рассмотреть вопрос замены окон в подъезде №2. " +
                        "Текущие окна находятся в неудовлетворительном состоянии - дует, рамы рассохлись. " +
                        "Готов участвовать в обсуждении на общем собрании.",
                7));
        requests.add(createFeedbackRequestIfNotExists(3, "Благодарность за ремонт крыши",
                "Хочу выразить благодарность за оперативный ремонт крыши нашего дома. " +
                        "Протечки устранены качественно. Спасибо за работу!",
                2));
        requests.add(createFeedbackRequestIfNotExists(4, "Шум в ночное время",
                "Уважаемый председатель! Прошу принять меры в отношении жильцов квартиры 45. " +
                        "Регулярно в ночное время (после 23:00) проводятся шумные мероприятия, что мешает отдыху соседей.",
                1));
        requests.add(createFeedbackRequestIfNotExists(5, "Предложение по благоустройству двора",
                "Предлагаю установить новые скамейки возле детской площадки. " +
                        "Старые скамейки находятся в аварийном состоянии и могут быть опасны. " +
                        "Также было бы хорошо добавить урны для мусора.",
                10));
        requests.add(createFeedbackRequestIfNotExists(6, "Вопрос по оплате коммунальных услуг",
                "Добрый день! Получил квитанцию за декабрь с завышенной суммой за отопление. " +
                        "Прошу пояснить расчет или пересмотреть сумму.",
                4));
        requests.add(createFeedbackRequestIfNotExists(7, "Просьба о ремонте домофона",
                "Домофон в подъезде работает с перебоями - не всегда срабатывает открытие двери. " +
                        "Просьба провести диагностику и ремонт.",
                6));
        requests.removeIf(Objects::isNull);
        System.out.println("Создано новых FeedbackRequests: " + requests.size());
    }

    private FeedbackRequest createFeedbackRequestIfNotExists(int userIndex, String subject, String message, int daysAgo) {
        List<FeedbackRequest> existing = feedbackRequestRepository.findAll();
        for (FeedbackRequest f : existing) {
            if (subject.equals(f.getSubject())) {
                System.out.println("  FeedbackRequest уже существует: " + subject.substring(0, Math.min(30, subject.length())) + "... - пропускаем");
                return null;
            }
        }
        User user = houseResidents.get(userIndex % houseResidents.size());
        FeedbackRequest request = FeedbackRequest.builder()
                .id(UUID.randomUUID().toString())
                .subject(subject)
                .message(message)
                .createdAt(LocalDateTime.now().minusDays(daysAgo).minusHours(userIndex * 2L))
                .user(user)
                .userId(user.getId())
                .build();
        FeedbackRequest saved = mongoTemplate.save(request);
        System.out.println("  Создан FeedbackRequest: " + subject.substring(0, Math.min(30, subject.length())) + "...");
        return saved;
    }
}
