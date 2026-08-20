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

                Vec3 hitVec = hitResult.getLocation();
                for (double d = 0; d < 1.0; d += 0.05) {
                    double px = eyePos.x + (hitVec.x - eyePos.x) * d;
                    double py = eyePos.y + (hitVec.y - eyePos.y) * d;
                    double pz = eyePos.z + (hitVec.z - eyePos.z) * d;
                    serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, px, py, pz, 1, 0, 0, 0, 0);
                }

                serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0f, 1.5f);

                spawnPortalParticles(serverLevel, portalPos, face);

                player.sendSystemMessage(Component.literal("Портал открыт!"));

                serverPlayer.teleportTo(
                    serverLevel,
                    portalPos.getX() + 0.5,
                    portalPos.getY(),
                    portalPos.getZ() + 0.5,
                    serverPlayer.getYRot(),
                    serverPlayer.getXRot()
                );
            }
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
    }

    private void spawnPortalParticles(ServerLevel level, BlockPos pos, Direction face) {
        double centerX = pos.getX() + 0.5;
        double centerY = pos.getY() + 1.0;
        double centerZ = pos.getZ() + 0.5;

        for (int i = 0; i < 360; i += 15) {
            double angle = Math.toRadians(i);
            double r1 = Math.cos(angle) * 0.8;
            double r2 = Math.sin(angle) * 1.2;

            double px = centerX, py = centerY + r2, pz = centerZ;
            if (face.getAxis() == Direction.Axis.X) {
                pz = centerZ + r1;
            } else {
                px = centerX + r1;
            }

            level.sendParticles(ParticleTypes.HAPPY_VILLAGER, px, py, pz, 3, 0.05, 0.05, 0.05, 0.01);
            level.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, px, py, pz, 2, 0.05, 0.05, 0.05, 0.02);
        }
    }
}
