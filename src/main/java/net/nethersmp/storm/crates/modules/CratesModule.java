package net.nethersmp.storm.crates.modules;

import net.nethersmp.storm.crates.commands.CrateCommandNodes;
import net.nethersmp.storm.crates.storage.CratesDataHandler;
import net.nethersmp.storm.crates.storage.CratesStorage;
import net.nethersmp.storm.module.api.Module;
import net.nethersmp.storm.module.api.Result;
import net.nethersmp.storm.utilities.modules.CommandsModule;
import net.nethersmp.storm.utilities.modules.ListenerModule;

import java.nio.file.Path;
import java.util.Set;

import static io.papermc.paper.command.brigadier.Commands.literal;

public class CratesModule implements Module<Void> {

    public static final String ID = "crates";
    public static final Set<String> DEPENDENCIES = Set.of("commands", "listeners");
    public static final int PRIORITY = 960;

    private final CommandsModule commands;
    private final ListenerModule events;
    private final CratesDataHandler loader;
    private final CratesStorage storage;


    public CratesModule(CommandsModule commands, ListenerModule events, Path path) {
        this.commands = commands;
        this.events = events;
        this.storage = new CratesStorage();
        this.loader = new CratesDataHandler(path, storage);
    }

    @Override
    public Result<Void> load() {
        loader.initialize();
        loader.read();

        commands.register(literal("crates")
                .then(CrateCommandNodes.open(loader, storage))
                .then(CrateCommandNodes.remove(loader, storage))
                .then(CrateCommandNodes.make(storage))
                .then(CrateCommandNodes.give(storage))
                .build());


        return Result.success();
    }

    @Override
    public void unload() {
    }

    @Override
    public String id() {
        return ID;
    }

    @Override
    public Set<String> dependencies() {
        return DEPENDENCIES;
    }

    @Override
    public int priority() {
        return PRIORITY;
    }
}
