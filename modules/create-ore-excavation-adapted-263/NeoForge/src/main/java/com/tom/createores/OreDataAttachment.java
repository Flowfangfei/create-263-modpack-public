package com.tom.createores;

import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.common.util.ValueIOSerializable;

public class OreDataAttachment implements ValueIOSerializable {
	private OreData data;

	public OreDataAttachment(IAttachmentHolder holder) {
		data = new OreData();
	}

	@Override
	public void serialize(ValueOutput output) {
		output.store("data", OreData.Serialized.CODEC, data.save());
	}

	@Override
	public void deserialize(ValueInput input) {
		input.read("data", OreData.Serialized.CODEC).ifPresent(data::load);
	}

	public static OreData getData(LevelChunk chunk) {
		if(chunk.getLevel().isClientSide())throw new RuntimeException("Ore Data accessed from client");
		OreDataAttachment at = chunk.getData(CreateOreExcavation.ORE_DATA);
		if (at != null) {
			OreData data = at.data;
			if (!data.isLoaded()) {
				data.populate(chunk);
			}
			return at.data;
		} else {
			at = new OreDataAttachment(chunk);
			at.data.populate(chunk);
			chunk.setData(CreateOreExcavation.ORE_DATA, at);
			return at.data;
		}
	}
}
