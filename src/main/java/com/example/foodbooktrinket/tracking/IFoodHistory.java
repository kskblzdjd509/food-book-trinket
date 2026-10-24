package com.example.foodbooktrinket.tracking;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.HashSet;
import java.util.Set;

/**
 * 记录玩家已食用过的食物种类。
 *
 * <p>独立于食物之书饰品存在，只要吃到新食物就会记录。
 * 攻击力加成只有在玩家佩戴食物之书(FoodBookTrinketItem)时才会体现。
 */
public interface IFoodHistory {

    /** 当 Capability 不存在时的空壳实现。 */
    final class Stub implements IFoodHistory {
        public static final Stub INSTANCE = new Stub();

        @Override
        public boolean hasEaten(ResourceLocation food) {
            return false;
        }

        @Override
        public boolean markEaten(ResourceLocation food) {
            return true;
        }

        @Override
        public Set<ResourceLocation> getEatenFoods() {
            return java.util.Collections.emptySet();
        }
    }

    /** 检查某个食物是否已经被该玩家食用过。 */
    boolean hasEaten(ResourceLocation food);

    /** 记录食物，若为新食物返回 true，若已存在返回 false。 */
    boolean markEaten(ResourceLocation food);

    /** 返回当前已食用食物的不可变集合。 */
    Set<ResourceLocation> getEatenFoods();
}
