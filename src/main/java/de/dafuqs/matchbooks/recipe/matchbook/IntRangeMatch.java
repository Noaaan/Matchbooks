package de.dafuqs.matchbooks.recipe.matchbook;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import de.dafuqs.matchbooks.recipe.RecipeParser;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;

/**
 * Inclusive.
 */
public class IntRangeMatch extends Match {
    public static final String TYPE = "intRange";

    private int min;
    private int max;

    public IntRangeMatch(String name, String key) {
        super(name, key);
    }

    @Override
    boolean matches(NbtCompound nbt) {
        if(nbt != null && nbt.contains(key)) {
            var testInt = nbt.getInt(key);
            return max >= testInt && testInt >= min;
        }

        return false;
    }

    @Override
    void configure(JsonObject json) {
        min = json.get(RecipeParser.MIN).getAsInt();
        min = json.get(RecipeParser.MAX).getAsInt();
    }

    @Override
    void configure(PacketByteBuf buf) {
        min = buf.readInt();
        max = buf.readInt();
    }

    @Override
    JsonObject toJson() {
        JsonObject main = new JsonObject();
        main.add(RecipeParser.TYPE, new JsonPrimitive(TYPE));
        main.add(RecipeParser.KEY, new JsonPrimitive(this.name));
        main.add(RecipeParser.MIN, new JsonPrimitive(min));
        main.add(RecipeParser.MAX, new JsonPrimitive(max));
        return main;
    }

    @Override
    void write(PacketByteBuf buf) {
        buf.writeInt(min);
        buf.writeInt(max);
    }

    public static class Factory extends MatchFactory<IntRangeMatch> {

        public Factory() {
            super(TYPE);
        }

        @Override
        public IntRangeMatch create(String key, JsonObject object) {
            var match = new IntRangeMatch(name, key);
            match.configure(object);

            return match;
        }

        @Override
        public IntRangeMatch fromPacket(PacketByteBuf buf) {
            var match = new IntRangeMatch(name, buf.readString());
            match.configure(buf);

            return match;
        }
    }

}
