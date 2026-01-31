package net.nethersmp.storm.punishment.modules;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import io.papermc.paper.event.player.AsyncChatEvent;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.nethersmp.storm.StormPlugin;
import net.nethersmp.storm.module.api.Module;
import net.nethersmp.storm.module.api.Result;
import net.nethersmp.storm.punishment.UserPunishment;
import net.nethersmp.storm.punishment.UserPunishmentAccessor;
import net.nethersmp.storm.punishment.api.PunishmentDuration;
import net.nethersmp.storm.punishment.api.PunishmentId;
import net.nethersmp.storm.punishment.api.PunishmentReason;
import net.nethersmp.storm.punishment.api.PunishmentType;
import net.nethersmp.storm.user.data.UserPunishmentDataType;
import net.nethersmp.storm.utilities.modules.CommandsModule;
import net.nethersmp.storm.utilities.modules.ListenerModule;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;

import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.mojang.brigadier.arguments.StringArgumentType.string;
import static io.papermc.paper.command.brigadier.Commands.argument;
import static io.papermc.paper.command.brigadier.Commands.literal;
import static io.papermc.paper.command.brigadier.argument.ArgumentTypes.player;
import static net.nethersmp.storm.brigadier.SetBrigadierSuggestion.keySuggestion;

@RequiredArgsConstructor
public class UserPunishmentModule implements Module<Void> {

    public static final String ID = "user_punishments";
    public static final Set<String> DEPENDENCIES = Set.of("commands", "listeners");
    public static final int PRIORITY = 960;

    private final StormPlugin plugin;
    private final CommandsModule commands;
    private final ListenerModule events;

    @Override
    public Result<Void> load() {
        events.listen(ID, AsyncChatEvent.class, EventPriority.HIGH, event -> {
            Player player = event.getPlayer();
            UserPunishmentAccessor.get(player.getUniqueId()).ifPresent(punishment -> {
                if (!punishment.type().isMute()) return;

                if (punishment.duration().isOver()) {
                    updatePunishments(player.getUniqueId(), punishment.id().text());
                    return;
                }

                event.setCancelled(true);

                boolean permanent = punishment.type().isPermanent();

                MiniMessage miniMessage = MiniMessage.miniMessage();

                TextColor bodyColor = TextColor.fromHexString("#ff3b15");

                long timeRemaining = punishment.duration().future() - System.currentTimeMillis();

                Component header = miniMessage.deserialize("<#ff0025>You have been muted!").appendNewline();
                Component reason = miniMessage.deserialize("    <gray>Reason ▪ ").append(Component.text(punishment.reason().text(), bodyColor)).appendNewline();
                Component duration = miniMessage.deserialize("    <gray>Duration ▪ ").append(
                        Component.text(permanent ? "Forever" : formatDate(timeRemaining), bodyColor)
                ).appendNewline();
                Component punishmentId = miniMessage.deserialize("    <gray>Id ▪ ").append(Component.text(punishment.id().text(), bodyColor));

                player.sendMessage(header.append(reason).append(duration).append(punishmentId));
            });
        });
        //This should be login, but bukkits event system.
        events.listen(ID, PlayerJoinEvent.class, event -> {
            Player player = event.getPlayer();

            UserPunishmentAccessor.get(player.getUniqueId()).ifPresent(punishment -> {
                if (!punishment.type().isBan()) return;

                if (punishment.duration().isOver()) {
                    updatePunishments(player.getUniqueId(), punishment.id().text());
                    return;
                }
                player.kick(buildBanMessage(punishment));
            });
        });

        commands.register(literal("punish").requires(context -> context.getSender().hasPermission("stormcore.punish"))
                .then(literal("mute").requires(context -> context.getSender().hasPermission("stormcore.punish.mute"))
                        .then(handlePermissionCommand("mute")))
                .then(literal("ban").requires(context -> context.getSender().hasPermission("stormcore.punish.ban"))
                        .then(handlePermissionCommand("ban")))
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

    private ArgumentBuilder<CommandSourceStack, ?> handlePermissionCommand(String type) {
        return argument("target", player()).then(argument("reason", string())
                .then(argument("time", string()).suggests(keySuggestion(Set.of("perm", "4d", "3h", "2m", "1s"))).executes(context -> {
                    CommandSourceStack source = context.getSource();
                    CommandSender sender = source.getSender();
                    final PlayerSelectorArgumentResolver targetResolver = context.getArgument("target", PlayerSelectorArgumentResolver.class);

                    final Player target = targetResolver.resolve(source).getFirst();
                    final String reason = context.getArgument("reason", String.class);
                    final String duration = context.getArgument("time", String.class);

                    boolean permanent = duration.equalsIgnoreCase("perm");

                    var future = permanent ? 0L : convertTimeSegment(duration);

                    PunishmentType punishmentType = PunishmentType.valueOf(MessageFormat.format("{0}_{1}",
                            permanent ? "PERMANENT" : "TEMPORARY",
                            type.toUpperCase()));

                    UserPunishment theirLoss = new UserPunishment(
                            PunishmentId.create(),
                            punishmentType,
                            PunishmentReason.of(reason),
                            PunishmentDuration.of(future)
                    );

                    UserPunishmentAccessor.write(theirLoss);
                    UserPunishmentDataType.CURRENT_PUNISHMENT.set(target.getUniqueId(), theirLoss.id().text());

                    if (punishmentType.isBan()) {
                        target.kick(buildBanMessage(theirLoss));
                    }
                    sender.sendRichMessage("<gray>Their <yellow>%s</yellow> will expire on: <green>%s</green>".formatted(type.toLowerCase(),
                            new Date(future)));
                    return Command.SINGLE_SUCCESS;
                }))
        );
    }

    private void updatePunishments(UUID player, String oldPunishmentId) {
        UserPunishmentDataType.CURRENT_PUNISHMENT.set(player, "");

        List<String> oldPunishments = UserPunishmentDataType.OLD_PUNISHMENTS.getOrDefault(player, new ArrayList<>());
        oldPunishments.add(oldPunishmentId);

        UserPunishmentDataType.OLD_PUNISHMENTS.set(player, oldPunishments);
    }

    private long convertTimeSegment(String timeSegment) {
        String[] words = timeSegment.replace(" ", ",").split(",");
        long years = 0L;
        long months = 0L;
        long days = 0L;
        long hours = 0L;
        long minutes = 0L;
        long seconds = 0L;

        for (String word : words) {
            long num = Long.parseLong(word.substring(0, word.length() - 1));
            char lastChar = word.charAt(word.length() - 1);
            switch (lastChar) {
                case 'y':
                    years = num * 31557600000L;
                    break;
                case 'M':
                    months = num * 2629800000L;
                    break;
                case 'd':
                    days = num * 86400000L;
                    break;
                case 'h':
                    hours = num * 3600000L;
                    break;
                case 'm':
                    minutes = num * 60000L;
                    break;
                case 's':
                    seconds = num * 1000L;
                    break;
            }
        }
        return System.currentTimeMillis() + (years + months + days + hours + minutes + seconds);
    }

    private Component buildBanMessage(UserPunishment punishment) {
        if (!punishment.type().name().endsWith("BAN")) return Component.text("");

        boolean permanent = punishment.type().isPermanent();
        MiniMessage miniMessage = MiniMessage.miniMessage();

        TextColor bodyColor = TextColor.fromHexString("#ff3b15");

        long timeRemaining = punishment.duration().future() - System.currentTimeMillis();

        Component header = miniMessage.deserialize("<#ff0025>You have been banned from this network.").appendNewline();
        Component reason = miniMessage.deserialize("<gray>Reason ▪ ").append(Component.text(punishment.reason().text(), bodyColor)).appendNewline();
        Component duration = miniMessage.deserialize("<gray>Duration ▪ ").append(
                Component.text(permanent ? "Forever" : formatDate(timeRemaining), bodyColor)
        ).appendNewline();

        Component punishmentId = miniMessage.deserialize("<gray>Id ▪ ").append(Component.text(punishment.id().text(), bodyColor));
        return header.append(reason).append(duration).append(punishmentId);
    }

    private String formatDate(long time) {

        var days = TimeUnit.MILLISECONDS.toDays(time);
        var hours = TimeUnit.MILLISECONDS.toHours(time) % 24;
        var minutes = TimeUnit.MILLISECONDS.toMinutes(time) % 60;
        var seconds = TimeUnit.MILLISECONDS.toSeconds(time) % 60;

        return MessageFormat.format("{0}d {1}h {2}m {3}s", days, hours, minutes, seconds);
    }
}
