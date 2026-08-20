package com.example.portalgun;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class PortalEntity extends Entity {
    private static final EntityDataAccessor<BlockPos> DESTINATION = 
        SynchedEntityData.defineId(PortalEntity.class, EntityDataSerializers.BLOCK_POS);

    public PortalEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DESTINATION, BlockPos.ZERO);
    }

    public void setDestination(BlockPos pos) {
        this.entityData.set(DESTINATION, pos);
    }

    public BlockPos getDestination() {
        return this.entityData.get(DESTINATION);
    }

    @Override
    public void tick() {
        super.tick();

        // Анимация вращающегося овального портала из зелёных частиц
        if (this.level().isClientSide()) {
            double centerX = this.getX();
            double centerY = this.getY() + 1.0;
            double centerZ = this.getZ();

            for (int i = 0; i < 360; i += 30) {
                double angle = Math.toRadians(i + (this.tickCount * 5) % 360);
                double r1 = Math.cos(angle) * 0.8;
                double r2 = Math.sin(angle) * 1.2;

                this.level().addParticle(ParticleTypes.HAPPY_VILLAGER, 
                    centerX + r1, centerY + r2, centerZ, 0, 0, 0);
                this.level().addParticle(ParticleTypes.TOTEM_OF_UNDYING, 
                    centerX + r1, centerY + r2, centerZ, 0, 0, 0);
            }
        } else {
            // Проверяем, зашел ли игрок в портал (хитбокс портала)
            AABB portalBox = new AABB(
                this.getX() - 0.8, this.getY(), this.getZ() - 0.8,
                this.getX() + 0.8, this.getY() + 2.0, this.getZ() + 0.8
            );

            List<Player> players = this.level().getEntitiesOfClass(Player.class, portalBox);
            for (Player player : players) {
                if (player instanceof ServerPlayer serverPlayer) {
                    BlockPos dest = getDestination();
                    if (!dest.equals(BlockPos.ZERO)) {
                        // Телепортируем игрока в точку назначения
                        serverPlayer.teleportTo(
                            (ServerLevel) this.level(),
                            dest.getX() + 0.5, dest.getY() + 1.0, dest.getZ() + 0.5,
                            serverPlayer.getYRot(), serverPlayer.getXRot()
                        );
                    }
                }
            }

            // Портал исчезает через 30 секунд (600 тиков)
            if (this.tickCount > 600) {
                this.discard();
            }
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {}

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {}
}
