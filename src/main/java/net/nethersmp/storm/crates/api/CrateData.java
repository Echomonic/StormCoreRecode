package net.nethersmp.storm.crates.api;

import lombok.AllArgsConstructor;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.ConcurrentMap;


@AllArgsConstructor
public class CrateData {

    private String name;
    private String color;
    private ConcurrentMap<Integer, ItemStack> items;

    public String name() {
        return name;
    }

    public String color() {
        return color;
    }

    public ConcurrentMap<Integer, ItemStack> items() {
        return items;
    }

    public void items(ConcurrentMap<Integer, ItemStack> items) {
        this.items = items;
    }
}
