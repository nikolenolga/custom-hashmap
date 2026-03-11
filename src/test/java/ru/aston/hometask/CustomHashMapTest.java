package ru.aston.hometask;

import com.github.javafaker.Faker;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("ConstantValue")
class CustomHashMapTest {
    static final Faker dataFaker = new Faker();
    static int size = 50;
    static String[] values = new String[size];
    static CustomHashMap<Integer, String> map;
    static CustomHashMap<Integer, String> emptyMap;
    static Integer existingKey;
    static String existingValue;

    @BeforeAll
    static void init() {
        map = new CustomHashMap<>();
        emptyMap = new CustomHashMap<>();
        String value = "not existing value";
        for (int i = 0; i < size; i++) {
            value = generateRandomSpell();
            values[i] = value;
            map.put(i, value);
        }
        existingKey = size - 1;
        existingValue = value;
    }

    static String generateRandomSpell() {
        return dataFaker.harryPotter().spell();
    }

    @Test
    void givenMapWithOutElements_whenCallSize_thenReturnActualSize() {
        /* given */
        int expected = 0;
        /* when */
        int actual = emptyMap.size();
        /* then */
        assertEquals(expected, actual);
    }

    @Test
    void givenMapWithElements_whenPutOneNewElementWithUniqueKeyAndCallSize_thenSizeIncreasedByOne() {
        /* given */
        int expected = 1;
        int oldSize = map.size();
        /* when */
        int key = 55;
        while (map.containsKey(key)) {
            key += 1;
        }
        map.put(key, "Test value");
        int actual = map.size() - oldSize;
        /* then */
        assertEquals(expected, actual);
    }

    @Test
    void givenMapWithElements_whenCallIsEmpty_thenReturnFalse() {
        /* given when then */
        assertFalse(map.isEmpty());
    }

    @Test
    void givenEmptyMap_whenCallIsEmpty_thenReturnTrue() {
        /* given when then */
        assertTrue(emptyMap.isEmpty());
    }

    @ParameterizedTest
    @ValueSource(ints = {56, 24, 78, 78, 78, 13, 5, 66, 435, 6546, 6546, 6546, 6546, 6546, 33, 663424, 9, 677, 98, Integer.MIN_VALUE, Integer.MAX_VALUE, 0, -535435, -522})
    void givenValidRepeatingKeysAndValidValue_whenCallPut_thenDoNotThrowException(int key) {
        /* given */
        String value = generateRandomSpell();
        /* when then */
        assertDoesNotThrow(() -> map.put(key, value));
    }

    @Test
    void givenNullKey_whenCallPut_thenDoNotThrowException() {
        /* given */
        Integer key = null;
        String value = generateRandomSpell();
        /* when then */
        assertDoesNotThrow(() -> map.put(key, value));
    }

    @Test
    void givenPuttedElementWithMaxKey_whenCallGet_thenReturnRightValue() {
        /* given */
        Integer key = Integer.MAX_VALUE;
        String expected = generateRandomSpell();
        map.put(key, expected);
        /* when */
        String actual = map.get(key);
        /* then */
        assertEquals(expected, actual);
    }

    @Test
    void givenMapWithNullKeyElement_whenCallGet_thenReturnExpectedValue() {
        /* given */
        Integer key = null;
        String expected = generateRandomSpell();
        map.put(key, expected);
        /* when */
        String actual = map.get(key);
        /* then */
        assertEquals(expected, actual);
    }

    @Test
    void givenValidKeyAndNullValue_whenCallPut_thenDoNotThrowException() {
        /* given */
        Integer key = 11111;
        String value = null;
        /* when then */
        assertDoesNotThrow(() -> map.put(key, value));
    }

    @Test
    void givenMapWithNullValue_whenContainsValue_thenReturnTrue() {
        /* given */
        Integer key = 11111;
        String value = null;
        map.put(key, value);
        /* when then */
        assertTrue(map.containsValue(value));
    }

    @Test
    void givenKeyNotExistingInMap_whenCallPut_thenReturnNullOldValue() {
        /* given */
        Integer key = 1454545;
        String value = generateRandomSpell();
        while (map.containsKey(key)) {
            key++;
        }
        /* when */
        String oldValue = map.put(key, value);
        /* then */
        assertNull(oldValue);
    }

    @Test
    void givenNewElementsWithNotExistingKeys_whenCallPutForAll_thenMapContainsAllOldKeys() {
        /* given when */
        Integer key = 1454545;
        for (int i = 0; i < 50; i++) {
            while (map.containsKey(key)) {
                key++;
            }
            String value = generateRandomSpell();
            map.put(key, value);
        }

        /* when */
        boolean containsAll = true;
        for (int i = 0; i < values.length; i++) {
            if (!map.containsKey(i)) {
                containsAll = false;
                break;
            }
        }

        /* then */
        assertTrue(containsAll);
    }

    @Test
    void givenKeyExistingInMap_whenCallPut_thenReturnValidOldValue() {
        /* given */
        Integer existingKey = 2;
        while (!map.containsKey(existingKey)) {
            existingKey++;
        }
        String expectedOldValue = map.get(existingKey);
        String newValue = generateRandomSpell();
        /* when */
        String actualOldValue = map.put(existingKey, newValue);
        /* then */
        assertEquals(expectedOldValue, actualOldValue);
    }

    @Test
    void givenExistingKey_whenCallContainsKey_thenReturnTrue() {
        /* given when then*/
        assertTrue(map.containsKey(existingKey));
    }

    @Test
    void givenNotExistingKey_whenCallContainsKey_thenReturnFalse() {
        /* given */
        Integer notExistingKey = 543545534;
        /* when then*/
        assertFalse(map.containsKey(notExistingKey));
    }

    @Test
    void givenExistingValue_whenCallContainsValue_thenReturnTrue() {
        /* given when then*/
        assertTrue(map.containsValue(existingValue));
    }

    @Test
    void givenNotExistingValue_whenCallContainsValue_thenReturnFalse() {
        /* given */
        String prefix = "Prefix for uniqueness";
        String notExistingValue = prefix + generateRandomSpell();
        /* when then*/
        assertFalse(map.containsValue(notExistingValue));
    }

    @ParameterizedTest
    @ValueSource(ints = {56, 24, 78, 13, 5, 66, 435, 6546, 33, 663424, 9, 677, 98, Integer.MIN_VALUE, Integer.MAX_VALUE, 0, -535435, -522})
    void givenValidRepeatingKeys_whenCallGet_thenDoNotThrowException(int key) {
        /* given when then */
        assertDoesNotThrow(() -> map.get(key));
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4, 5, 10, 45, 16, 33, 24, 19, 37, 0})
    void givenExistingKeys_whenCallGet_thenReturnNotNullValue(int key) {
        /* given when */
        String value = map.get(key);
        /* then */
        assertNotNull(value);
    }

    @SuppressWarnings("DataFlowIssue")
    @Test
    void givenNullMap_whenCallPutAll_thenThrowNullPointerException() {
        assertThrows(NullPointerException.class, () -> map.putAll(null));
    }

    @Test
    void givenNotNullEmptyMap_whenCallPutAll_thenNotThrowException() {
        assertDoesNotThrow(() -> map.putAll(new HashMap<>()));
    }

    @Test
    void givenNewMapWithElements_whenCallPutAll_thenMapContainsAllPuttedElements() {
        /* given */
        Map<Integer, String> addingMap = new HashMap<>();
        String value;
        for (int i = 0; i < size; i++) {
            value = generateRandomSpell();
            addingMap.put(i, value);
        }
        /* when */
        map.putAll(addingMap);
        /* then */
        boolean containsAll = true;
        for (Map.Entry<Integer, String> entry : addingMap.entrySet()) {
            if (!map.containsKey(entry.getKey()) || !map.containsValue(entry.getValue())) {
                containsAll = false;
                break;
            }
        }
        assertTrue(containsAll);
    }

    @Test
    void givenMapContainsElement_whenCallRemove_thenReturnRemovedElementValue() {
        /* given */
        Integer key = 1111153499;
        String expected = "prefix" + generateRandomSpell();
        map.put(key, expected);
        /* when */
        String actual = map.remove(key);
        /* then */
        assertEquals(expected, actual);
    }

    @Test
    void givenMapContainsElement_whenCallRemove_thenMapDoesNotContainElementKey() {
        /* given */
        Integer key = 1995111534;
        String expected = "prefix" + generateRandomSpell();
        map.put(key, expected);
        /* when */
        String actual = map.remove(key);
        /* then */
        assertFalse(map.containsKey(key));
    }

    @Test
    void givenNotExistingKey_whenCallRemove_thenReturnNullValue() {
        /* given */
        Integer notExistingKey = 222111134;
        /* when */
        String actual = map.remove(notExistingKey);
        /* then */
        assertNull(actual);
    }
}