package com.simibubi.create.foundation.block;

import org.jetbrains.annotations.NotNull;


import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChangeOverTimeBlock;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.WeatheringCopperStairBlock;

public class CreateWeatheringCopperStairBlock extends WeatheringCopperStairBlock {

	public CreateWeatheringCopperStairBlock(WeatherState weatherState, Properties properties) {
		super(weatherState, Blocks.AIR.defaultBlockState(), properties);
	}

	@Override
	public float getExplosionResistance() {
		return explosionResistance;
	}

}
