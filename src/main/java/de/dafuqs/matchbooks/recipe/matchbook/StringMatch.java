package de.dafuqs.matchbooks.recipe.matchbook;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import de.dafuqs.matchbooks.recipe.RecipeParser;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;

public class StringMatch extends Match {
    public static final String TYPE = "string";

    private String targetString;

    public StringMatch(String name, String key) {
        super(name, key);
    }

    @Override
    boolean matches(NbtCompound nbt) {
        if(nbt != null && nbt.contains(key)) {
            return nbt.getString(key).equals(targetString);
        }

        return false;
    }

    @Override
    void configure(JsonObject json) {
        targetString = json.get(RecipeParser.TARGET).getAsString();
    }

    @Override
    void configure(PacketByteBuf buf) {
        targetString = buf.readString();
    }

    @Override
    JsonObject toJson() {
        JsonObject main = new JsonObject();
        main.add(RecipeParser.TYPE, new JsonPrimitive(TYPE));
        main.add(RecipeParser.KEY, new JsonPrimitive(this.name));
        main.add(RecipeParser.TARGET, new JsonPrimitive(targetString));
        return main;
    }

    @Override
    void write(PacketByteBuf buf) {
        buf.writeString(targetString);
    }

    public static class Factory extends MatchFactory<StringMatch> {

        public Factory() {
            super(TYPE);
        }

        @Override
        public StringMatch create(String key, JsonObject object) {
            var match = new StringMatch(name, key);
            match.configure(object);

            return match;
        }

        @Override
        public StringMatch fromPacket(PacketByteBuf buf) {
            var match = new StringMatch(name, buf.readString());
            match.configure(buf);

            return match;
        }
    }

}
