package com.simplycmd.simplysnow.mixin.shared;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.Random;

import com.simplycmd.simplysnow.Util;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.SnowBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.LightType;
import net.minecraft.world.Heightmap;

@Mixin(SnowBlock.class)
public class SnowBlockMixin {
    private static final int MIN_SNOW_STICKINESS = 1;
    private static final int MIN_SNOW_ACCUMULATION = 1;
    private static final int MAX_SNOW_STICKINESS = 3;
    private static final int MAX_SNOW_ACCUMULATION = 2;

    /**
     * @author SimplyCmd
     * @reason Snowier snow, of course! :D
     */
    @Overwrite
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return VoxelShapes.empty();
    }

    @Overwrite
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (world.getLightLevel(LightType.BLOCK, pos) > 11) {
            SnowBlock.dropStacks(state, world, pos);
            world.removeBlock(pos, false);
        }

        if (random.nextInt(16) == 0) {
            var ground = world.getTopPosition(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, pos);
            if (pos.equals(ground) && Util.canIncreaseSnow(world, pos)) 
                for (int i = 0; i < random.nextInt(MIN_SNOW_ACCUMULATION, MAX_SNOW_ACCUMULATION); i++)
                    increaseLayer(world, pos);
            doLeveling(pos, pos.offset(Direction.byId(random.nextInt(5) + 2)), state, world, random);
        }
    }

    /**
     * Handles snow physics
     */
    private static void doLeveling(BlockPos originPos, BlockPos offsetPos, BlockState state, ServerWorld world, Random random) {
        var originHeight = getLayers(world, originPos);
        var offsetHeight = getLayers(world, offsetPos);
        if (offsetHeight + random.nextInt(MIN_SNOW_STICKINESS, MAX_SNOW_STICKINESS) < originHeight && increaseLayer(world, offsetPos)) decreaseBlockLayer(world, originPos);
    }
    
    private static Integer getLayers(ServerWorld world, BlockPos pos) {
        return (Util.getBlock(world, pos) == Util.GetBlock.SNOW) ? world.getBlockState(pos).get(SnowBlock.LAYERS) : 0;
    }

    /**
     * Increase a snow block's layer if it exists, otherwise attempt to increase the snow block's layer below it.
     */
    private static boolean increaseLayer(ServerWorld world, BlockPos pos) {
        final var height = getLayers(world, pos);
        final var down = pos.offset(Direction.DOWN);

        if (Util.getBlock(world, pos) == Util.GetBlock.SNOW) {
            // Spread to nearby snow
            if (height < SnowBlock.MAX_LAYERS) {
                world.setBlockState(pos, world.getBlockState(pos).with(SnowBlock.LAYERS, height + 1));
                return true;
            }
        } else if (Util.getBlock(world, pos) == Util.GetBlock.AIR) {
            // Spread to nearby air
            if (Blocks.SNOW.getDefaultState().canPlaceAt(world, pos)) {
                world.setBlockState(pos, Blocks.SNOW.getDefaultState());
                return true;
            // Spread to snow below
            } else if (Util.getBlock(world, down) == Util.GetBlock.SNOW) {
                var downHeight = getLayers(world, down);
                if (downHeight < SnowBlock.MAX_LAYERS) {
                    world.setBlockState(down, world.getBlockState(down).with(SnowBlock.LAYERS, downHeight + 1));
                    return true;
                }
            // Spread to air below
            } else if (Blocks.SNOW.getDefaultState().canPlaceAt(world, down)) {
                world.setBlockState(down, Blocks.SNOW.getDefaultState());
                return true;
            }
        }
        return false;
    }

    /**
     * Decrease a snow block's layer if it is above 1, otherwise remove it.
     */
    private static void decreaseBlockLayer(ServerWorld world, BlockPos pos) {
        final var height = getLayers(world, pos);
        if (Util.getBlock(world, pos) == Util.GetBlock.SNOW)
            if (height > 1) {
                world.setBlockState(pos, world.getBlockState(pos).with(SnowBlock.LAYERS, getLayers(world, pos) - 1));
            } else {
                world.setBlockState(pos, Blocks.AIR.getDefaultState());
            }
    }
}
