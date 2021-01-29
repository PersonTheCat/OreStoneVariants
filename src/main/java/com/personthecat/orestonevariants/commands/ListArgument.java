package com.personthecat.orestonevariants.commands;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

// Todo: Testing whether it would just be easier to register commands with any number of repeated arguments instead.
public class ListArgument<T, A extends ArgumentType<T>> implements ArgumentType<List<T>> {

    @Nullable private final String terminator;
    @NotNull private final A type;

    protected ListArgument(@NotNull A type, @Nullable String terminator) {
        this.type = type;
        this.terminator = terminator;
    }

    @Override
    public List<T> parse(StringReader reader) throws CommandSyntaxException {
        final List<T> list = new ArrayList<>();

        while (reader.canRead()) {
            final String t = reader.readStringUntil(' ');
            if (t.equals(terminator)) {
                return list;
            }
            list.add(type.parse(new StringReader(t)));
        }
        return list;
    }

    @Override
    public Collection<String> getExamples() {
        return type.getExamples();
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        final String full = context.getInput().substring(builder.getStart());
        final int space = full.lastIndexOf(' ') + 1;
        final String before = full.substring(0, space);
        final SuggestionsBuilder offset = builder.createOffset(builder.getStart() + space);

        if (terminator != null) {
            builder.suggest(before + terminator);
        }
        return join(context.getInput(), builder.buildFuture(), type.listSuggestions(context, offset));
    }

    @SafeVarargs
    private final CompletableFuture<Suggestions> join(String command, CompletableFuture<Suggestions>... futures) {
        return CompletableFuture.supplyAsync(() -> {
            final List<Suggestions> suggestions =  new ArrayList<>();
            for (CompletableFuture<Suggestions> future : futures) {
                suggestions.add(future.join());
            }
            return Suggestions.merge(command, suggestions);
        });
    }
}
