package org.spacelab.housingutilitiessystemadmin.controller.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.spacelab.housingutilitiessystemadmin.service.RedisService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.*;

/**
 * Демонстрационный контроллер для работы с Redis.
 * Показывает примеры использования RedisService.
 */
@RestController
@RequestMapping("/api/redis-demo")
@RequiredArgsConstructor
@Slf4j
public class RedisDemoController {

    private final RedisService redisService;

    // ==================== ПРИМЕРЫ РАБОТЫ СО СТРОКАМИ ====================

    @PostMapping("/string/set")
    public ResponseEntity<String> setString(@RequestParam String key, @RequestParam String value) {
        redisService.setValue(key, value);
        return ResponseEntity.ok("Значение сохранено: " + key);
    }

    @PostMapping("/string/set-with-ttl")
    public ResponseEntity<String> setStringWithTTL(
            @RequestParam String key,
            @RequestParam String value,
            @RequestParam long ttlSeconds) {
        redisService.setValue(key, value, Duration.ofSeconds(ttlSeconds));
        return ResponseEntity.ok("Значение сохранено с TTL " + ttlSeconds + " секунд");
    }

    @GetMapping("/string/get")
    public ResponseEntity<?> getString(@RequestParam String key) {
        Object value = redisService.getValue(key);
        if (value == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(Map.of("key", key, "value", value));
    }

    @DeleteMapping("/string/delete")
    public ResponseEntity<String> deleteString(@RequestParam String key) {
        boolean deleted = redisService.deleteKey(key);
        return ResponseEntity.ok(deleted ? "Ключ удален" : "Ключ не найден");
    }

    // ==================== ПРИМЕРЫ РАБОТЫ С HASH ====================

    @PostMapping("/hash/set-field")
    public ResponseEntity<String> setHashField(
            @RequestParam String key,
            @RequestParam String field,
            @RequestParam String value) {
        redisService.setHashField(key, field, value);
        return ResponseEntity.ok("Поле сохранено в hash");
    }

    @GetMapping("/hash/get")
    public ResponseEntity<?> getHash(@RequestParam String key) {
        Map<Object, Object> hash = redisService.getHash(key);
        if (hash.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(hash);
    }

    @GetMapping("/hash/get-field")
    public ResponseEntity<?> getHashField(
            @RequestParam String key,
            @RequestParam String field) {
        Object value = redisService.getHashField(key, field);
        if (value == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(Map.of("field", field, "value", value));
    }

    // ==================== ПРИМЕРЫ РАБОТЫ СО СПИСКАМИ ====================

    @PostMapping("/list/add")
    public ResponseEntity<String> addToList(
            @RequestParam String key,
            @RequestBody List<String> values) {
        redisService.addToList(key, values.toArray());
        return ResponseEntity.ok("Элементы добавлены в список");
    }

    @GetMapping("/list/get")
    public ResponseEntity<?> getList(@RequestParam String key) {
        List<Object> list = redisService.getList(key);
        if (list == null || list.isEmpty()) {
            return ResponseEntity.ok(Collections.emptyList());
        }
        return ResponseEntity.ok(list);
    }

    @GetMapping("/list/range")
    public ResponseEntity<?> getListRange(
            @RequestParam String key,
            @RequestParam long start,
            @RequestParam long end) {
        List<Object> list = redisService.getListRange(key, start, end);
        return ResponseEntity.ok(list != null ? list : Collections.emptyList());
    }

    // ==================== ПРИМЕРЫ РАБОТЫ С МНОЖЕСТВАМИ ====================

    @PostMapping("/set/add")
    public ResponseEntity<String> addToSet(
            @RequestParam String key,
            @RequestBody List<String> values) {
        redisService.addToSet(key, values.toArray());
        return ResponseEntity.ok("Элементы добавлены во множество");
    }

    @GetMapping("/set/get")
    public ResponseEntity<?> getSet(@RequestParam String key) {
        Set<Object> set = redisService.getSet(key);
        return ResponseEntity.ok(set != null ? set : Collections.emptySet());
    }

    @GetMapping("/set/is-member")
    public ResponseEntity<?> isMemberOfSet(
            @RequestParam String key,
            @RequestParam String value) {
        boolean isMember = redisService.isMemberOfSet(key, value);
        return ResponseEntity.ok(Map.of("isMember", isMember));
    }

    // ==================== ПРИМЕРЫ РАБОТЫ С SORTED SET ====================

    @PostMapping("/sorted-set/add")
    public ResponseEntity<String> addToSortedSet(
            @RequestParam String key,
            @RequestParam String value,
            @RequestParam double score) {
        redisService.addToSortedSet(key, value, score);
        return ResponseEntity.ok("Элемент добавлен в sorted set");
    }

    @GetMapping("/sorted-set/range")
    public ResponseEntity<?> getSortedSetRange(
            @RequestParam String key,
            @RequestParam long start,
            @RequestParam long end) {
        Set<Object> set = redisService.getSortedSetRange(key, start, end);
        return ResponseEntity.ok(set != null ? set : Collections.emptySet());
    }

    @GetMapping("/sorted-set/by-score")
    public ResponseEntity<?> getSortedSetByScore(
            @RequestParam String key,
            @RequestParam double min,
            @RequestParam double max) {
        Set<Object> set = redisService.getSortedSetByScore(key, min, max);
        return ResponseEntity.ok(set != null ? set : Collections.emptySet());
    }

    // ==================== ДОПОЛНИТЕЛЬНЫЕ ОПЕРАЦИИ ====================

    @GetMapping("/keys")
    public ResponseEntity<?> getKeys(@RequestParam(defaultValue = "*") String pattern) {
        Set<String> keys = redisService.getKeys(pattern);
        return ResponseEntity.ok(keys != null ? keys : Collections.emptySet());
    }

    @GetMapping("/ttl")
    public ResponseEntity<?> getTTL(@RequestParam String key) {
        Long ttl = redisService.getTTL(key);
        return ResponseEntity.ok(Map.of("key", key, "ttl", ttl != null ? ttl : -2));
    }

    @PostMapping("/increment")
    public ResponseEntity<?> increment(@RequestParam String key) {
        Long value = redisService.increment(key);
        return ResponseEntity.ok(Map.of("key", key, "value", value));
    }

    @PostMapping("/increment-by")
    public ResponseEntity<?> incrementBy(
            @RequestParam String key,
            @RequestParam long delta) {
        Long value = redisService.incrementBy(key, delta);
        return ResponseEntity.ok(Map.of("key", key, "value", value));
    }

    @GetMapping("/exists")
    public ResponseEntity<?> exists(@RequestParam String key) {
        boolean exists = redisService.hasKey(key);
        return ResponseEntity.ok(Map.of("key", key, "exists", exists));
    }

    @DeleteMapping("/delete-pattern")
    public ResponseEntity<String> deletePattern(@RequestParam String pattern) {
        redisService.deleteKeys(pattern);
        return ResponseEntity.ok("Ключи удалены по паттерну: " + pattern);
    }
}
