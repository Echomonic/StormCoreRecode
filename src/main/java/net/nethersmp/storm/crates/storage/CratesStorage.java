package net.nethersmp.storm.crates.storage;

import net.nethersmp.storm.crates.api.CrateData;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CratesStorage {

    private final HashMap<String, CrateData> crates = new HashMap<>();

    public void add(String id, CrateData data) {
        crates.put(id, data);
    }

    public void remove(String id) {
        crates.remove(id);
    }

    public CrateData get(String id) {
        return crates.get(id);
    }

    public void clear() {
        crates.clear();
    }

    public Set<Map.Entry<String, CrateData>> entries() {
        return crates.entrySet();
    }
}
