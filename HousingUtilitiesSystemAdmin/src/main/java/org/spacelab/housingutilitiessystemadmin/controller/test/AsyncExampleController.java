package org.spacelab.housingutilitiessystemadmin.controller.test;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.WebAsyncTask;

/**
 * Примеры правильной работы с асинхронными эндпоинтами и JWT безопасностью
 * 
 * ВАЖНО: Используйте асинхронность только для долгих операций (>1 секунда)
 * Для обычных CRUD операций (<100ms) используйте синхронные методы!
 */
@RestController
@RequestMapping("/api/async-examples")
@RequiredArgsConstructor
@Slf4j
public class AsyncExampleController {

    // Внедрите ваши сервисы здесь
    // private final ReportService reportService;
    // private final ExportService exportService;

    // ========================================================================
    // СПОСОБ 1: WebAsyncTask с явной передачей SecurityContext
    // Подходит для: Операции 1-5 секунд, когда клиент ждет результат
    // ========================================================================
    
    /**
     * Пример: Экспорт данных в Excel
     * Клиент ждет файл (держим HTTP соединение открытым)
     */
    @GetMapping("/export/{id}")
    public WebAsyncTask<ResponseEntity<ByteArrayResource>> exportData(
            @PathVariable Long id,
            Authentication authentication) {
        
        log.info("[{}] Запрос на экспорт данных ID: {} от пользователя: {}", 
                 Thread.currentThread().getName(), id, authentication.getName());
        
        // 1. КРИТИЧНО: Сохраняем SecurityContext ДО входа в async поток
        final SecurityContext securityContext = SecurityContextHolder.getContext();
        final String username = authentication.getName();
        
        // 2. Создаем WebAsyncTask
        return new WebAsyncTask<>(
            60000L,           // таймаут 60 секунд
            "taskExecutor",   // имя bean executor из AsyncConfig
            () -> {
                try {
                    // 3. Устанавливаем SecurityContext в async потоке
                    SecurityContextHolder.setContext(securityContext);
                    
                    log.info("[{}] Выполнение экспорта в async потоке для пользователя: {}", 
                             Thread.currentThread().getName(), username);
                    
                    // 4. Выполняем долгую операцию
                    // ByteArrayResource resource = exportService.exportToExcel(id);
                    
                    // Для примера - заглушка
                    byte[] data = "Example export data".getBytes();
                    ByteArrayResource resource = new ByteArrayResource(data);
                    
                    return ResponseEntity.ok()
                        .header("Content-Disposition", 
                                "attachment; filename=export_" + id + ".xlsx")
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .body(resource);
                        
                } finally {
                    // 5. ОБЯЗАТЕЛЬНО очищаем SecurityContext
                    SecurityContextHolder.clearContext();
                }
            }
        );
    }

    // ========================================================================
    // СПОСОБ 2: @Async метод сервиса (с передачей данных)
    // Подходит для: Фоновые задачи, клиент не ждет результат
    // ========================================================================
    
    /**
     * Пример: Генерация отчета
     * Клиент получает немедленный ответ "Started", обработка идет в фоне
     */
    @PostMapping("/report/generate")
    public ResponseEntity<String> generateReport(Authentication authentication) {
        
        // 1. Извлекаем данные пользователя ДО async операции
        String username = authentication.getName();
        // Long userId = ((CustomUserDetails) authentication.getPrincipal()).getId();
        
        log.info("Запуск генерации отчета для пользователя: {}", username);
        
        // 2. Передаем данные явно в async метод
        // reportService.generateReportAsync(userId, username);
        
        // 3. Немедленно возвращаем ответ клиенту
        return ResponseEntity.accepted()
            .body("Report generation started. You will be notified when ready.");
    }
    
    /* Соответствующий метод в сервисе:
    
    @Service
    public class ReportService {
        
        @Async("taskExecutor")  // Использует executor с DelegatingSecurityContext
        public void generateReportAsync(Long userId, String username) {
            log.info("Generating report for user: {}", username);
            
            // Долгая операция
            Report report = generateComplexReport(userId);
            
            // Сохранение
            reportRepository.save(report);
            
            // Уведомление
            notificationService.notifyReportReady(username, report.getId());
        }
    }
    */

    // ========================================================================
    // СПОСОБ 3: Синхронная загрузка дашборда
    // Подходит для: Обычных операций без параллельной обработки
    // ========================================================================
    
    /**
     * Пример: Загрузка дашборда
     * Синхронная загрузка данных
     */
    @GetMapping("/dashboard")
    public ResponseEntity<DashboardData> getDashboard(
            Authentication authentication) {
        
        // 1. Извлекаем данные пользователя
        String username = authentication.getName();
        // Long userId = ((CustomUserDetails) authentication.getPrincipal()).getId();
        Long userId = 1L; // Для примера
        
        log.info("Загрузка дашборда для пользователя: {}", username);
        
        // 2. Синхронная загрузка данных
        /* Пример с реальными сервисами:
        
        UserStats stats = statsService.getUserStats(userId);
        List<Notification> notifications = notificationService.getRecentNotifications(userId);
        List<Activity> activity = activityService.getRecentActivity(userId);
        
        DashboardData dashboard = new DashboardData();
        dashboard.setUsername(username);
        dashboard.setStats(stats);
        dashboard.setNotifications(notifications);
        dashboard.setActivity(activity);
        
        return ResponseEntity.ok(dashboard);
        */
        
        // Заглушка для примера
        DashboardData dashboard = new DashboardData();
        dashboard.setUsername(username);
        return ResponseEntity.ok(dashboard);
    }

    // ========================================================================
    // СПОСОБ 4: Синхронный + фоновая обработка
    // Подходит для: Немедленный ответ клиенту + фоновая работа после
    // ========================================================================
    
    /**
     * Пример: Создание заказа
     * Заказ создается синхронно, уведомления отправляются асинхронно
     */
    @PostMapping("/order")
    public ResponseEntity<OrderResponse> createOrder(
            @RequestBody OrderRequest request,
            Authentication authentication) {
        
        String username = authentication.getName();
        // Long userId = ((CustomUserDetails) authentication.getPrincipal()).getId();
        Long userId = 1L; // Для примера
        
        log.info("Создание заказа для пользователя: {}", username);
        
        // 1. Синхронно создаем заказ (быстрая операция)
        // Order order = orderService.createOrder(request, userId);
        
        // 2. Асинхронно отправляем уведомления (после ответа клиенту)
        // orderService.sendOrderNotificationsAsync(order.getId(), username);
        
        // 3. Немедленно возвращаем ответ
        OrderResponse response = new OrderResponse();
        response.setOrderId(123L);
        response.setStatus("CREATED");
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ========================================================================
    // ❌ ПЛОХИЕ ПРИМЕРЫ (НЕ ДЕЛАЙТЕ ТАК!)
    // ========================================================================
    
    /**
     * ✅ ПРАВИЛЬНО: Синхронный метод
     */
    @GetMapping("/bad-example-1/{id}")
    public ResponseEntity<String> badExample1(@PathVariable Long id) {
        // Синхронный код - SecurityContext доступен
        return ResponseEntity.ok("Synchronous method with security!");
    }
    
    /**
     * ❌ ПЛОХО: Async для быстрой операции
     * Overhead на переключение потоков больше, чем выигрыш
     */
    @GetMapping("/bad-example-2/{id}")
    public WebAsyncTask<ResponseEntity<String>> badExample2(@PathVariable Long id) {
        final SecurityContext ctx = SecurityContextHolder.getContext();
        
        return new WebAsyncTask<>(() -> {
            try {
                SecurityContextHolder.setContext(ctx);
                // Операция занимает 10ms → async бессмысленен!
                String result = "Quick operation";
                return ResponseEntity.ok(result);
            } finally {
                SecurityContextHolder.clearContext();
            }
        });
        // ❌ Для быстрых операций (<100ms) используйте синхронный код!
    }
    
    /**
     * ✅ ПРАВИЛЬНО: Синхронный метод для быстрых операций
     */
    @GetMapping("/good-example/{id}")
    public ResponseEntity<String> goodExample(@PathVariable Long id) {
        // Простая, быстрая операция → синхронный код
        String result = "Quick operation";
        return ResponseEntity.ok(result);
    }

    // ========================================================================
    // Вспомогательные классы (для примеров)
    // ========================================================================
    
    private static class DashboardData {
        private String username;
        // private UserStats stats;
        // private List<Notification> notifications;
        // private List<Activity> activity;
        
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
    }
    
    private static class OrderRequest {
        private String productId;
        private int quantity;
        
        public String getProductId() { return productId; }
        public void setProductId(String productId) { this.productId = productId; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
    }
    
    private static class OrderResponse {
        private Long orderId;
        private String status;
        
        public Long getOrderId() { return orderId; }
        public void setOrderId(Long orderId) { this.orderId = orderId; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
}
