package com.simibubi.create.content.contraptions.behaviour.dispenser.storage;

import com.simibubi.create.api.contraption.storage.item.simple.SimpleMountedStorage;
import com.simibubi.create.api.contraption.storage.item.simple.SimpleMountedStorageType;

import com.simibubi.create.compat.neoforge263.items.IItemHandler;

public class DispenserMountedStorageType extends SimpleMountedStorageType<DispenserMountedStorage> {
	public DispenserMountedStorageType() {
		super(DispenserMountedStorage.CODEC);
	}

	@Override
	protected SimpleMountedStorage createStorage(IItemHandler handler) {
		return new DispenserMountedStorage(handler);
	}
}
