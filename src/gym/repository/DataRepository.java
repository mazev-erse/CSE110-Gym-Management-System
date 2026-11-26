package gym.repository;

import gym.exception.DuplicateEntityException;
import gym.exception.EntityNotFoundException;
import gym.model.Identifiable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Generic in-memory repository for managing Identifiable entities.
 * Demonstrates:
 * - Bounded Generics <T extends Identifiable> (Rubric Criterion 3)
 * - Java Collections: Map (LinkedHashMap/HashMap) + List (ArrayList) (Requirement 3)
 */
public class DataRepository<T extends Identifiable> {
    private final Map<String, T> storage = new LinkedHashMap<>();

    public void add(T item) throws DuplicateEntityException {
        String key = normalizeKey(item.getId());
        if (storage.containsKey(key)) {
            throw new DuplicateEntityException("An entry with ID '" + item.getId() + "' already exists.");
        }
        storage.put(key, item);
    }

    public void update(T item) throws EntityNotFoundException {
        String key = normalizeKey(item.getId());
        if (!storage.containsKey(key)) {
            throw new EntityNotFoundException("Cannot update: ID '" + item.getId() + "' was not found.");
        }
        storage.put(key, item);
    }

    public void remove(String id) throws EntityNotFoundException {
        String key = normalizeKey(id);
        if (!storage.containsKey(key)) {
            throw new EntityNotFoundException("Cannot delete: ID '" + id + "' was not found.");
        }
        storage.remove(key);
    }

    public T findById(String id) throws EntityNotFoundException {
        String key = normalizeKey(id);
        T item = storage.get(key);
        if (item == null) {
            throw new EntityNotFoundException("Record with ID '" + id + "' does not exist.");
        }
        return item;
    }

    public boolean exists(String id) {
        if (id == null) {
            return false;
        }
        return storage.containsKey(normalizeKey(id));
    }

    public List<T> getAll() {
        return new ArrayList<>(storage.values());
    }

    public void clear() {
        storage.clear();
    }

    public int count() {
        return storage.size();
    }

    private String normalizeKey(String id) {
        return id == null ? "" : id.trim().toUpperCase();
    }
}
