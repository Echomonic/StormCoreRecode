package net.nethersmp.storm.utilities.modules;

import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import lombok.Getter;
import net.nethersmp.storm.module.api.Module;
import net.nethersmp.storm.module.api.Result;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class CommandsModule implements Module<Void> {

    public static final String ID = "commands";
    public static final Set<String> DEPENDENCIES = Set.of();
    public static final int PRIORITY = 1000;

    @Getter
    private final List<LiteralCommandNode<CommandSourceStack>> commandNodes = new ArrayList<>();

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

    @Override
    public Result<Void> load() {
        return Result.success();
    }

    public void register(LiteralCommandNode<CommandSourceStack> node) {
        commandNodes.add(node);
    }

    @Override
    public void unload() {

    }
}
