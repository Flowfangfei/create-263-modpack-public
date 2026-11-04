package plus.dragons.createdragonsplus.common.recipe;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.crafting.RecipeHolder;
import plus.dragons.createdragonsplus.common.CDPCommon;

/** Bounded recipe chunks keep remote previews in step with the authoritative server. */
public record RecipeSnapshotPayload(boolean reset, List<RecipeHolder<?>> recipes) implements CustomPacketPayload {
    public static final Type<RecipeSnapshotPayload> TYPE = new Type<>(CDPCommon.asResource("recipe_snapshot"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RecipeSnapshotPayload> CODEC = new StreamCodec<>() {
        @Override public RecipeSnapshotPayload decode(RegistryFriendlyByteBuf buffer) {
            boolean reset = buffer.readBoolean(); int size = buffer.readVarInt();
            if (size < 0 || size > 32) throw new IllegalArgumentException("Invalid recipe snapshot chunk size: " + size);
            var recipes = new ArrayList<RecipeHolder<?>>(size);
            for (int i = 0; i < size; i++) recipes.add(RecipeHolder.STREAM_CODEC.decode(buffer));
            return new RecipeSnapshotPayload(reset, List.copyOf(recipes));
        }
        @Override public void encode(RegistryFriendlyByteBuf buffer, RecipeSnapshotPayload payload) {
            buffer.writeBoolean(payload.reset()); buffer.writeVarInt(payload.recipes().size());
            payload.recipes().forEach(recipe -> RecipeHolder.STREAM_CODEC.encode(buffer, recipe));
        }
    };
    @Override public Type<RecipeSnapshotPayload> type() { return TYPE; }
}
