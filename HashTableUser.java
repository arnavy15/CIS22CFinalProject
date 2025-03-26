import java.util.ArrayList;

public class HashTableUser {
    private class Entry {
        String key;
        User value;
        Entry next;

        Entry(String key, User value) {
            this.key = key;
            this.value = value;
        }
    }

    private Entry[] table;
    private int size;

    public HashTableUser(int capacity) {
        table = new Entry[capacity];
        size = 0;
    }

    private int hash(String key) {
        return Math.abs(key.hashCode()) % table.length;
    }

    public void insert(String key, User value) {
        int index = hash(key);
        Entry e = table[index];
        while (e != null) {
            if (e.key.equals(key)) {
                // update
                e.value = value;
                return;
            }
            e = e.next;
        }
        // insert at head
        Entry newEntry = new Entry(key, value);
        newEntry.next = table[index];
        table[index] = newEntry;
        size++;
    }

    public User search(String key) {
        int index = hash(key);
        Entry e = table[index];
        while (e != null) {
            if (e.key.equals(key)) {
                return e.value;
            }
            e = e.next;
        }
        return null;
    }

    public ArrayList<User> getAllUsers() {
        ArrayList<User> all = new ArrayList<>();
        for (Entry bucket : table) {
            Entry current = bucket;
            while (current != null) {
                all.add(current.value);
                current = current.next;
            }
        }
        return all;
    }
}
