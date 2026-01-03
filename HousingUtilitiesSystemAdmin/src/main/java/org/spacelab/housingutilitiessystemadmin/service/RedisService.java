package org.spacelab.housingutilitiessystemadmin.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Сервис для работы с Redis напрямую через RedisTemplate.
 * Используйте этот подход для сложных операций, которые не покрываются аннотациями кэширования.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RedisService {

    private final RedisTemplate<String, Object> redisTemplate;

    // ==================== РАБОТА СО СТРОКАМИ ====================

    /**
     * Сохранить значение с ключом
     */
    public void setValue(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
        log.debug("Сохранено значение в Redis: key={}", key);
    }

    /**
     * Сохранить значение с TTL (время жизни)
     */
    public void setValue(String key, Object value, Duration timeout) {
        redisTemplate.opsForValue().set(key, value, timeout);
        log.debug("Сохранено значение в Redis с TTL: key={}, timeout={}", key, timeout);
    }

    /**
     * Получить значение по ключу
     */
    public Object getValue(String key) {
        Object value = redisTemplate.opsForValue().get(key);
        log.debug("Получено значение из Redis: key={}, found={}", key, value != null);
        return value;
    }

    /**
     * Проверить существование ключа
     */
    public boolean hasKey(String key) {
        Boolean exists = redisTemplate.hasKey(key);
        return exists != null && exists;
    }

    /**
     * Удалить ключ
     */
    public boolean deleteKey(String key) {
        Boolean deleted = redisTemplate.delete(key);
        log.debug("Удален ключ из Redis: key={}, success={}", key, deleted);
        return deleted != null && deleted;
    }

    /**
     * Установить время жизни для ключа
     */
    public boolean setExpire(String key, Duration timeout) {
        Boolean result = redisTemplate.expire(key, timeout);
        log.debug("Установлен TTL для ключа: key={}, timeout={}", key, timeout);
        return result != null && result;
    }

    // ==================== РАБОТА СО СПИСКАМИ ====================

    /**
     * Добавить элементы в конец списка
     */
    public void addToList(String key, Object... values) {
        redisTemplate.opsForList().rightPushAll(key, values);
        log.debug("Добавлены элементы в список: key={}, count={}", key, values.length);
    }

    /**
     * Получить все элементы списка
     */
    public List<Object> getList(String key) {
        List<Object> list = redisTemplate.opsForList().range(key, 0, -1);
        log.debug("Получен список из Redis: key={}, size={}", key, list != null ? list.size() : 0);
        return list;
    }

    /**
     * Получить элементы списка с пагинацией
     */
    public List<Object> getListRange(String key, long start, long end) {
        return redisTemplate.opsForList().range(key, start, end);
    }

    /**
     * Удалить первый элемент из списка
     */
    public Object removeFirstFromList(String key) {
        return redisTemplate.opsForList().leftPop(key);
    }

    // ==================== РАБОТА С HASH (СЛОВАРЯМИ) ====================

    /**
     * Сохранить поле в hash
     */
    public void setHashField(String key, String field, Object value) {
        redisTemplate.opsForHash().put(key, field, value);
        log.debug("Сохранено поле в hash: key={}, field={}", key, field);
    }

    /**
     * Получить значение поля из hash
     */
    public Object getHashField(String key, String field) {
        return redisTemplate.opsForHash().get(key, field);
    }

    /**
     * Получить все поля hash
     */
    public Map<Object, Object> getHash(String key) {
        Map<Object, Object> hash = redisTemplate.opsForHash().entries(key);
        log.debug("Получен hash из Redis: key={}, fields={}", key, hash.size());
        return hash;
    }

    /**
     * Удалить поле из hash
     */
    public void deleteHashField(String key, String... fields) {
        redisTemplate.opsForHash().delete(key, (Object[]) fields);
        log.debug("Удалены поля из hash: key={}, fields={}", key, fields.length);
    }

    /**
     * Проверить существование поля в hash
     */
    public boolean hasHashField(String key, String field) {
        return redisTemplate.opsForHash().hasKey(key, field);
    }

    // ==================== РАБОТА С МНОЖЕСТВАМИ (SET) ====================

    /**
     * Добавить элементы во множество
     */
    public void addToSet(String key, Object... values) {
        redisTemplate.opsForSet().add(key, values);
        log.debug("Добавлены элементы во множество: key={}, count={}", key, values.length);
    }

    /**
     * Получить все элементы множества
     */
    public Set<Object> getSet(String key) {
        Set<Object> set = redisTemplate.opsForSet().members(key);
        log.debug("Получено множество из Redis: key={}, size={}", key, set != null ? set.size() : 0);
        return set;
    }

    /**
     * Проверить, является ли элемент членом множества
     */
    public boolean isMemberOfSet(String key, Object value) {
        Boolean result = redisTemplate.opsForSet().isMember(key, value);
        return result != null && result;
    }

    /**
     * Удалить элемент из множества
     */
    public void removeFromSet(String key, Object... values) {
        redisTemplate.opsForSet().remove(key, values);
        log.debug("Удалены элементы из множества: key={}, count={}", key, values.length);
    }

    // ==================== РАБОТА С SORTED SET (УПОРЯДОЧЕННЫМИ МНОЖЕСТВАМИ) ====================

    /**
     * Добавить элемент в sorted set с оценкой
     */
    public void addToSortedSet(String key, Object value, double score) {
        redisTemplate.opsForZSet().add(key, value, score);
        log.debug("Добавлен элемент в sorted set: key={}, score={}", key, score);
    }

    /**
     * Получить элементы sorted set по диапазону индексов
     */
    public Set<Object> getSortedSetRange(String key, long start, long end) {
        return redisTemplate.opsForZSet().range(key, start, end);
    }

    /**
     * Получить элементы sorted set по диапазону оценок
     */
    public Set<Object> getSortedSetByScore(String key, double min, double max) {
        return redisTemplate.opsForZSet().rangeByScore(key, min, max);
    }

    /**
     * Удалить элемент из sorted set
     */
    public void removeFromSortedSet(String key, Object... values) {
        redisTemplate.opsForZSet().remove(key, values);
        log.debug("Удалены элементы из sorted set: key={}, count={}", key, values.length);
    }

    // ==================== ДОПОЛНИТЕЛЬНЫЕ ОПЕРАЦИИ ====================

    /**
     * Получить все ключи по паттерну
     */
    public Set<String> getKeys(String pattern) {
        return redisTemplate.keys(pattern);
    }

    /**
     * Удалить все ключи по паттерну
     */
    public void deleteKeys(String pattern) {
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
            log.info("Удалено {} ключей по паттерну: {}", keys.size(), pattern);
        }
    }

    /**
     * Получить TTL (оставшееся время жизни) ключа
     */
    public Long getTTL(String key) {
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    /**
     * Инкремент числового значения
     */
    public Long increment(String key) {
        return redisTemplate.opsForValue().increment(key);
    }

    /**
     * Инкремент на заданное значение
     */
    public Long incrementBy(String key, long delta) {
        return redisTemplate.opsForValue().increment(key, delta);
    }

    /**
     * Декремент числового значения
     */
    public Long decrement(String key) {
        return redisTemplate.opsForValue().decrement(key);
    }
}
