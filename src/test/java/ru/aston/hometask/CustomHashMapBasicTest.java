package ru.aston.hometask;

import com.github.javafaker.Country;
import com.github.javafaker.Faker;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings({"DataFlowIssue", "ConstantValue", "MismatchedQueryAndUpdateOfCollection"})
class CustomHashMapBasicTest {
    static final Faker dataFaker = new Faker();
    static Map<String, String> map = new CustomHashMap<>();
    static Set<String> keys = new HashSet<>();
    static List<String> values = new ArrayList<>();

    @BeforeAll
    static void init() {
        Country country;
        for (int i = 0; i < 50; ) {
            country = dataFaker.country();
            String name = country.name();
            String capital = country.capital();
            if (!map.containsKey(name)) {
                map.put(name, capital);
                keys.add(name);
                values.add(capital);
                i++;
            }
        }
    }

    @Test
    void givenNullInitialCapacity_whenCreateCustomHashMap_thenThrowNullPointerException() {
        /* given */
        Integer initialCapacity = null;
        /* when then */
        assertThrows(NullPointerException.class, () -> new CustomHashMap<>(initialCapacity));
    }

    @ParameterizedTest
    @ValueSource(ints = {-56, -24, -78, -66, -435, -6546, -6546})
    void givenNegativeInitialCapacity_whenCreateCustomHashMap_thenThrowNullPointerException(int initialCapacity) {
        assertThrows(IllegalArgumentException.class, () -> new CustomHashMap<>(initialCapacity));
    }

    @Test
    void givenZeroInitialCapacity_whenCreateCustomHashMap_thenDoesNotThrowException() {
        /* given */
        int initialCapacity = 0;
        /* when then */
        assertDoesNotThrow(() -> new CustomHashMap<>(initialCapacity));
    }

    @ParameterizedTest
    @ValueSource(ints = {56, 24, 78, 78, 78, 13, 5, 66, 435, 6546, 6546})
    void givenValidIntegerCapacity_whenCreateCustomHashMap_thenDoesNotThrowException(int initialCapacity) {
        assertDoesNotThrow(() -> new CustomHashMap<>(initialCapacity));
    }


    @Test
    void givenNullLoaderFactor_whenCreateCustomHashMap_thenThrowNullPointerException() {
        /* given */
        Float loaderFactor = null;
        /* when then */
        assertThrows(NullPointerException.class, () -> new CustomHashMap<>(loaderFactor));
    }

    @ParameterizedTest
    @ValueSource(floats = {-3.75f, -0.75f, -0.0f, -75f})
    void givenNegativeLoaderFactor_whenCreateCustomHashMap_thenThrowNullPointerException(float loaderFactor) {
        assertThrows(IllegalArgumentException.class, () -> new CustomHashMap<>(loaderFactor));
    }

    @Test
    void givenZeroLoaderFactor_whenCreateCustomHashMap_thenThrowException() {
        /* given */
        float loaderFactor = 0;
        /* when then */
        assertThrows(IllegalArgumentException.class, () -> new CustomHashMap<>(loaderFactor));
    }

    @ParameterizedTest
    @ValueSource(floats = {0.75f, 2.0f, 1.78F, 78.0f})
    void givenValidLoaderFactor_whenCreateCustomHashMap_thenDoesNotThrowException(float loaderFactor) {
        assertDoesNotThrow(() -> new CustomHashMap<>(loaderFactor));
    }

    @Test
    void givenNotEmptyMap_whenCallClear_thenMapIsEmpty() {
        /* given */
        Map<String, String> newMap = new CustomHashMap<>();
        newMap.put("Test key", "Test value");
        /* when */
        newMap.clear();
        /* then */
        assertTrue(newMap.isEmpty());
    }

    @Test
    void givenMap_whenCallValues_thenReturnAllMapValues() {
        /* given when */
        Collection<String> actual = map.values();
        /* then */
        assertEquals(values.size(), actual.size());
        assertTrue(values.containsAll(actual));
        assertTrue(actual.containsAll(values));
    }

    @Test
    void givenEmptyMap_whenCallValues_thenReturnEmptyCollection() {
        /* given */
        Map<String, Integer> newMap = new CustomHashMap<>();
        /* when */
        Collection<Integer> actual = newMap.values();
        /* then */
        assertTrue(actual.isEmpty());
    }

    @Test
    void givenMap_whenCallKeySet_thenReturnAllMapKeys() {
        /* given when */
        Set<String> actual = map.keySet();
        /* then */
        assertEquals(keys.size(), actual.size());
        assertTrue(keys.containsAll(actual));
        assertTrue(actual.containsAll(keys));
    }

    @Test
    void givenEmptyMap_whenCallKeySet_thenReturnEmptySet() {
        /* given */
        Map<String, Integer> newMap = new CustomHashMap<>();
        /* when */
        Set<String> actual = newMap.keySet();
        /* then */
        assertTrue(actual.isEmpty());
    }

    @Test
    void givenMap_whenCallEntrySet_thenReturnAllMapEntries() {
        /* given */
        Set<String> mapKeys = new HashSet<>(keys);
        Collection<String> mapValues = new ArrayList<>(values);
        /* when */
        Set<Map.Entry<String, String>> entries = map.entrySet();
        /* then */
        boolean containsAllEntries = true;
        for (Map.Entry<String, String> entry : entries) {
            boolean containsKey = mapKeys.remove(entry.getKey());
            boolean containsValue = mapValues.remove(entry.getValue());
            if (!containsKey || !containsValue) {
                containsAllEntries = false;
                break;
            }
        }
        assertTrue(containsAllEntries);

    }

    @Test
    void givenEmptyMap_whenCallEntrySet_thenReturnEmptySet() {
        /* given */
        Map<String, String> newMap = new CustomHashMap<>();
        /* when */
        Set<Map.Entry<String, String>> actual = newMap.entrySet();
        /* then */
        assertTrue(actual.isEmpty());
    }
}