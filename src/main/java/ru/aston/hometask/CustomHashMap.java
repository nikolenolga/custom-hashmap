package ru.aston.hometask;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class CustomHashMap<K, V> implements Map<K, V> {
    static final int DEFAULT_CAPACITY = 1 << 4;
    static final int MAXIMUM_CAPACITY = 1 << 30;
    static final float DEFAULT_LOAD_FACTOR = 0.75f;
    final float loadFactor;
    Node<K, V>[] storage;
    int threshold;
    int size;

    public CustomHashMap(int capacity, float loadFactor) {
        if (capacity < 0) {
            throw new IllegalArgumentException("Illegal initial capacity value: " + capacity);
        }
        if (loadFactor <= 0 || Float.isNaN(loadFactor) || Float.isInfinite(loadFactor)) {
            throw new IllegalArgumentException("Illegal initial capacity value: " + capacity);
        }
        if (capacity > MAXIMUM_CAPACITY) {
            capacity = MAXIMUM_CAPACITY;
        }
        this.loadFactor = loadFactor;
        this.threshold = initialTableSize(capacity);
    }

    public CustomHashMap(int capacity) {
        this(capacity, DEFAULT_LOAD_FACTOR);
    }

    public CustomHashMap(float loadFactor) {
        this(DEFAULT_CAPACITY, loadFactor);
    }

    public CustomHashMap() {
        this(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR);
    }

    public CustomHashMap(Map<? extends K, ? extends V> m) {
        this.loadFactor = DEFAULT_LOAD_FACTOR;
        putAll(m);
    }

    static int hash(Object key) {
        int h;
        return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
    }

    static int initialTableSize(int capacity) {
        int n = -1 >>> Integer.numberOfLeadingZeros(capacity - 1);
        return (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
    }

    static int calculateThreshold(int capacity, float loadFactor) {
        int t = (int) (capacity * loadFactor);
        return (capacity < MAXIMUM_CAPACITY && t < MAXIMUM_CAPACITY) ?
                t : Integer.MAX_VALUE;
    }

    private int resolveIndex(Object key) {
        return resolveIndex(hash(key), storage.length);
    }

    private int resolveIndex(int hash) {
        return resolveIndex(hash, storage.length);
    }

    private int resolveIndex(int hash, int capacity) {
        return (capacity - 1) & hash;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public V get(Object key) {
        Node<K, V> node = getNode(key);
        return node == null ? null : node.getValue();
    }

    private Node<K, V> getNode(Object key) {
        int hash = hash(key);
        Node<K, V> node;

        if (storage == null || isEmpty() || (node = storage[resolveIndex(key)]) == null) {
            return null;
        }
        while (node.hash != hash || !(key == node.key || Objects.equals(key, node.key))) {
            if (node.next == null) {
                return null;
            }
            node = node.next;
        }

        return node;
    }

    @Override
    public V put(K key, V value) {
        Node<K, V>[] table = resizeIfNecessary();
        int hash = hash(key);
        int index = resolveIndex(hash);
        Node<K, V> node;

        if ((node = table[index]) == null) {
            table[index] = new Node<>(hash, key, value);
        } else {
            while (true) {
                if (node.hash == hash && (key == node.key || Objects.equals(key, node.key))) {
                    V oldValue = node.value;
                    node.setValue(value);

                    return oldValue;
                }
                if (node.next == null) {
                    break;
                }
                node = node.next;
            }
            node.next = new Node<>(hash, key, value);
        }

        size++;
        return null;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        int s = m.size();
        if (s > 0) {
            if (storage == null || storage.length == 0) {
                int ceil = (int) Math.ceil(s / (double) loadFactor);
                int t = ceil < MAXIMUM_CAPACITY ? ceil : MAXIMUM_CAPACITY;
                if (ceil > threshold) {
                    threshold = initialTableSize(t);
                }
            }

            for (Map.Entry<? extends K, ? extends V> entry : m.entrySet()) {
                put(entry.getKey(), entry.getValue());
            }
        }
    }

    @Override
    public boolean containsKey(Object key) {
        return getNode(key) != null;
    }

    @Override
    public boolean containsValue(Object value) {
        Node<K, V>[] table = storage;
        V current;
        if (table != null && size > 0) {
            for (Node<K, V> node : table) {
                while (node != null) {
                    if ((current = node.value) == value ||
                            (value != null && value.equals(current))) {
                        return true;
                    }
                    node = node.next;
                }
            }
        }
        return false;
    }

    private Node<K, V>[] resizeIfNecessary() {
        if (storage != null && storage.length > 0 && (storage.length >= MAXIMUM_CAPACITY || size <= threshold)) {
            return storage;
        }

        return resize();
    }

    private Node<K, V>[] resize() {
        Node<K, V>[] oldStorage = storage;
        int oldCapacity = (oldStorage == null) ? 0 : oldStorage.length;
        int newCapacity = calculateResizingCapacityAndSetNewThreshold(oldCapacity);

        @SuppressWarnings({"unchecked"})
        Node<K, V>[] newStorage = (Node<K, V>[]) new Node[newCapacity];
        storage = newStorage;

        if (oldStorage != null && size > 0) {
            for (int i = 0; i < oldCapacity; i++) {
                Node<K, V> current = oldStorage[i];
                if (current != null) {
                    oldStorage[i] = null;

                    if (current.next == null) {
                        newStorage[resolveIndex(current.hash, newCapacity)] = current;
                    } else {
                        Node<K, V> loHead = null, loTail = null;
                        Node<K, V> hiHead = null, hiTail = null;
                        Node<K, V> next;

                        do {
                            next = current.next;
                            if ((current.hash & oldCapacity) == 0) {
                                if (loTail == null)
                                    loHead = current;
                                else
                                    loTail.next = current;
                                loTail = current;
                            } else {
                                if (hiTail == null)
                                    hiHead = current;
                                else
                                    hiTail.next = current;
                                hiTail = current;
                            }
                        } while ((current = next) != null);
                        if (loTail != null) {
                            loTail.next = null;
                            newStorage[i] = loHead;
                        }
                        if (hiTail != null) {
                            hiTail.next = null;
                            newStorage[i + oldCapacity] = hiHead;
                        }
                    }
                }
            }
        }

        return newStorage;
    }

    private int calculateResizingCapacityAndSetNewThreshold(int capacity) {
        int currentCapacity;
        float currentLoadFactor = loadFactor;

        if (capacity == 0) {
            if (threshold > 0) {
                currentCapacity = threshold;
            } else {
                currentCapacity = DEFAULT_CAPACITY;
                currentLoadFactor = DEFAULT_LOAD_FACTOR;
            }
        } else {
            currentCapacity = capacity << 1;
            if (currentCapacity >= MAXIMUM_CAPACITY) {
                threshold = Integer.MAX_VALUE;
                return MAXIMUM_CAPACITY;
            }
        }

        threshold = calculateThreshold(currentCapacity, currentLoadFactor);
        return currentCapacity;
    }

    @Override
    public Collection<V> values() {
        @SuppressWarnings({"unchecked"})
        V[] result = (V[]) (new Object[size]);
        int index = 0;
        for (Entry<K, V> entry : entrySet()) {
            result[index++] = entry.getValue();
        }
        return List.of(result);
    }

    @Override
    public Set<K> keySet() {
        @SuppressWarnings({"unchecked"})
        K[] result = (K[]) (new Object[size]);
        int index = 0;
        for (Entry<K, V> entry : entrySet()) {
            result[index++] = entry.getKey();
        }

        return Set.of(result);
    }


    @Override
    public Set<Entry<K, V>> entrySet() {
        Node<K, V>[] table = storage;
        @SuppressWarnings({"unchecked"})
        Entry<K, V>[] result = (Entry<K, V>[]) new Entry[size];
        Node<K, V> node;
        if (table != null) {
            int index = 0;
            for (int i = 0; i < table.length; i++) {
                node = table[i];
                while (node != null) {
                    result[index++] = node;
                    node = node.next;
                }
            }
        }
        return Set.of(result);
    }

    @Override
    public V remove(Object key) {
        Node<K, V>[] table = storage;
        if (storage != null && size > 0) {
            int hash = hash(key);
            int index = resolveIndex(hash);
            Node<K, V> prev = null;
            Node<K, V> node = storage[index];
            while (node != null) {
                if (node.hash == hash && (key == node.key || Objects.equals(key, node.key))) {
                    if (prev != null) {
                        prev.next = node.next;
                    } else {
                        storage[index] = node.next;
                    }
                    node.next = null;
                    size--;
                    return node.value;
                }
                prev = node;
                node = node.next;
            }
        }
        return null;
    }

    @Override
    public void clear() {
        Node<K, V>[] table = storage;
        if (table != null && size > 0) {
            for (int i = 0; i < table.length; i++) {
                table[i] = null;
            }
            size = 0;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }

        if (o == this) {
            return true;
        }

        if (!(o instanceof Map<?, ?> map)) {
            return false;
        }

        if (map.size() != size())
            return false;

        Node<K, V>[] table = storage;
        try {
            for (Node<K, V> node : storage) {
                while (node != null) {
                    K key = node.getKey();
                    V value = node.getValue();
                    Object objectValue = map.get(key);

                    if (value == null) {
                        if (!(objectValue == null && map.containsKey(key)))
                            return false;
                    } else {
                        if (!value.equals(objectValue))
                            return false;
                    }
                    node = node.next;
                }
            }
        } catch (ClassCastException e) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int h = 0;
        for (Node<K, V> node : storage) {
            while (node != null) {
                h += node.hashCode();
                node = node.next;
            }
        }
        return h;
    }

    @Override
    public String toString() {
        if (storage == null || size == 0)
            return "{}";

        Iterator<Entry<K, V>> iterator = entrySet().iterator();
        StringBuilder stringBuilder = new StringBuilder("{");

        while (true) {
            Entry<K, V> entry = iterator.next();
            K key = entry.getKey();
            V value = entry.getValue();
            stringBuilder.append(key == this ? "this" : key)
                    .append("=")
                    .append(value == this ? "this" : value);

            if (!iterator.hasNext()) {
                return stringBuilder.append("}").toString();
            }
            stringBuilder.append(", ");
        }
    }

    static class Node<K, V> implements Map.Entry<K, V> {
        private final int hash;
        private final K key;
        private V value;
        private Node<K, V> next;

        Node(int hash, K key, V value, Node<K, V> next) {
            this.hash = hash;
            this.key = key;
            this.value = value;
            this.next = next;
        }

        Node(int hash, K key, V value) {
            this.hash = hash;
            this.key = key;
            this.value = value;
        }

        @Override
        public K getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return value;
        }

        @Override
        public V setValue(V value) {
            V oldValue = this.value;
            this.value = value;
            return oldValue;
        }

        @Override
        public final boolean equals(Object o) {
            if (o == null) {
                return false;
            }
            if (o == this) {
                return true;
            }
            if (!(o instanceof Map.Entry<?, ?> entry)) return false;

            return Objects.equals(key, entry.getKey()) && Objects.equals(value, entry.getValue());
        }

        @Override
        public int hashCode() {
            int result = Objects.hashCode(key);
            result = 31 * result + Objects.hashCode(value);
            return result;
        }

        @Override
        public String toString() {
            return key + "=" + value;
        }
    }
}