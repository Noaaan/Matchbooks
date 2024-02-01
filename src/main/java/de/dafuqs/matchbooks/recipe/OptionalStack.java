package de.dafuqs.matchbooks.recipe;

import de.dafuqs.matchbooks.Matchbooks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class OptionalStack {

    public static final TagKey<Item> EMPTY_FOLLY = TagKey.of(Registries.ITEM.getKey(), Matchbooks.id("empty_item_folly"));
    public static final OptionalStack EMPTY = new OptionalStack(EMPTY_FOLLY, 0);

    @NotNull
    private final Optional<TagKey<Item>> tag;
    @NotNull
    private final ItemStack stack;
    private final int count;
    private static final Registry<Item> REGISTRY = Registries.ITEM;


    private List<ItemStack> cachedStacks = null;

    public OptionalStack(@NotNull TagKey<Item> tag, int count) {
        this.tag = Optional.of(tag);
        this.stack = ItemStack.EMPTY;
        this.count = count;
    }

    public OptionalStack(@NotNull ItemStack stack, int count) {
        this.stack = stack;
        this.tag = Optional.empty();
        this.count = count;
    }

    public void write(PacketByteBuf buf) {
        getStacks();
        buf.writeInt(cachedStacks.size());
        for (ItemStack cachedStack : cachedStacks) {
            buf.writeItemStack(cachedStack);
        }
        buf.writeInt(count);
    }

    public static OptionalStack fromByteBuf(PacketByteBuf buf) {
        List<ItemStack> stacks = new ArrayList<>();
        int size = buf.readInt();
        for (int i = 0; i < size; i++) {
            stacks.add(buf.readItemStack());
        }
        OptionalStack folly = new OptionalStack(ItemStack.EMPTY, buf.readInt());
        folly.cachedStacks = stacks;
        return folly;
    }

    public static List<OptionalStack> decodeByteBuf(PacketByteBuf buf, int size) {
        List<OptionalStack> stacks = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            stacks.add(fromByteBuf(buf));
        }
        return stacks;
    }

    public boolean isEmpty() {
        return this == EMPTY || (tag.map(RegistryHelper::isTagEmpty).orElse(true) && stack.isEmpty() && cachedStacks.isEmpty());
    }

    public List<ItemStack> getStacks() {
        assert !isEmpty() : "Can't access an empty OptionalStack! Did you check if it was empty first?";
        if(cachedStacks == null) {
            if((tag.map(RegistryHelper::isTagEmpty).orElse(true))) {
                cachedStacks = Collections.singletonList(stack);
            } else {
                var entries = RegistryHelper.getEntries(tag.get());
                entries.ifPresent(registryEntries -> cachedStacks = registryEntries.stream().map(RegistryEntry::value).map(item -> new ItemStack(item, count)).collect(Collectors.toList()));
            }
        }
        return cachedStacks;
    }

    public int getCount() {
        return count;
    }

    public @Nullable ItemStack getFirstStack() {
        if(cachedStacks == null)
            return getStacks().get(0);
        else
            return cachedStacks.get(0);
    }

    public boolean itemMatch(ItemStack stack) {
        if(cachedStacks == null)
            getStacks();
        if(cachedStacks.isEmpty()) {
            return false;
        }
        else
            return cachedStacks.stream().anyMatch(testStack -> ItemStack.areItemsEqual(testStack, stack));
    }

    public boolean contains(ItemStack stack) {
        if(cachedStacks == null)
            getStacks();
        if(cachedStacks.isEmpty()) {
            return false;
        }
        else
            return cachedStacks.stream().anyMatch(testStack -> ItemStack.areItemsEqual(testStack, stack) && stack.getCount() >= testStack.getCount());
    }
}
