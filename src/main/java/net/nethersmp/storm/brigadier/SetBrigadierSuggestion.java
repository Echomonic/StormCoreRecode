package net.nethersmp.storm.brigadier;


import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import lombok.RequiredArgsConstructor;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class SetBrigadierSuggestion implements SuggestionProvider<CommandSourceStack> {

    private final Set<String> set;

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSourceStack> commandContext, SuggestionsBuilder suggestionsBuilder) {
        String remaining = suggestionsBuilder.getRemaining().toLowerCase();

        for (String string : set) {
            if (string.toLowerCase().startsWith(remaining)) {
                suggestionsBuilder.suggest(string);
            }
        }
        return suggestionsBuilder.buildFuture();
    }

    public static SetBrigadierSuggestion keySuggestion(Set<String> set) {
        return new SetBrigadierSuggestion(set);
    }
}
