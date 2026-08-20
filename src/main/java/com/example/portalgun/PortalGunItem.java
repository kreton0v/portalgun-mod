package com.example.portalgun;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class PortalGunItem extends Item {
    private BlockPos lastPortalPos = null;

    public PortalGunItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            Vec3 eyePos = player.getEyePosition();
            Vec3 lookVec = player.getLookAngle();
            Vec3 reachPos = eyePos.add(lookVec.x * 50, lookVec.y * 50, lookVec.z * 50);

            BlockHitResult hitResult = level.clip(new ClipContext(
                eyePos, reachPos,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                player
            ));

            if (hitResult.getType() == HitResult.Type.BLOCK) {
                BlockPos targetPos = hitResult.getBlockPos();
                Direction face = hitResult.getDirection();
                BlockPos portalPos = targetPos.relative(face);

                ServerLevel serverLevel = (ServerLevel) level;

                // Визуальный луч выстрела
                Vec3 hitVec = hitResult.getLocation();
                for (double d = 0; d < 1.0; d += 0.05) {
                    double px = eyePos.x + (hitVec.x - eyePos.x) * d;
                    double py = eyePos.y + (hitVec.y - eyePos.y) * d;
                    double pz = eyePos.z + (hitVec.z - eyePos.z) * d;
                    serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, px, py, pz, 1, 0, 0, 0, 0);
                }

                // Звук выстрела
                serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0f, 1.5f);

                // Создаем сущность портала на стене
                PortalEntity portal = new PortalEntity(PortalGunMod.PORTAL_ENTITY.get(), serverLevel);
                portal.setPos(portalPos.getX() + 0.5, portalPos.getY(), portalPos.getZ() + 0.5);

                if (lastPortalPos != null) {
                    // Если прошлый портал уже был — связываем их!
                    portal.setDestination(lastPortalPos);
                    player.sendSystemMessage(Component.literal("Портал связан с предыдущим!"));
                } else {
                    // Если это первый выстрел — портал ведет в спавн
                    portal.setDestination(serverLevel.getSharedSpawnPos());
                    player.sendSystemMessage(Component.literal("Портал открыт! Наступи в него для телепортации."));
                }

                lastPortalPos = portalPos;
                serverLevel.addFreshEntity(portal);
            }
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
    }
}
