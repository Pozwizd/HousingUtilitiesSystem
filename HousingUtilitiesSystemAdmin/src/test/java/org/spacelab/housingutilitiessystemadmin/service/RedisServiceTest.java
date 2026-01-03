package org.spacelab.housingutilitiessystemadmin.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.*;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RedisService Tests")
class RedisServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Mock
    private ListOperations<String, Object> listOperations;

    @Mock
    private HashOperations<String, Object, Object> hashOperations;

    @Mock
    private SetOperations<String, Object> setOperations;

    @Mock
    private ZSetOperations<String, Object> zSetOperations;

    @InjectMocks
    private RedisService redisService;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        lenient().when(redisTemplate.opsForList()).thenReturn(listOperations);
        lenient().when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        lenient().when(redisTemplate.opsForSet()).thenReturn(setOperations);
        lenient().when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
    }

    @Nested
    @DisplayName("String Operations Tests")
    class StringOperationsTests {
        @Test
        @DisplayName("Should set value")
        void setValue_shouldSetValue() {
            redisService.setValue("key", "value");
            verify(valueOperations).set("key", "value");
        }

        @Test
        @DisplayName("Should set value with timeout")
        void setValue_withTimeout_shouldSetValueWithTimeout() {
            Duration timeout = Duration.ofMinutes(5);
            redisService.setValue("key", "value", timeout);
            verify(valueOperations).set("key", "value", timeout);
        }

        @Test
        @DisplayName("Should get value")
        void getValue_shouldGetValue() {
            when(valueOperations.get("key")).thenReturn("value");
            Object result = redisService.getValue("key");
            assertThat(result).isEqualTo("value");
        }

        @Test
        @DisplayName("Should return null when key not found")
        void getValue_shouldReturnNull_whenNotFound() {
            when(valueOperations.get("key")).thenReturn(null);
            Object result = redisService.getValue("key");
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should check if key exists - true")
        void hasKey_shouldReturnTrue_whenKeyExists() {
            when(redisTemplate.hasKey("key")).thenReturn(true);
            boolean result = redisService.hasKey("key");
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should check if key exists - false")
        void hasKey_shouldReturnFalse_whenKeyNotExists() {
            when(redisTemplate.hasKey("key")).thenReturn(false);
            boolean result = redisService.hasKey("key");
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should check if key exists - null")
        void hasKey_shouldReturnFalse_whenNull() {
            when(redisTemplate.hasKey("key")).thenReturn(null);
            boolean result = redisService.hasKey("key");
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should delete key successfully")
        void deleteKey_shouldReturnTrue_whenDeleted() {
            when(redisTemplate.delete("key")).thenReturn(true);
            boolean result = redisService.deleteKey("key");
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should return false when delete fails")
        void deleteKey_shouldReturnFalse_whenNotDeleted() {
            when(redisTemplate.delete("key")).thenReturn(false);
            boolean result = redisService.deleteKey("key");
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return false when delete returns null")
        void deleteKey_shouldReturnFalse_whenNull() {
            when(redisTemplate.delete("key")).thenReturn(null);
            boolean result = redisService.deleteKey("key");
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should set expire successfully")
        void setExpire_shouldReturnTrue_whenExpireSet() {
            when(redisTemplate.expire("key", Duration.ofMinutes(5))).thenReturn(true);
            boolean result = redisService.setExpire("key", Duration.ofMinutes(5));
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should return false when expire fails")
        void setExpire_shouldReturnFalse_whenFails() {
            when(redisTemplate.expire("key", Duration.ofMinutes(5))).thenReturn(null);
            boolean result = redisService.setExpire("key", Duration.ofMinutes(5));
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should increment value")
        void increment_shouldIncrementValue() {
            when(valueOperations.increment("key")).thenReturn(1L);
            Long result = redisService.increment("key");
            assertThat(result).isEqualTo(1L);
        }

        @Test
        @DisplayName("Should increment by delta")
        void incrementBy_shouldIncrementByDelta() {
            when(valueOperations.increment("key", 5L)).thenReturn(5L);
            Long result = redisService.incrementBy("key", 5L);
            assertThat(result).isEqualTo(5L);
        }

        @Test
        @DisplayName("Should decrement value")
        void decrement_shouldDecrementValue() {
            when(valueOperations.decrement("key")).thenReturn(0L);
            Long result = redisService.decrement("key");
            assertThat(result).isEqualTo(0L);
        }
    }

    @Nested
    @DisplayName("List Operations Tests")
    class ListOperationsTests {
        @Test
        @DisplayName("Should add to list")
        void addToList_shouldAddElements() {
            redisService.addToList("key", "val1", "val2");
            verify(listOperations).rightPushAll("key", "val1", "val2");
        }

        @Test
        @DisplayName("Should get list")
        void getList_shouldReturnList() {
            List<Object> list = Arrays.asList("val1", "val2");
            when(listOperations.range("key", 0, -1)).thenReturn(list);
            List<Object> result = redisService.getList("key");
            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("Should get list range")
        void getListRange_shouldReturnRange() {
            List<Object> list = Arrays.asList("val1");
            when(listOperations.range("key", 0, 0)).thenReturn(list);
            List<Object> result = redisService.getListRange("key", 0, 0);
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Should remove first from list")
        void removeFirstFromList_shouldRemoveFirst() {
            when(listOperations.leftPop("key")).thenReturn("val1");
            Object result = redisService.removeFirstFromList("key");
            assertThat(result).isEqualTo("val1");
        }
    }

    @Nested
    @DisplayName("Hash Operations Tests")
    class HashOperationsTests {
        @Test
        @DisplayName("Should set hash field")
        void setHashField_shouldSetField() {
            redisService.setHashField("key", "field", "value");
            verify(hashOperations).put("key", "field", "value");
        }

        @Test
        @DisplayName("Should get hash field")
        void getHashField_shouldGetField() {
            when(hashOperations.get("key", "field")).thenReturn("value");
            Object result = redisService.getHashField("key", "field");
            assertThat(result).isEqualTo("value");
        }

        @Test
        @DisplayName("Should get hash")
        void getHash_shouldReturnHash() {
            Map<Object, Object> hash = new HashMap<>();
            hash.put("field", "value");
            when(hashOperations.entries("key")).thenReturn(hash);
            Map<Object, Object> result = redisService.getHash("key");
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Should delete hash field")
        void deleteHashField_shouldDeleteField() {
            redisService.deleteHashField("key", "field1", "field2");
            verify(hashOperations).delete("key", "field1", "field2");
        }

        @Test
        @DisplayName("Should check has hash field")
        void hasHashField_shouldReturnTrue() {
            when(hashOperations.hasKey("key", "field")).thenReturn(true);
            boolean result = redisService.hasHashField("key", "field");
            assertThat(result).isTrue();
        }
    }

    @Nested
    @DisplayName("Set Operations Tests")
    class SetOperationsTests {
        @Test
        @DisplayName("Should add to set")
        void addToSet_shouldAddElements() {
            redisService.addToSet("key", "val1", "val2");
            verify(setOperations).add("key", "val1", "val2");
        }

        @Test
        @DisplayName("Should get set")
        void getSet_shouldReturnSet() {
            Set<Object> set = new HashSet<>(Arrays.asList("val1", "val2"));
            when(setOperations.members("key")).thenReturn(set);
            Set<Object> result = redisService.getSet("key");
            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("Should check is member of set - true")
        void isMemberOfSet_shouldReturnTrue() {
            when(setOperations.isMember("key", "val")).thenReturn(true);
            boolean result = redisService.isMemberOfSet("key", "val");
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should check is member of set - false")
        void isMemberOfSet_shouldReturnFalse() {
            when(setOperations.isMember("key", "val")).thenReturn(false);
            boolean result = redisService.isMemberOfSet("key", "val");
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should check is member of set - null")
        void isMemberOfSet_shouldReturnFalse_whenNull() {
            when(setOperations.isMember("key", "val")).thenReturn(null);
            boolean result = redisService.isMemberOfSet("key", "val");
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should remove from set")
        void removeFromSet_shouldRemoveElements() {
            redisService.removeFromSet("key", "val1", "val2");
            verify(setOperations).remove("key", "val1", "val2");
        }
    }

    @Nested
    @DisplayName("Sorted Set Operations Tests")
    class SortedSetOperationsTests {
        @Test
        @DisplayName("Should add to sorted set")
        void addToSortedSet_shouldAddElement() {
            redisService.addToSortedSet("key", "value", 1.0);
            verify(zSetOperations).add("key", "value", 1.0);
        }

        @Test
        @DisplayName("Should get sorted set range")
        void getSortedSetRange_shouldReturnRange() {
            Set<Object> set = new HashSet<>(Arrays.asList("val1", "val2"));
            when(zSetOperations.range("key", 0, 10)).thenReturn(set);
            Set<Object> result = redisService.getSortedSetRange("key", 0, 10);
            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("Should get sorted set by score")
        void getSortedSetByScore_shouldReturnByScore() {
            Set<Object> set = new HashSet<>(Arrays.asList("val1"));
            when(zSetOperations.rangeByScore("key", 0.0, 5.0)).thenReturn(set);
            Set<Object> result = redisService.getSortedSetByScore("key", 0.0, 5.0);
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Should remove from sorted set")
        void removeFromSortedSet_shouldRemoveElements() {
            redisService.removeFromSortedSet("key", "val1", "val2");
            verify(zSetOperations).remove("key", "val1", "val2");
        }
    }

    @Nested
    @DisplayName("Additional Operations Tests")
    class AdditionalOperationsTests {
        @Test
        @DisplayName("Should get keys by pattern")
        void getKeys_shouldReturnKeys() {
            Set<String> keys = new HashSet<>(Arrays.asList("key1", "key2"));
            when(redisTemplate.keys("key*")).thenReturn(keys);
            Set<String> result = redisService.getKeys("key*");
            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("Should delete keys by pattern")
        void deleteKeys_shouldDeleteKeys() {
            Set<String> keys = new HashSet<>(Arrays.asList("key1", "key2"));
            when(redisTemplate.keys("key*")).thenReturn(keys);
            redisService.deleteKeys("key*");
            verify(redisTemplate).delete(keys);
        }

        @Test
        @DisplayName("Should not delete when no keys found")
        void deleteKeys_shouldNotDelete_whenNoKeys() {
            when(redisTemplate.keys("key*")).thenReturn(new HashSet<>());
            redisService.deleteKeys("key*");
            verify(redisTemplate, never()).delete(anyCollection());
        }

        @Test
        @DisplayName("Should not delete when keys is null")
        void deleteKeys_shouldNotDelete_whenNull() {
            when(redisTemplate.keys("key*")).thenReturn(null);
            redisService.deleteKeys("key*");
            verify(redisTemplate, never()).delete(anyCollection());
        }

        @Test
        @DisplayName("Should get TTL")
        void getTTL_shouldReturnTTL() {
            when(redisTemplate.getExpire("key", TimeUnit.SECONDS)).thenReturn(100L);
            Long result = redisService.getTTL("key");
            assertThat(result).isEqualTo(100L);
        }
    }
}
