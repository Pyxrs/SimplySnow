package com.simplycmd.simplysnow.mixin.server;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.block.Blocks;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;

@Mixin(ServerWorld.class)
public class ServerWorldMixin {
    @Redirect(
        method = "tickChunk",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;getTopPosition(Lnet/minecraft/world/Heightmap$Type;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/util/math/BlockPos;")
    )
    private BlockPos getTopPosition(ServerWorld self, Heightmap.Type type, BlockPos pos) {
        var currentPos = self.getTopPosition(Heightmap.Type.MOTION_BLOCKING, pos);
        var currentBlock = self.getBlockState(currentPos);
        while (currentBlock.getBlock() == Blocks.SNOW || currentBlock.isAir() || currentBlock.isIn(BlockTags.LEAVES)) {
            if (!(currentBlock.getBlock() == Blocks.SNOW) && self.getBiome(currentPos).value().canSetSnow(self, currentPos)) {
                return currentPos;
            }
            
            currentPos = currentPos.down();
            currentBlock = self.getBlockState(currentPos);
        }
        return self.getTopPosition(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, pos);
    }
}
