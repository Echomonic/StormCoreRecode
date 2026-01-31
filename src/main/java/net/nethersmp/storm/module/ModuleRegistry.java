package net.nethersmp.storm.module;

import io.papermc.paper.command.brigadier.PaperCommands;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import net.nethersmp.storm.StormPlugin;
import net.nethersmp.storm.module.api.ModuleDefinition;
import net.nethersmp.storm.permission.modules.RankHandlerModule;
import net.nethersmp.storm.permission.modules.RankLoaderModule;
import net.nethersmp.storm.permission.modules.UserPermissionsModule;
import net.nethersmp.storm.punishment.modules.UserPunishmentModule;
import net.nethersmp.storm.utilities.modules.CommandsModule;
import net.nethersmp.storm.utilities.modules.ListenerModule;

import java.lang.reflect.Field;
import java.util.Set;

@RequiredArgsConstructor
public class ModuleRegistry {

    private final StormPlugin plugin;

    @Getter
    private ModuleLoader moduleLoader;

    private CommandsModule commandsModule;

    public void register() {
        moduleLoader = new ModuleLoader();

        commandsModule = new CommandsModule();

        moduleLoader.register(new ModuleDefinition<>("commands", Set.of(), 1000, access -> commandsModule));
        moduleLoader.register(new ModuleDefinition<>("listeners", Set.of(), 1000, access -> new ListenerModule(plugin)));

        moduleLoader.register(new ModuleDefinition<>("user_permissions",
                Set.of("listeners"),
                990,
                access -> new UserPermissionsModule(access.require("listeners", ListenerModule.class))));
        moduleLoader.register(new ModuleDefinition<>("user_ranks_loader",
                Set.of(),
                980,
                access -> new RankLoaderModule(plugin.getDataPath().resolve("ranks.json"))));

        moduleLoader.register(new ModuleDefinition<>("user_ranks_handler",
                Set.of("user_ranks_loader", "listeners", "commands"),
                970,
                access -> new RankHandlerModule(plugin,
                        access.require("user_ranks_loader", RankLoaderModule.class),
                        access.require("listeners", ListenerModule.class),
                        access.require("commands", CommandsModule.class))));

        moduleLoader.register(new ModuleDefinition<>(UserPunishmentModule.ID,
                UserPunishmentModule.DEPENDENCIES,
                UserPunishmentModule.PRIORITY,
                access -> new UserPunishmentModule(plugin,
                        access.require("commands", CommandsModule.class),
                        access.require("listeners", ListenerModule.class))));

    }


    @SneakyThrows
    public void registerCommands() {
        Field f = PaperCommands.INSTANCE.getClass().getDeclaredField("invalid");
        f.setAccessible(true);
        f.set(PaperCommands.INSTANCE, false);

        PaperCommands.INSTANCE.setCurrentContext(plugin);
        commandsModule.getCommandNodes().forEach(PaperCommands.INSTANCE::register);

        f.set(PaperCommands.INSTANCE, true);
        f.setAccessible(false);
    }

    public void cancel() {

    }
}
