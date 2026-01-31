package net.nethersmp.storm.utilities.modules;

import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import lombok.Getter;
import net.nethersmp.storm.module.api.Module;
import net.nethersmp.storm.module.api.Result;

import java.util.ArrayList;
import java.util.List;

public class CommandsModule implements Module<Void> {

    @Getter
    private final List<LiteralCommandNode<CommandSourceStack>> commandNodes = new ArrayList<>();

    @Override
    public String id() {
        return "commands";
    }

    @Override
    public int priority() {
        return 1000;
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
