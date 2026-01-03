package org.spacelab.housingutilitiessystemadmin.config.database;

import lombok.AllArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.util.List;


/**
 * Конфигурационный класс для выполнения массовых операций с MongoDB.
 * Обеспечивает высокую производительность при работе с большими объемами данных.
 */
@Component  // Делает класс Spring-бином, доступным для автоматической инъекции зависимостей
@AllArgsConstructor  // Lombok генерирует конструктор со всеми final полями
public class MongoBulkConfig {

    // MongoTemplate - основной интерфейс Spring Data MongoDB для работы с базой данных
    private final MongoTemplate mongoTemplate;

    /**
     * Выполняет массовую вставку документов в MongoDB.
     * @param documents - список документов для вставки
     * @param entityClass - класс сущности для определения коллекции
     * @param <T> - тип сущности (generic для универсальности)
     */
    public <T> void bulkInsert(List<T> documents, Class<T> entityClass) {
        // Если список пустой - прерываем выполнение
        if (documents.isEmpty()) return;

        // Проходим по всем документам и гарантируем наличие ID
        // Использование method reference для краткости (эквивалент forEach(doc -> ensureObjectHasId(doc)))
        documents.forEach(this::ensureObjectHasId);

        // Создаем объект для групповых операций в режиме UNORDERED:
        // - UNORDERED позволяет MongoDB выполнять операции параллельно для повышения производительности
        // - При ошибке в одной операции остальные продолжат выполняться
        BulkOperations bulkOps = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, entityClass);

        // Добавляем каждый документ в bulk-операцию (подготовка без выполнения)
        for (T document : documents) {
            bulkOps.insert(document);
        }

        // Выполняем все накопленные операции одним запросом к базе данных
        // Это значительно эффективнее множества отдельных INSERT запросов
        bulkOps.execute();

        // Логируем результат операции
        System.out.println("Bulk inserted " + documents.size() + " documents of type " + entityClass.getSimpleName());
    }

    /**
     * Гарантирует наличие ID у документа перед вставкой в MongoDB.
     * Использует Java Reflection API для доступа к полям объекта.
     * @param document - документ для проверки
     * @param <T> - тип документа
     */
    private <T> void ensureObjectHasId(T document) {
        try {
            // Получаем поле "id" через Reflection API
            // getDeclaredField() возвращает поле независимо от модификатора доступа (private/protected/public)
            var idField = document.getClass().getDeclaredField("id");

            // Делаем поле доступным для чтения/записи, даже если оно private
            // Подавляет стандартные проверки доступа Java
            idField.setAccessible(true);

            // Читаем текущее значение поля ID
            Object currentId = idField.get(document);

            // Проверяем, пустое ли значение ID (null или пустая строка)
            if (currentId == null || (currentId instanceof String && ((String) currentId).isEmpty())) {
                // Генерируем новый ObjectId (стандартный 12-байтовый идентификатор MongoDB)
                // Конвертируем в строку и устанавливаем как значение поля
                idField.set(document, new ObjectId().toString());
            }
        } catch (Exception e) {
            // Если поле "id" не найдено - пробуем альтернативный подход
            try {
                // Получаем все объявленные поля класса
                var fields = document.getClass().getDeclaredFields();

                // Перебираем все поля в поисках аннотации @Id из Spring Data
                for (var field : fields) {
                    // Проверяем, помечено ли поле аннотацией @Id
                    if (field.isAnnotationPresent(org.springframework.data.annotation.Id.class)) {
                        // Делаем поле доступным
                        field.setAccessible(true);

                        // Читаем текущее значение
                        Object currentId = field.get(document);

                        // Если ID пустой - генерируем новый
                        if (currentId == null || (currentId instanceof String && ((String) currentId).isEmpty())) {
                            field.set(document, new ObjectId().toString());
                        }
                        // Прерываем цикл после нахождения поля с @Id
                        break;
                    }
                }
            } catch (Exception ex) {
                // В случае любой ошибки логируем проблему
                System.err.println("Не удалось установить ID для объекта: " + document.getClass().getSimpleName());
            }
        }
    }

    /**
     * Выполняет массовый upsert (update + insert) документов.
     * Если документ существует - обновляет его, если нет - вставляет новый.
     * @param documents - список документов для upsert
     * @param entityClass - класс сущности
     * @param <T> - тип сущности
     */
    public <T> void bulkUpsert(List<T> documents, Class<T> entityClass) {
        // Проверка на пустой список
        if (documents.isEmpty()) return;

        // Создаем объект для групповых операций в неупорядоченном режиме
        BulkOperations bulkOps = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, entityClass);

        // Обрабатываем каждый документ
        for (T document : documents) {
            // Извлекаем ID документа через вспомогательный метод
            Object documentId = getId(document);

            // Создаем пустой BSON Document (внутренний формат MongoDB)
            org.bson.Document mongoDoc = new org.bson.Document();

            // Конвертируем Java-объект в BSON Document с помощью конвертера MongoTemplate
            // Это преобразует все поля объекта в формат, понятный MongoDB
            mongoTemplate.getConverter().write(document, mongoDoc);

            // Удаляем поле _id из документа, так как оно будет использоваться в query,
            // а не в данных для обновления
            mongoDoc.remove("_id");

            // Создаем объект Update для определения, какие поля нужно обновить
            org.springframework.data.mongodb.core.query.Update update =
                    new org.springframework.data.mongodb.core.query.Update();

            // Перебираем все поля документа и добавляем их в Update
            // set() устанавливает значение поля в MongoDB
            for (String key : mongoDoc.keySet()) {
                update.set(key, mongoDoc.get(key));
            }

            // Добавляем upsert-операцию в bulk:
            // - Query.query(...) создает запрос для поиска документа по _id
            // - Criteria.where("_id").is(documentId) - условие поиска: _id == documentId
            // - update - объект с данными для обновления
            // Если документ найден - обновляется, если нет - вставляется новый
            bulkOps.upsert(
                    org.springframework.data.mongodb.core.query.Query.query(
                            org.springframework.data.mongodb.core.query.Criteria.where("_id").is(documentId)
                    ),
                    update
            );
        }

        // Выполняем все upsert операции одним запросом
        bulkOps.execute();

        // Логируем результат
        System.out.println("Bulk upserted " + documents.size() + " documents of type " + entityClass.getSimpleName());
    }

    /**
     * Извлекает ID из документа, используя Reflection API.
     * Сначала ищет поле "id", затем поле с аннотацией @Id.
     * @param document - документ для извлечения ID
     * @return значение ID или новый ObjectId в случае ошибки
     */
    private Object getId(Object document) {
        try {
            // Первая попытка: найти поле с именем "id"
            var idField = document.getClass().getDeclaredField("id");

            // Делаем поле доступным для чтения
            idField.setAccessible(true);

            // Возвращаем значение поля
            return idField.get(document);

        } catch (NoSuchFieldException e) {
            // Если поле "id" не найдено - ищем поле с аннотацией @Id
            try {
                // Получаем все поля класса
                var fields = document.getClass().getDeclaredFields();

                // Перебираем поля
                for (var field : fields) {
                    // Проверяем наличие аннотации @Id
                    if (field.isAnnotationPresent(org.springframework.data.annotation.Id.class)) {
                        // Делаем поле доступным
                        field.setAccessible(true);

                        // Возвращаем значение
                        return field.get(document);
                    }
                }
            } catch (Exception ex) {
                // В случае любой ошибки возвращаем новый ObjectId
                // Это fallback-механизм на случай исключительных ситуаций
                return new org.bson.types.ObjectId();
            }

            // Если не нашли поле с @Id - возвращаем новый ObjectId
            return new org.bson.types.ObjectId();

        } catch (Exception e) {
            // Для любых других исключений возвращаем новый ObjectId
            return new org.bson.types.ObjectId();
        }
    }
}
