package com.example.foodbooktrinket.tracking;

import com.example.foodbooktrinket.FoodBookConstants;
import com.example.foodbooktrinket.item.FoodBookTrinketItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.UUID;

/**
 * 处理进食事件：当玩家吃完一种新食物时，为其攻击力属性累加永久加成。
 *
 * <p>攻击力加成通过给玩家实体附加一个永久 AttributeModifier 实现，
 * 修饰符的数值随已食用新食物数量动态更新。
 */
public final class FoodHistoryEvents {

    /** 攻击力加成修饰符的固定 UUID。 */
    private static final UUID ATTACK_MODIFIER_UUID =
            UUID.fromString("5f6d4c7e-8a2b-4c1d-9e3f-0a0b1c2d3e4f");

    private static final String ATTACK_MODIFIER_NAME = "food_book_trinket_attack_bonus";

    private FoodHistoryEvents() {
    }

    @SubscribeEvent
    public static void onItemUseFinish(final LivingEntityUseItemEvent.Finish event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        ItemStack stack = event.getItem();
        FoodProperties food = stack.getItem().getFoodProperties();
        if (food == null) {
            return; // 不是食物
        }

        // 仅服务端处理，属性同步交给 Minecraft 自带网络同步
        if (player.level().isClientSide()) {
            return;
        }

        ResourceLocation foodId = net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (foodId == null) {
            return;
        }

        // 必须先佩戴食物之书，进食新食物才会被记录并获得加成
        if (!FoodBookTrinketItem.isWornBy(player)) {
            return;
        }

        IFoodHistory history = FoodHistoryCapability.get(player);
        boolean isNewFood = history.markEaten(foodId);
        if (!isNewFood) {
            return; // 重复食用，无效
        }

        applyAttackBonus((ServerPlayer) player, history.getEatenFoods().size());
    }

    /**
     * 玩家登录/复活时，根据已持久化的食物历史重新应用攻击力加成，
     * 因为属性修饰符不会自动持久化到玩家数据。
     */
    @SubscribeEvent
    public static void onPlayerLogin(final PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            IFoodHistory history = FoodHistoryCapability.get(player);
            applyAttackBonus(player, history.getEatenFoods().size());
        }
    }

    /** 玩家从末地/跨维度 clone 后同步加成。 */
    @SubscribeEvent
    public static void onPlayerClone(final PlayerEvent.Clone event) {
        // 死亡/跨维度时新实体不会自动继承 persistentData，需要手动复制
        if (event.getOriginal() instanceof ServerPlayer original
                && event.getEntity() instanceof ServerPlayer newPlayer) {
            if (original.getPersistentData().contains(FoodHistoryCapability.NBT_KEY)) {
                newPlayer.getPersistentData().put(
                        FoodHistoryCapability.NBT_KEY,
                        original.getPersistentData().get(FoodHistoryCapability.NBT_KEY));
            }
            IFoodHistory history = FoodHistoryCapability.get(newPlayer);
            applyAttackBonus(newPlayer, history.getEatenFoods().size());
        }
    }

    /**
     * 根据已食用新食物数量(menu 中的种类数)重新计算并应用攻击力加成。
     */
    public static void applyAttackBonus(ServerPlayer player, int eatenFoodCount) {
        AttributeInstance attack = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attack == null) {
            return;
        }

        double bonus = eatenFoodCount * (double) FoodBookConstants.ATTACK_DAMAGE_PER_FOOD;

        // 先移除旧的加成修饰符
        attack.removeModifier(ATTACK_MODIFIER_UUID);

        if (bonus > 0) {
            AttributeModifier modifier = new AttributeModifier(
                    ATTACK_MODIFIER_UUID,
                    ATTACK_MODIFIER_NAME,
                    bonus,
                    AttributeModifier.Operation.ADDITION);
            attack.addPermanentModifier(modifier);
        }
    }
}
