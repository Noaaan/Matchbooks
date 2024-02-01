package de.dafuqs.matchbooks.recipe;

import com.google.gson.*;
import de.dafuqs.matchbooks.recipe.matchbook.Matchbook;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.util.collection.DefaultedList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public final class IngredientStack {

    public static final IngredientStack EMPTY = new IngredientStack(Ingredient.EMPTY, Matchbook.empty(), Optional.empty(), 0);
    private final Ingredient ingredient;
    private final Matchbook matchbook;
    private final Optional<NbtCompound> recipeViewNbt;
    private final int count;

    private IngredientStack(@NotNull Ingredient ingredient, @NotNull Matchbook matchbook, Optional<NbtCompound> recipeViewNbt, int count) {
        this.ingredient = ingredient;
        this.matchbook = matchbook;
        this.recipeViewNbt = recipeViewNbt;
        this.count = count;
    }

    public static IngredientStack of(@NotNull Ingredient ingredient, @NotNull Matchbook matchbook, @Nullable NbtCompound recipeViewNbt, int count) {
        if(ingredient.isEmpty()) {
            return EMPTY;
        }
        return new IngredientStack(ingredient, matchbook, Optional.ofNullable(recipeViewNbt), count);
    }

    public static IngredientStack of(Ingredient ingredient) {
        return of(ingredient, Matchbook.empty(), null, 1);
    }

    public static IngredientStack ofItems(ItemConvertible... items) {
        return of(Ingredient.ofItems(items), Matchbook.empty(), null, 1);
    }

    public static IngredientStack ofItems(int count, ItemConvertible... items) {
        return of(Ingredient.ofItems(items), Matchbook.empty(), null, count);
    }

    public static IngredientStack ofStacks(ItemStack... stacks) {
        return of(Ingredient.ofStacks(stacks), Matchbook.empty(), null, 1);
    }

    public static IngredientStack ofStacks(int count, ItemStack... stacks) {
        return of(Ingredient.ofStacks(stacks), Matchbook.empty(), null, count);
    }

    public boolean test(ItemStack stack) {
        return ingredient.test(stack) && stack.getCount() >= count && matchbook.test(stack);
    }

    public boolean testStrict(ItemStack stack) {
        return ingredient.test(stack) && stack.getCount() == count && matchbook.test(stack);
    }

    public boolean testCountless(ItemStack stack) {
        return ingredient.test(stack) && matchbook.test(stack);
    }

    public Matchbook getMatchbook() {
        return  matchbook;
    }

    public void write(PacketByteBuf buf) {
        ingredient.write(buf);
        matchbook.write(buf);
        buf.writeBoolean(recipeViewNbt.isPresent());
        recipeViewNbt.ifPresent(buf::writeNbt);
        buf.writeInt(count);
    }

    public JsonElement toJson() {
        JsonObject main = new JsonObject();
        main.add("ingredient", this.ingredient.toJson());
        if (this.count > 1) main.add(RecipeParser.COUNT, new JsonPrimitive(this.count));
        if (!this.matchbook.isEmpty()) main.add(RecipeParser.MATCHBOOK, this.matchbook.toJson());
        if (this.recipeViewNbt.isPresent()) main.add("recipeViewNbt", RecipeParser.asJson(recipeViewNbt.get()));
        return main;
    }

    public static IngredientStack fromByteBuf(PacketByteBuf buf) {
        return new IngredientStack(Ingredient.fromPacket(buf), Matchbook.fromByteBuf(buf), buf.readBoolean() ? Optional.ofNullable(buf.readNbt()) : Optional.empty(), buf.readInt());
    }

    public static List<IngredientStack> decodeByteBuf(PacketByteBuf buf, int size) {
        List<IngredientStack> ingredients = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            ingredients.add(fromByteBuf(buf));
        }
        return ingredients;
    }

    public List<ItemStack> getStacks() {
        var stacks = ingredient.getMatchingStacks();

        if (stacks == null)
            return new ArrayList<>();

        return Arrays.stream(stacks)
                .peek(stack -> stack.setCount(count))
                .peek(stack -> recipeViewNbt.ifPresent(stack::setNbt))
                .collect(Collectors.toList());
    }

    public Ingredient getIngredient() {
        return ingredient;
    }

    public int getCount() {
        return count;
    }

    public boolean isEmpty() {
        return this == EMPTY || ingredient.isEmpty();
    }

    public static DefaultedList<Ingredient> listIngredients(List<IngredientStack> ingredients) {
        DefaultedList<Ingredient> preview = DefaultedList.ofSize(ingredients.size(), Ingredient.EMPTY);
        for (int i = 0; i < ingredients.size(); i++) {
            preview.set(i, ingredients.get(i).getIngredient());
        }
        return preview;
    }


    public static boolean matchInvExclusively(Inventory inv, List<IngredientStack> ingredients, int size, int offset) {
        List<ItemStack> invStacks = new ArrayList<>(size);
        for (int i = offset; i < size + offset; i++) {
            invStacks.add(inv.getStack(i));
        }
        AtomicInteger matches = new AtomicInteger();
        ingredients.forEach(ingredient -> {
            for (int i = 0; i < invStacks.size(); i++) {
                if(ingredient.isEmpty()) {
                    matches.getAndIncrement();
                    break;
                }
                ItemStack stack = invStacks.get(i);
                if(ingredient.test(stack)) {
                    matches.getAndIncrement();
                    invStacks.remove(i);
                    break;
                }
            }
        });
        return matches.get() == size;
    }

    public static void decrementExclusively(Inventory inv, List<IngredientStack> ingredients, int size, int offset) {
        List<ItemStack> invStacks = new ArrayList<>(size);
        for (int i = offset; i < size + offset; i++) {
            invStacks.add(inv.getStack(i));
        }
        ingredients.forEach(ingredient -> {
            for (int i = 0; i < invStacks.size(); i++) {
                if(ingredient.isEmpty()) {
                    break;
                }
                ItemStack stack = invStacks.get(i);
                if(ingredient.test(stack)) {
                    stack.decrement(ingredient.count);
                    invStacks.remove(i);
                    break;
                }
            }
        });
    }
}
