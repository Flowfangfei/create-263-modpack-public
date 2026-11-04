/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2025 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.railwayteam.railways.content.custom_tracks.generic_crossing;

import com.railwayteam.railways.content.custom_tracks.generic_crossing.TrackShapeLookup.GenericCrossingData;
import com.railwayteam.railways.mixin_interfaces.IGenericCrossingTrackBE;
import com.simibubi.create.content.trains.track.TrackMaterial;
import com.simibubi.create.content.trains.track.TrackShape;
import com.simibubi.create.foundation.blockEntity.IMergeableBE;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.createmod.catnip.api.data.Couple;
import net.createmod.catnip.api.data.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class GenericCrossingBlockEntity extends SmartBlockEntity implements IMergeableBE, IGenericCrossingTrackBE {

    boolean cancelDrops = false;
    private Couple<TrackMaterial> materials = null;

    public GenericCrossingBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        setLazyTickRate(100);
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (level == null || cancelDrops)
            return;
        Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(),
                new ItemStack(getPrimary().getBlock().asItem()));
        Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(),
                new ItemStack(getSecondary().getBlock().asItem()));
    }

    @NotNull
    public TrackMaterial getPrimary() {
        if (materials == null)
            return TrackMaterial.ANDESITE;
        return materials.getFirst() == null ? TrackMaterial.ANDESITE : materials.getFirst();
    }

    @NotNull
    public TrackMaterial getSecondary() {
        if (materials == null)
            return TrackMaterial.ANDESITE;
        return materials.getSecond() == null ? TrackMaterial.ANDESITE : materials.getSecond();
    }
    public void accept(BlockEntity other) {
        level.scheduleTick(worldPosition, getBlockState().getBlock(), 1);
    }
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {}

    @Override
    protected void read(net.minecraft.nbt.CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries, boolean clientPacket) {
        ValueInput input = net.minecraft.world.level.storage.TagValueInput.create(net.minecraft.util.ProblemReporter.DISCARDING, registries, tag);
        super.read(tag, registries, clientPacket);

        boolean updateMesh = false;
        TrackMaterial primary = TrackMaterial.deserialize(input.getString("PrimaryMaterial").orElse(TrackMaterial.ANDESITE.id.toString()));
        TrackMaterial secondary = TrackMaterial.deserialize(input.getString("SecondaryMaterial").orElse(TrackMaterial.ANDESITE.id.toString()));

        if (primary != getPrimary() || secondary != getSecondary()) updateMesh = true;

        materials = Couple.create(primary, secondary);

        if (clientPacket && updateMesh)
            redraw();
    }

    private void redraw() {
        if (level != null)
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 16);
    }

    @Override
    protected void write(net.minecraft.nbt.CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries, boolean clientPacket) {
        net.minecraft.world.level.storage.TagValueOutput output = net.minecraft.world.level.storage.TagValueOutput.createWithContext(net.minecraft.util.ProblemReporter.DISCARDING, registries);
        super.write(tag, registries, clientPacket);

        output.putString("PrimaryMaterial", getPrimary().id.toString());
        output.putString("SecondaryMaterial", getSecondary().id.toString());

        tag.merge(output.buildResult());
    }
    public @Nullable Pair<TrackMaterial, TrackShape> railways$getFirstCrossingPiece() {
        TrackMaterial primary = getPrimary();
        Couple<TrackShape> unmerged = TrackShapeLookup.getUnmerged(getBlockState().getValue(GenericCrossingBlock.SHAPE));
        if (unmerged == null) return null;

        return Pair.of(primary, unmerged.getFirst());
    }
    public @Nullable Pair<TrackMaterial, TrackShape> railways$getSecondCrossingPiece() {
        TrackMaterial secondary = getSecondary();
        Couple<TrackShape> unmerged = TrackShapeLookup.getUnmerged(getBlockState().getValue(GenericCrossingBlock.SHAPE));
        if (unmerged == null) return null;

        return Pair.of(secondary, unmerged.getSecond());
    }

    public void initFrom(GenericCrossingData crossingData) {
        if (level.isClientSide()) return;
        boolean flip = crossingData.merged().getSecond();
        TrackMaterial primary = flip ? crossingData.overlayMaterial() : crossingData.existingMaterial();
        TrackMaterial secondary = flip ? crossingData.existingMaterial() : crossingData.overlayMaterial();
        materials = Couple.create(primary, secondary);
        notifyUpdate();
    }
}
