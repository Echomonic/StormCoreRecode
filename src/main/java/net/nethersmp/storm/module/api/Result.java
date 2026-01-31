package net.nethersmp.storm.module.api;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jspecify.annotations.NonNull;

public sealed interface Result<T> permits Result.Success, Result.Warn, Result.Fail {
    record Success<T>(T value) implements Result<T> {
        @Override
        public @NonNull String toString() {
            return value.toString();
        }

        @Override
        public Component toComponent() {
            return MiniMessage.miniMessage().deserialize(value.toString()).colorIfAbsent(NamedTextColor.GREEN);
        }
    }

    record Fail<T>(String code, String message) implements Result<T> {

        @Override
        public @NonNull String toString() {
            return "[FAILED] (" + code() + ") " + message();
        }

        @Override
        public Component toComponent() {
            return Component.text("[FAILED]", NamedTextColor.RED)
                    .appendSpace()
                    .append(
                            Component.text("(", NamedTextColor.GRAY)
                                    .append(Component.text(code(), NamedTextColor.GRAY))
                                    .append(Component.text(")", NamedTextColor.GRAY))
                    )
                    .appendSpace()
                    .append(Component.text(message(), NamedTextColor.RED));
        }
    }

    record Warn<T>(String code, String message) implements Result<T> {
        @Override
        public @NonNull String toString() {
            return "[WARNING] (" + code() + ") " + message();
        }

        @Override
        public Component toComponent() {
            return Component.text("[WARNING]", NamedTextColor.YELLOW)
                    .appendSpace()
                    .append(
                            Component.text("(", NamedTextColor.GRAY)
                                    .append(Component.text(code(), NamedTextColor.GRAY))
                                    .append(Component.text(")", NamedTextColor.GRAY))
                    )
                    .appendSpace()
                    .append(Component.text(message(), NamedTextColor.RED));
        }
    }

    Component toComponent();

    default boolean failed() {
        return Result.failed(this);
    }

    default boolean warned() {
        return Result.warned(this);
    }

    default boolean succeeded() {
        return Result.succeeded(this);
    }

    static <T> Result<T> success(T value) {
        return new Success<>(value);
    }

    static <T> Result<T> success() {
        return new Success<>(null);
    }

    static <T> Result<T> fail(String code, String message) {
        return new Fail<>(code, message);
    }

    static <T> Result<T> warn(String code, String message) {
        return new Warn<>(code, message);
    }

    static boolean failed(Result<?> result) {
        return result instanceof Fail;
    }

    static boolean warned(Result<?> result) {
        return result instanceof Warn;
    }

    static boolean succeeded(Result<?> result) {
        return result instanceof Result.Success<?>;
    }
}
