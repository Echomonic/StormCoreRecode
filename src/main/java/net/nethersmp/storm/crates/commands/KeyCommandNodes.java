package net.nethersmp.storm.crates.commands;

import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import net.nethersmp.storm.user.data.UserCrateKeyDataType;
import net.nethersmp.storm.user.data.api.UserDataKey;
import net.nethersmp.storm.utilities.Strings;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static io.papermc.paper.command.brigadier.Commands.argument;
import static io.papermc.paper.command.brigadier.Commands.literal;

public class KeyCommandNodes {

    public static LiteralCommandNode<CommandSourceStack> base() {

        return literal("keys").requires(source -> source.getSender().hasPermission("stormcore.crates.keys"))
                .then(get())
                .then(set())
                .build();
    }

    private static LiteralCommandNode<CommandSourceStack> get() {

        return literal("get").requires(source -> source.getSender().hasPermission("stormcore.crates.keys.get"))
                .then(argument("target", ArgumentTypes.player()).then(argument("key-type", word()).executes(context -> {
                    CommandSourceStack source = context.getSource();
                    CommandSender sender = source.getSender();

                    final PlayerSelectorArgumentResolver targetResolver = context.getArgument("target", PlayerSelectorArgumentResolver.class);
                    final Player target = targetResolver.resolve(source).getFirst();

                    final String type = getString(context, "key-type").toLowerCase();

                    UserDataKey<Integer> userCrateKey = UserCrateKeyDataType.isStandardType(type) ? UserCrateKeyDataType.getStandardType(type) : UserCrateKeyDataType.template(type);
                    int crateKeyAmount = userCrateKey.get(target.getUniqueId());

                    sender.sendRichMessage("<gray><yellow>%s</yellow> has <aqua>%s</aqua> <green>%s Crate</green> keys.".formatted(target.getName(), crateKeyAmount, Strings.fixCase(type)));
                    return 1;
                }))).build();
    }

    private static LiteralCommandNode<CommandSourceStack> set() {

        return literal("set").requires(source -> source.getSender().hasPermission("stormcore.crates.keys.set"))
                .then(argument("target", ArgumentTypes.player()).then(argument("key-type", word()).then(argument("amount", integer())
                        .executes(context -> {
                            CommandSourceStack source = context.getSource();
                            CommandSender sender = source.getSender();

                            final PlayerSelectorArgumentResolver targetResolver = context.getArgument("target", PlayerSelectorArgumentResolver.class);
                            final Player target = targetResolver.resolve(source).getFirst();

                            final String type = getString(context, "key-type").toLowerCase();
                            final int amount = getInteger(context, "amount");
                            UserDataKey<Integer> userCrateKey = UserCrateKeyDataType.isStandardType(type) ? UserCrateKeyDataType.getStandardType(type) : UserCrateKeyDataType.template(type);

                            userCrateKey.set(target.getUniqueId(), amount);

                            sender.sendRichMessage("<gray><green>Set</green> <yellow>%s</yellow> <aqua>%s Crate</aqua> keys to <light_purple>%s</light_purple>."
                                    .formatted(target.getName(), Strings.fixCase(type), amount)
                            );
                            return 1;
                        })))).build();
    }
}
