package com.railwayteam.railways.content.extended_sliding_doors;
import com.simibubi.create.content.decoration.slidingDoor.SlidingDoorBlockEntity;
public final class SlidingDoorModeServerBehaviour extends SlidingDoorModeScrollOptionBehaviour {
 public SlidingDoorModeServerBehaviour(SlidingDoorBlockEntity be) { super(be); }
 @Override public void read(net.minecraft.nbt.CompoundTag tag,net.minecraft.core.HolderLookup.Provider registries,boolean clientPacket) {
  super.read(tag,registries,clientPacket);value=Math.max(0,Math.min(SlidingDoorMode.values().length-1,value));
 }
}
