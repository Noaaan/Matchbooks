package de.dafuqs.matchbooks.recipe.matchbook;

import com.google.gson.JsonObject;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;

/**
 * Tests ItemStack nbt against a set of rules. Defined by json, must be able to be loaded from a bytebuf.
 */
public abstract class Match {

    protected final String name;
    protected final String key;

    protected Match(String name, String key) {
        this.name = name;
        this.key = key;
    }

    public String getName() {
        return name;
    }

    abstract boolean matches(NbtCompound nbt);

    abstract void configure(JsonObject json);

    abstract void configure(PacketByteBuf buf);

    abstract JsonObject toJson();

    public void writeInternal(PacketByteBuf buf) {
        buf.writeString(name);
        buf.writeString(key);
        write(buf);
    }

    abstract void write(PacketByteBuf buf);

}
