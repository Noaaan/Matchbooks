package de.dafuqs.matchbooks.recipe.matchbook;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import de.dafuqs.matchbooks.recipe.RecipeParser;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;

public class IntMatch extends Match {
    public static final String TYPE = "int";

    private int targetInt;

    public IntMatch(String name, String key) {
        super(name, key);
    }

    @Override
    boolean matches(NbtCompound nbt) {
        if(nbt != null && nbt.contains(key)) {
            return nbt.getInt(key) == targetInt;
        }

        return false;
    }

    @Override
    void configure(JsonObject json) {
        targetInt = json.get(RecipeParser.TARGET).getAsInt();
    }

    @Override
    void configure(PacketByteBuf buf) {
        targetInt = buf.readInt();
    }

    @Override
    JsonObject toJson() {
        JsonObject main = new JsonObject();
        main.add(RecipeParser.TYPE, new JsonPrimitive(TYPE));
        main.add(RecipeParser.KEY, new JsonPrimitive(this.name));
        main.add(RecipeParser.TARGET, new JsonPrimitive(targetInt));
        return main;
    }

    @Override
    void write(PacketByteBuf buf) {
        buf.writeInt(targetInt);
    }

    public static class Factory extends MatchFactory<IntMatch> {

        public Factory() {
            super(TYPE);
        }

        @Override
        public IntMatch create(String key, JsonObject object) {
            var match = new IntMatch(name, key);
            match.configure(object);

            return match;
        }

        @Override
        public IntMatch fromPacket(PacketByteBuf buf) {
            var match = new IntMatch(name, buf.readString());
            match.configure(buf);

            return match;
        }
    }

}
