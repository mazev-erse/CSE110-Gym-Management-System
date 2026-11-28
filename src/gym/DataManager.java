package gym;

import java.util.ArrayList;
import java.util.HashMap;

// Generic repository class demonstrating Generics (<T extends Identifiable>)
// and Java Collections (ArrayList and HashMap).
public class DataManager<T extends Identifiable> {
    private ArrayList<T> list = new ArrayList<>();
    private HashMap<String, T> map = new HashMap<>();

    public void add(T item) throws DuplicateIdException {
        String key = item.getId().trim().toUpperCase();
        if (map.containsKey(key)) {
            throw new DuplicateIdException("ID '" + item.getId() + "' already exists!");
        }
        list.add(item);
        map.put(key, item);
    }

    public T findById(String id) throws NotFoundException {
        String key = id.trim().toUpperCase();
        T item = map.get(key);
        if (item == null) {
            throw new NotFoundException("No record found with ID '" + id + "'.");
        }
        return item;
    }

    public void remove(String id) throws NotFoundException {
        T item = findById(id);
        list.remove(item);
        map.remove(id.trim().toUpperCase());
    }

    public boolean exists(String id) {
        return map.containsKey(id.trim().toUpperCase());
    }

    public ArrayList<T> getAll() {
        return list;
    }

    public void clear() {
        list.clear();
        map.clear();
    }

    public int size() {
        return list.size();
    }
}
