package com.cazsius.solcarrot.tracking;

import com.cazsius.solcarrot.SOLCarrot;
import com.cazsius.solcarrot.SOLCarrotConfig;
import com.cazsius.solcarrot.api.FoodCapability;
import com.cazsius.solcarrot.communication.FoodListMessage;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.network.NetworkDirection;

import static net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus.MOD;

@Mod.EventBusSubscriber(modid = SOLCarrot.MOD_ID)
public final class CapabilityHandler {
	private static final ResourceLocation FOOD = SOLCarrot.resourceLocation("food");
	
	@Mod.EventBusSubscriber(modid = SOLCarrot.MOD_ID, bus = MOD)
	private static final class Setup {
		@SubscribeEvent
		public static void setUp(FMLCommonSetupEvent event) {
			CapabilityManager.INSTANCE.register(FoodCapability.class, new FoodList.Storage(), FoodList::new);
		}
	}
	
	@SubscribeEvent
	public static void attachPlayerCapability(AttachCapabilitiesEvent<Entity> event) {
		if (!(event.getObject() instanceof PlayerEntity)) return;
		
		event.addCapability(FOOD, new FoodList());
	}
	
	@SubscribeEvent
	public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
		// server needs to send any loaded data to the client
		syncFoodList(event.getPlayer());
	}
	
	@SubscribeEvent
	public static void onPlayerDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
		syncFoodList(event.getPlayer());
	}
	
	@SubscribeEvent
	public static void onClone(PlayerEvent.Clone event) {
		if (event.isWasDeath() && SOLCarrotConfig.shouldResetOnDeath()) return;
		
		PlayerEntity originalPlayer = event.getOriginal();
		originalPlayer.revive(); // so we can access the capabilities; entity will get removed either way
		FoodList original = FoodList.get(originalPlayer);
		FoodList newInstance = FoodList.get(event.getPlayer());
		newInstance.deserializeNBT(original.serializeNBT());
		// can't sync yet; client hasn't attached capabilities yet
	}
	
	@SubscribeEvent
	public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
		syncFoodList(event.getPlayer());
	}
	
	public static void syncFoodList(PlayerEntity player) {
		if (player.level.isClientSide) return;
		
		ServerPlayerEntity target = (ServerPlayerEntity) player;
		SOLCarrot.channel.sendTo(
			new FoodListMessage(FoodList.get(player)),
			target.connection.getConnection(),
			NetworkDirection.PLAY_TO_CLIENT
		);
		
		MaxHealthHandler.updateFoodHPModifier(player);
	}
}
