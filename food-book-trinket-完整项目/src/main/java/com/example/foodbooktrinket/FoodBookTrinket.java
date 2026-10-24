package com.example.foodbooktrinket;

import com.example.foodbooktrinket.item.FoodBookTrinketItem;
import com.example.foodbooktrinket.tracking.FoodHistoryCapability;
import com.example.foodbooktrinket.tracking.FoodHistoryEvents;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * 食物之书模组：一件 Curios 配饰，玩家每食用一种新食物，
 * 佩戴状态下永久提升 0.2 攻击力，重复食用无效。
 */
@Mod(FoodBookTrinket.MOD_ID)
public class FoodBookTrinket {

    public static final String MOD_ID = "foodbooktrinket";

    private static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);

    public static final RegistryObject<Item> FOOD_BOOK =
            ITEMS.register("food_book", FoodBookTrinketItem::new);

    public FoodBookTrinket() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        ITEMS.register(modBus);

        // AttachCapability 注册到 mod 总线
        modBus.addListener(FoodHistoryCapability::register);

        // 事件监听注册到 Forge 总线
        MinecraftForge.EVENT_BUS.register(FoodHistoryEvents.class);
    }
}
