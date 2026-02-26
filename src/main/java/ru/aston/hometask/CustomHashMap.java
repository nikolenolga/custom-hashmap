package ru.aston.hometask;

import java.util.Map;
import java.util.Objects;

public class CustomHashMap<K, V> {
    static final int DEFAULT_CAPACITY = 1 << 4;
    static final int MAXIMUM_CAPACITY = 1 << 30;
    static final float DEFAULT_LOAD_FACTOR = 0.75f;
    protected final float loadFactor;
    protected Node<K, V>[] storage;
    protected int threshold;
    protected int size;

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

    protected int resolveIndex(Object key) {
        return (storage.length - 1) & hash(key);
    }

    protected int resolveIndex(int hash) {
        return (storage.length - 1) & hash;
    }

    protected int resolveIndex(int hash, int capacity) {
        return (capacity - 1) & hash;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public V get(K key) {
        Node<K, V> node = getNode(key);
        return node == null ? null : node.getValue();
    }

    private Node<K, V> getNode(Object key) {
        int hash = hash(key);
        Node<K, V> node;

        if (storage == null || isEmpty() || (node = storage[resolveIndex(key)]) == null) {
            return null;
        }

        while (node.hash != hash && key != node.key && !Objects.equals(key, node.key)) {
            if (node.isLast()) {
                return null;
            }

            node = node.next;
        }
        return node;
    }

    public V put(K key, V value) {
        Node<K, V>[] table = resizeIfNecessary();
        int hash = hash(key);
        int index = resolveIndex(hash);
        Node<K, V> node;

        if ((node = table[index]) == null) {
            table[index] = new Node<>(hash, key, value);
        } else {
            do {
                if (node.hash == hash && (key == node.key || Objects.equals(key, node.key))) {
                    V oldValue = node.value;
                    node.setValue(value);

                    return oldValue;
                }

                node = node.next;
            } while (node.next != null);

            node.next = new Node<>(hash, key, value);
        }

        size++;
        return null;
    }

    private Node<K, V>[] resizeIfNecessary() {
        Node<K, V>[] oldStorage = storage;
        int oldCapacity = (oldStorage == null) ? 0 : oldStorage.length;
        if (oldStorage != null && oldCapacity > 0 && (oldCapacity >= MAXIMUM_CAPACITY || size <= threshold)) {
            return oldStorage;
        }

        int newCapacity = calculateResizingCapacityAndSetNewThreshold(oldCapacity);

        @SuppressWarnings({"unchecked"})
        Node<K, V>[] newStorage = (Node<K, V>[]) new Node[newCapacity];
        storage = newStorage;

        if (oldStorage != null && size > 0) {
            System.out.printf("Resizing! size = %d, %d -> %d%n", size, oldCapacity, newCapacity);
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


    protected static class Node<K, V> {
        private final int hash;
        private final K key;
        private V value;
        private Node<K, V> next;

        protected Node(int hash, K key, V value, Node<K, V> next) {
            this.hash = hash;
            this.key = key;
            this.value = value;
            this.next = next;
        }

        protected Node(int hash, K key, V value) {
            this.hash = hash;
            this.key = key;
            this.value = value;
        }

        public K getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }

        public V setValue(V value) {
            V oldValue = this.value;
            this.value = value;
            return oldValue;
        }

        protected boolean isLast() {
            return next == null;
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
