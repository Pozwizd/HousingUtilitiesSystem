// ============================================================================
// MongoDB Replica Set Initialization Script
// Скрипт автоматической инициализации и настройки replica set с аутентификацией
// 
// ⚠️  WARNING: This script contains DEV passwords!
// ⚠️  For PRODUCTION use docker-compose.prod.yml with environment variables!
// ============================================================================

// ============================================================================
// Инициализация Replica Set
// ============================================================================

print("Configuring replica set...");
try {
    // rs.initiate() - метод для инициализации нового replica set
    // Документация: https://www.mongodb.com/docs/manual/reference/method/rs.initiate/
    rs.initiate({
        _id: "rs0",  // Уникальное имя replica set (должно совпадать с --replSet в docker-compose)

        // Массив членов replica set
        members: [
            {
                _id: 0,                  // Уникальный ID члена в пределах replica set
                host: "mongo1:27017",    // Адрес узла (hostname:port) - доступен по DNS в Docker сети
                priority: 2              // Приоритет выборов: выше = больше вероятность стать PRIMARY
                                         // mongo1 имеет наивысший приоритет и будет предпочтительным primary
            },
            {
                _id: 1,
                host: "mongo2:27017",
                priority: 1              // Стандартный приоритет для secondary узла
                                         // Может стать primary только если mongo1 недоступен
            },
            {
                _id: 2,
                host: "mongo3:27017",
                priority: 1              // Такой же приоритет как у mongo2
                                         // При равном приоритете выбирается узел с более свежими данными
            }
        ]
    });
    print("Replica Set initiated!");

} catch (e) {
    // Обработка ошибки "Already Initialized" - replica set уже был инициализирован ранее
    // Делает скрипт идемпотентным - безопасно запускать многократно
    if (e.codeName === 'AlreadyInitialized') {
        print("Replica set already initialized");
    } else {
        // Любая другая ошибка - критическая, прерываем выполнение
        print("Error initiating replica set: " + e);
        throw e;
    }
}

// ============================================================================
// Ожидание выбора PRIMARY узла
// ============================================================================

print("Waiting for primary election...");
var attempts = 0;  // Счетчик попыток

// Цикл с таймаутом: максимум 30 попыток (30 секунд)
while (attempts < 30) {
    try {
        // rs.status() - получает текущее состояние replica set
        // Возвращает объект с информацией о всех членах и их состояниях
        var status = rs.status();

        // Ищем узел в состоянии PRIMARY среди всех членов
        // Возможные состояния: PRIMARY, SECONDARY, ARBITER, RECOVERING и др.
        var primary = status.members.find(m => m.stateStr === "PRIMARY");

        if (primary) {
            // PRIMARY найден - выборы завершились успешно
            print("Primary elected: " + primary.name);
            break;  // Выходим из цикла ожидания
        }
    } catch (e) {
        // Ошибка может возникнуть если replica set еще не готов
        print("Waiting... " + e.message);
    }

    sleep(1000);  // Пауза 1 секунда перед следующей попыткой
    attempts++;
}

// Проверка результата ожидания
if (attempts >= 30) {
    // Таймаут - PRIMARY не был выбран за 30 секунд (критическая проблема)
    print("ERROR: Timeout waiting for primary election");
} else {
    // Успех - replica set полностью инициализирован
    print("Replica Set initialized successfully!");
}

// ============================================================================
// Создание администраторского пользователя
// ============================================================================

print("Creating admin user...");
try {
    // getSiblingDB('admin') - переключается на базу данных admin
    // База admin используется для хранения системных пользователей и ролей
    db.getSiblingDB('admin').createUser({
        user: "admin",                 // Имя пользователя
        pwd: "HousingAdmin2024!",      // Пароль (ВАЖНО: использовать переменные окружения в production!)

        // Роли пользователя - набор привилегий
        roles: [
            {
                role: "userAdminAnyDatabase",  // Управление пользователями во ВСЕХ базах данных
                db: "admin"                    // Роль действует из базы admin
                // Фактически суперпользователь - может назначать любые привилегии
            },
            {
                role: "readWriteAnyDatabase",  // Чтение и запись во всех БД (кроме local и config)
                db: "admin"
            },
            {
                role: "dbAdminAnyDatabase",    // Администрирование всех БД (индексы, статистика и т.д.)
                db: "admin"
            },
            {
                role: "clusterAdmin",          // Максимальные привилегии управления кластером
                db: "admin"                    // Включает управление replica set и sharding
                // Объединяет: clusterManager, clusterMonitor, hostManager + dropDatabase
            }
        ]
    });
    print("Admin user created!");

} catch (e) {
    // Обработка ошибки дублирования - пользователь уже существует
    if (e.codeName === 'DuplicateKey') {
        print("Admin user already exists");
    } else {
        // Критическая ошибка при создании пользователя
        print("Error creating admin user: " + e);
        throw e;
    }
}

// ============================================================================
// Создание пользователя приложения
// ============================================================================

print("Creating application user...");
try {
    // Переключаемся на базу данных приложения
    // Этот пользователь будет храниться в БД приложения (не в admin)
    db.getSiblingDB('HousingUtilitiesSystemDB').createUser({
        user: "housingapp",            // Имя пользователя для Spring Boot приложения
        pwd: "HousingApp2024!",        // Пароль (должен совпадать с connection string в application-docker.yml)

        // Минимальные необходимые права для работы приложения
        roles: [
            {
                role: "readWrite",                        // Чтение и запись в указанной БД
                db: "HousingUtilitiesSystemDB"           // Ограничено только этой базой данных
                // Позволяет: find, insert, update, delete, createCollection
            },
            {
                role: "dbAdmin",                          // Административные операции в БД
                db: "HousingUtilitiesSystemDB"
                // Позволяет: создание индексов, статистика, профилирование
                // НЕ включает управление пользователями (безопаснее для приложения)
            }
        ]
    });
    print("Application user created!");

} catch (e) {
    // Обработка дублирования - пользователь уже создан
    if (e.codeName === 'DuplicateKey') {
        print("Application user already exists");
    } else {
        // Критическая ошибка
        print("Error creating application user: " + e);
        throw e;
    }
}

// ============================================================================
// Завершение инициализации
// ============================================================================

print("Initialization complete!");

// ИТОГОВОЕ СОСТОЯНИЕ:
// ✓ Replica Set из 3 узлов инициализирован (rs0)
// ✓ PRIMARY узел выбран (скорее всего mongo1 из-за priority=2)
// ✓ Admin пользователь создан с полными правами
// ✓ Application пользователь создан с ограниченными правами для БД
// ✓ Spring Boot приложение может подключаться с аутентификацией
