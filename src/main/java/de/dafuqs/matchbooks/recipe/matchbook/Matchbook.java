package de.dafuqs.matchbooks.recipe.matchbook;

import com.google.gson.*;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import java.util.*;

/**
 * Mode defines how nbt is filtered.
 * AND demands all matches return true to be considered a match.
 * OR demands only one match return true for such to happen.
 */
public class Matchbook {

    private static final Matchbook EMPTY = new Matchbook(new ArrayList<>(), Mode.PASS);
    private final List<Match> matches;
    private final Mode mode;

    private Matchbook(List<Match> matches, Mode mode) {
        this.matches = Collections.unmodifiableList(matches);
        this.mode = mode;
    }

    public boolean test(ItemStack stack) {
        if(mode == Mode.PASS) { // fast succeed to skip stack.getNbt()
            return true;
        }

        NbtCompound nbt = stack.getNbt();
        return switch (mode) {
            case AND -> matches.stream().allMatch(match -> match.matches(nbt));
            case OR -> matches.stream().anyMatch(match -> match.matches(nbt));
            default -> true;
        };
    }

    public boolean isEmpty() {
        return this == EMPTY || matches.isEmpty();
    }

    public static Matchbook empty() {
        return EMPTY;
    }

    public void write(PacketByteBuf buf) {
        if(isEmpty()) {
            buf.writeBoolean(true);
        } else {
            buf.writeBoolean(false);
            buf.writeInt(mode.ordinal());
            buf.writeInt(matches.size());
            matches.forEach(match -> match.writeInternal(buf));
        }
    }

    public static Matchbook fromByteBuf(PacketByteBuf buf) {
        if(buf.readBoolean()) {
            return empty();
        }

        var mode = Mode.values()[buf.readInt()];
        var size = buf.readInt();
        var list = new ArrayList<Match>();

        for (int i = 0; i < size; i++) {
            var name = buf.readString();

            var factoryOptional = MatchFactory.getForPacket(name);
            factoryOptional.ifPresent(factory -> list.add(factory.fromPacket(buf)));
        }

        return new Matchbook(list, mode);
    }

    public JsonObject toJson() {
        JsonObject main = new JsonObject();
        JsonArray matches = new JsonArray();
        this.matches.forEach(match -> matches.add(match.toJson()));
        main.add("matches", matches);
        main.add("mode", new JsonPrimitive(this.mode.name().toLowerCase()));
        return main;
    }

    public static class Builder {

        private final ArrayList<Match> matchHolder = new ArrayList<>();

        public void add(Match match) {
            matchHolder.add(match);
        }

        public Matchbook build(Mode mode) {
            return new Matchbook(matchHolder, mode);
        }

    }

    public enum Mode {
        AND,
        OR,
        PASS
    }

}
