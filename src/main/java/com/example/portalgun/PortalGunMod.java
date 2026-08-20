package com.example.portalgun;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod(PortalGunMod.MOD_ID)
public class PortalGunMod {
    public static final String MOD_ID = "portalgun";

    public static final DeferredRegister<Item> ITEMS = 
        DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);

    public static final DeferredRegister<EntityType<?>> ENTITIES = 
        DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MOD_ID);

    public static final RegistryObject<Item> PORTAL_GUN = ITEMS.register("portal_gun",
        () -> new PortalGunItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<EntityType<PortalEntity>> PORTAL_ENTITY = 
        ENTITIES.register("portal", () -> EntityType.Builder.<PortalEntity>of(PortalEntity::new, MobCategory.MISC)
            .sized(1.5f, 2.0f)
            .build("portal"));

    public PortalGunMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ITEMS.register(modEventBus);
        ENTITIES.register(modEventBus);
        modEventBus.addListener(this::addCreative);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(PORTAL_GUN);
        }
    }
}
