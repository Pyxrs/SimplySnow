package com.simplycmd.simplysnow;

import net.minecraft.block.Blocks;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;
import net.minecraft.world.biome.Biome;

public class Util {
    public static GetBlock getBlock(ServerWorld world, BlockPos pos) {
        var block = world.getBlockState(pos).getBlock();
        if (block == Blocks.SNOW) {
            return GetBlock.SNOW;
        } else if (block == Blocks.AIR) {
            return GetBlock.AIR;
        } else {
            return GetBlock.OTHER;
        }
    }
    public enum GetBlock {
        SNOW,
        AIR,
        OTHER
    }

    public static boolean canIncreaseSnow(ServerWorld world, BlockPos pos) {
        return world.isRaining() && world.getBiome(pos).value().getPrecipitation() == Biome.Precipitation.SNOW && pos.getY() >= world.getBottomY() && pos.getY() < world.getTopY() && world.getLightLevel(LightType.BLOCK, pos) < 10;
    }
}
