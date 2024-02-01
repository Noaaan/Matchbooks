package de.dafuqs.matchbooks.recipe.matchbook;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import de.dafuqs.matchbooks.recipe.RecipeParser;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;

public class ByteMatch extends Match {
    public static final String TYPE = "byte";

    private byte targetByte;

    public ByteMatch(String name, String key) {
        super(name, key);
    }

    @Override
    boolean matches(NbtCompound nbt) {
        if(nbt != null && nbt.contains(key)) {
            return nbt.getByte(key) == targetByte;
        }

        return false;
    }

    @Override
    void configure(JsonObject json) {
        targetByte = json.get(RecipeParser.TARGET).getAsByte();
    }

    @Override
    void configure(PacketByteBuf buf) {
        targetByte = buf.readByte();
    }

    @Override
    JsonObject toJson() {
        JsonObject main = new JsonObject();
        main.add(RecipeParser.TYPE, new JsonPrimitive(TYPE));
        main.add(RecipeParser.KEY, new JsonPrimitive(this.name));
        main.add(RecipeParser.TARGET, new JsonPrimitive(this.targetByte));
        return main;
    }

    @Override
    void write(PacketByteBuf buf) {
        buf.writeByte(targetByte);
    }

    public static class Factory extends MatchFactory<ByteMatch> {

        public Factory() {
            super(TYPE);
        }

        @Override
        public ByteMatch create(String key, JsonObject object) {
            var match = new ByteMatch(name, key);
            match.configure(object);

            return match;
        }

        @Override
        public ByteMatch fromPacket(PacketByteBuf buf) {
            var match = new ByteMatch(name, buf.readString());
            match.configure(buf);

            return match;
        }
    }

}
