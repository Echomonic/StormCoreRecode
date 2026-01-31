package net.nethersmp.storm.crates.api;

import org.bukkit.inventory.ItemStack;

import java.util.concurrent.ConcurrentMap;

public record CrateData(String name, String color, ConcurrentMap<Integer, ItemStack> items) {

}
