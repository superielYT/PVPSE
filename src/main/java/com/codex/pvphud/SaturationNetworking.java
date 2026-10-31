package com.codex.pvphud;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** Implements the public AppleSkin saturation channel, without an AppleSkin dependency. */
public final class SaturationNetworking {
    public record Sync(float value) implements CustomPacketPayload {
        public static final Type<Sync> ID=new Type<>(Identifier.fromNamespaceAndPath("appleskin","saturation"));
        public static final StreamCodec<FriendlyByteBuf,Sync> CODEC=CustomPacketPayload.codec(
                (payload,buffer)->buffer.writeFloat(payload.value()),buffer->new Sync(buffer.readFloat()));
        public Type<? extends CustomPacketPayload> type(){return ID;}
    }
    public static void register(){
        ClientPlayConnectionEvents.DISCONNECT.register((handler,client)->SaturationState.clearNetwork());
        // AppleSkin owns its channel if installed; its FoodData updates are observed by our mixin.
        if(FabricLoader.getInstance().isModLoaded("appleskin"))return;
        PayloadTypeRegistry.clientboundPlay().register(Sync.ID,Sync.CODEC);
        ClientPlayNetworking.registerGlobalReceiver(Sync.ID,(payload,context)->context.client().execute(()->SaturationState.accept(payload.value(),true)));
    }
}
