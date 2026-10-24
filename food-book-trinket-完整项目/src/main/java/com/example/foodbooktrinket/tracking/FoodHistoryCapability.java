package com.example.foodbooktrinket.tracking;

import com.example.foodbooktrinket.FoodBookTrinket;
import net.minecraft.core.Direction;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * 食物历史的 Capability 实现。
 */
public final class FoodHistoryCapability {

    public static final Capability<IFoodHistory> FOOD_HISTORY =
            CapabilityManager.get(new CapabilityToken<>() {});

    /** 食物历史在玩家 persistentData 中使用的 NBT key。 */
    public static final String NBT_KEY = "FoodBookTrinketEatenFoods";

    private FoodHistoryCapability() {
    }

    /** 注册 Capability 到 AttachCapabilitiesEvent<Player>。 */
    public static void register(final net.minecraftforge.event.AttachCapabilitiesEvent<net.minecraft.world.entity.Entity> event) {
        if (event.getObject() instanceof net.minecraft.world.entity.player.Player player) {
            event.addCapability(
                    new ResourceLocation(FoodBookTrinket.MOD_ID, "food_history"),
                    new Provider(player.getPersistentData()));
        }
    }

    /** 便捷获取。 */
    public static IFoodHistory get(final net.minecraft.world.entity.player.Player player) {
        return player.getCapability(FOOD_HISTORY).orElse(IFoodHistory.Stub.INSTANCE);
    }

    /** 默认实现，数据存放在玩家的持久化 NBT 中。 */
    public static final class Impl implements IFoodHistory {

        private final net.minecraft.nbt.CompoundTag persistentData;
        private final Set<ResourceLocation> eatenFoods;

        public Impl(net.minecraft.nbt.CompoundTag persistentData) {
            this.persistentData = persistentData;
            this.eatenFoods = new HashSet<>();
            load();
        }

        private void load() {
            eatenFoods.clear();
            if (persistentData.contains(NBT_KEY, Tag.TAG_LIST)) {
                ListTag list = persistentData.getList(NBT_KEY, Tag.TAG_STRING);
                for (Tag tag : list) {
                    ResourceLocation rl = ResourceLocation.tryParse(tag.getAsString());
                    if (rl != null) {
                        eatenFoods.add(rl);
                    }
                }
            }
        }

        private void save() {
            ListTag list = new ListTag();
            for (ResourceLocation rl : eatenFoods) {
                list.add(StringTag.valueOf(rl.toString()));
            }
            persistentData.put(NBT_KEY, list);
        }

        @Override
        public boolean hasEaten(ResourceLocation food) {
            return eatenFoods.contains(food);
        }

        @Override
        public boolean markEaten(ResourceLocation food) {
            boolean added = eatenFoods.add(food);
            if (added) {
                save();
            }
            return added;
        }

        @Override
        public Set<ResourceLocation> getEatenFoods() {
            return Collections.unmodifiableSet(eatenFoods);
        }
    }

    /** Capability Provider，数据直接读写玩家持久化 NBT，无需额外序列化。 */
    public static final class Provider implements ICapabilityProvider {

        private final Impl impl;
        private final LazyOptional<IFoodHistory> lazy;

        public Provider(final net.minecraft.nbt.CompoundTag persistentData) {
            this.impl = new Impl(persistentData);
            this.lazy = LazyOptional.of(() -> impl);
        }

        @Override
        public @NotNull <T> LazyOptional<T> getCapability(
                @NotNull Capability<T> cap, @Nullable Direction side) {
            return cap == FOOD_HISTORY ? lazy.cast() : LazyOptional.empty();
        }
    }
}
