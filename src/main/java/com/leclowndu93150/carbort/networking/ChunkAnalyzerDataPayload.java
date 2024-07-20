package com.leclowndu93150.carbort.networking;

import com.leclowndu93150.carbort.Carbort;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public record ChunkAnalyzerDataPayload(List<Integer> blocks, List<Integer> amounts, List<Integer> entities, List<Integer> entityamount) implements CustomPacketPayload {
    public static final Type<ChunkAnalyzerDataPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Carbort.MODID, "ca_data_payload"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ChunkAnalyzerDataPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT.apply(ByteBufCodecs.list()),
            ChunkAnalyzerDataPayload::blocks,
            ByteBufCodecs.INT.apply(ByteBufCodecs.list()),
            ChunkAnalyzerDataPayload::amounts,
            ByteBufCodecs.INT.apply(ByteBufCodecs.list()),
            ChunkAnalyzerDataPayload::entities,
            ByteBufCodecs.INT.apply(ByteBufCodecs.list()),
            ChunkAnalyzerDataPayload::entityamount,
            ChunkAnalyzerDataPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

