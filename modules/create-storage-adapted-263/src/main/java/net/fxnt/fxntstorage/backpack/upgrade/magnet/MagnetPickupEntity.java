package net.fxnt.fxntstorage.backpack.upgrade.magnet;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class MagnetPickupEntity extends ArmorStand {

    public MagnetPickupEntity(EntityType<? extends ArmorStand> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        setNoGravity(true);
        setPermanentlyInvulnerable(true);
        setInvisible(true);
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean isAffectedByFluids() {
        return false;
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    public void tick() {
    }

    @Override
    public boolean skipAttackInteraction(Entity entity) {
        return true;
    }

    @Override
    public boolean save(net.minecraft.world.level.storage.ValueOutput output) {
        return false;
    }

    @Override
    public InteractionResult interact(Player pPlayer, InteractionHand pHand, Vec3 pVec) {
        return InteractionResult.FAIL;
    }
}
