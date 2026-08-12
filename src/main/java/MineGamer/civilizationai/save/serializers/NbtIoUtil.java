package MineGamer.civilizationai.save.serializers;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

/**
 * Small, dependency-free NBT read/write helpers shared by every serializer
 * in this package. Kept separate from any one domain serializer since
 * {@link GlobalPos} and UUID show up in civilizations, profiles, and almost
 * every memory category.
 */
final class NbtIoUtil {

    private static final String KEY_DIM = "Dim";
    private static final String KEY_X = "X";
    private static final String KEY_Y = "Y";
    private static final String KEY_Z = "Z";

    private NbtIoUtil() {
    }

    static CompoundTag writeGlobalPos(GlobalPos pos) {
        CompoundTag tag = new CompoundTag();
        tag.putString(KEY_DIM, pos.dimension().location().toString());
        tag.putInt(KEY_X, pos.pos().getX());
        tag.putInt(KEY_Y, pos.pos().getY());
        tag.putInt(KEY_Z, pos.pos().getZ());
        return tag;
    }

    static GlobalPos readGlobalPos(CompoundTag tag) {
        ResourceKey<Level> dimension = ResourceKey.create(
                net.minecraft.core.registries.Registries.DIMENSION,
                new ResourceLocation(tag.getString(KEY_DIM))
        );
        BlockPos pos = new BlockPos(tag.getInt(KEY_X), tag.getInt(KEY_Y), tag.getInt(KEY_Z));
        return GlobalPos.of(dimension, pos);
    }

    static CompoundTag writeBlockPos(BlockPos pos) {
        CompoundTag tag = new CompoundTag();
        tag.putInt(KEY_X, pos.getX());
        tag.putInt(KEY_Y, pos.getY());
        tag.putInt(KEY_Z, pos.getZ());
        return tag;
    }

    static BlockPos readBlockPos(CompoundTag tag) {
        return new BlockPos(tag.getInt(KEY_X), tag.getInt(KEY_Y), tag.getInt(KEY_Z));
    }

    static ListTag newList() {
        return new ListTag();
    }
}
