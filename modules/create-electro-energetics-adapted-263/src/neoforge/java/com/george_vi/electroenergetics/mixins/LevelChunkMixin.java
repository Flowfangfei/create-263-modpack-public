package com.george_vi.electroenergetics.mixins;

import com.george_vi.electroenergetics.devices.device.DeviceBlock;
import com.george_vi.electroenergetics.devices.device.SimulatedDevice;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.sugar.Local;
import com.george_vi.electroenergetics.devices.device.DevicesSavedData;
import com.george_vi.electroenergetics.devices.device.SimulatedDeviceType;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LevelChunk.class)
public class LevelChunkMixin {

    @Shadow
    @Final
    private Level level;

    @WrapOperation(method = "setBlockState", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/level/chunk/LevelChunkSection;setBlockState(IIILnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/world/level/block/state/BlockState;"))
    private BlockState electroenergetics$removeOldDevice(LevelChunkSection section, int x, int y, int z,
        BlockState state, Operation<BlockState> original, @Local(argsOnly = true) BlockPos pos) {
        BlockState previous = original.call(section, x, y, z, state);
        if (level instanceof ServerLevel serverLevel && previous.getBlock() != state.getBlock()) {
            DevicesSavedData data = DevicesSavedData.load(serverLevel);
            SimulatedDevice device = data.getDevice(pos);
            if (device != null && device.shouldRemove(previous, state)) data.removeDevice(pos);
        }
        return previous;
    }

    @Inject(method = "setBlockState", at = @At("TAIL"), remap = false)
    public void setBlockState(BlockPos pos, BlockState state, int flags, CallbackInfoReturnable<BlockState> cir) {
        if (!(level instanceof ServerLevel level) || cir.getReturnValue() == null)
            return;

        if (state.getBlock() instanceof DeviceBlock<?> db) {
            SimulatedDeviceType<?> type = db.getDevice();
            DevicesSavedData sd = DevicesSavedData.load(level);
            sd.addDevice(type, pos, db.getDefaultDeviceData(level, pos, state));
        }
    }

    private static final java.util.function.Predicate<BlockState> IS_DEVICE_BLOCK = state -> state.getBlock() instanceof DeviceBlock<?>;

    /**
     * This catches device blocks placed during worldgen that weren't registered via setBlockState. It seems
     * like setBlockState isn't usually called during worldgen, so this is necessary for blocks to be registered
     * as devices if they are part of worldgen.
     */
    @Inject(method = "postProcessGeneration", at = @At("TAIL"), remap = false)
    public void onPostProcessGeneration(CallbackInfo ci) {
        if (!(level instanceof ServerLevel serverLevel))
            return;

        LevelChunk self = (LevelChunk)(Object)this;
        DevicesSavedData sd = DevicesSavedData.load(serverLevel);
        ChunkPos chunkPos = self.getPos();
        LevelChunkSection[] sections = self.getSections();

        int minY = self.getMinY();

        for (int sectionIndex = 0; sectionIndex < sections.length; sectionIndex++) {
            LevelChunkSection section = sections[sectionIndex];
            if (section == null || !section.maybeHas(IS_DEVICE_BLOCK))
                continue;

            int sectionY = minY + (sectionIndex * 16);

            for (int x = 0; x < 16; x++) {
                for (int y = 0; y < 16; y++) {
                    for (int z = 0; z < 16; z++) {
                        BlockState state = section.getBlockState(x, y, z);
                        if (state.getBlock() instanceof DeviceBlock<?> db) {
                            BlockPos pos = new BlockPos(
                                chunkPos.getMinBlockX() + x,
                                sectionY + y,
                                chunkPos.getMinBlockZ() + z
                            );
                            if (sd.getDevice(pos) == null) {
                                sd.addDevice(db.getDevice(), pos, db.getDefaultDeviceData(serverLevel, pos, state));
                            }
                        }
                    }
                }
            }
        }
    }
}
