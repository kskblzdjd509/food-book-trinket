package com.example.foodbooktrinket.item;

import com.example.foodbooktrinket.FoodBookTrinket;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.List;

/**
 * 食物之书饰品：佩戴在 Curios 的 "charm"(配饰) 栏位。
 *
 * <p>属性加成逻辑在 {@link com.example.foodbooktrinket.tracking.FoodHistoryEvents}，
 * 本类负责 Curios 集成、限制为 charm(配饰)栏位与 tooltip 展示。
 */
public class FoodBookTrinketItem extends net.minecraft.world.item.Item implements ICurioItem {

    /** Curios 中"配饰"栏位的标识符。 */
    private static final String CHARM_SLOT = "charm";

    public FoodBookTrinketItem() {
        super(new Properties()
                .stacksTo(1)
                .fireResistant());
    }

    /**
     * 检测玩家当前是否在 "charm"(配饰) 栏位佩戴了食物之书。
     */
    public static boolean isWornBy(Player player) {
        return top.theillusivec4.curios.api.CuriosApi.getCuriosInventory(player)
                .map(inv -> inv.findFirstCurio(FoodBookTrinket.FOOD_BOOK.get()).isPresent())
                .orElse(false);
    }

    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        return true;
    }

    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        // 仅允许装备到 charm(配饰) 栏位
        return CHARM_SLOT.equals(slotContext.identifier());
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.foodbooktrinket.food_book.line1")
                .withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("tooltip.foodbooktrinket.food_book.line2")
                .withStyle(ChatFormatting.GRAY));
    }
}
