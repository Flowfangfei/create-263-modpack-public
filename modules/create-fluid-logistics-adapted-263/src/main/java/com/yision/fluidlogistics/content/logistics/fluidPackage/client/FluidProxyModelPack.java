package com.yision.fluidlogistics.content.logistics.fluidPackage.client;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Set;

import com.yision.fluidlogistics.FluidLogistics;
import com.yision.fluidlogistics.content.logistics.fluidPackage.FluidProxyItem;
import com.yision.fluidlogistics.content.logistics.fluidPackage.FluidProxyRegistry;

import net.minecraft.SharedConstants;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.world.item.Item;

final class FluidProxyModelPack implements net.minecraft.server.packs.PackResources {
	private static final byte[] MODEL = "{\"model\":{\"type\":\"fluidlogistics:fluid_proxy\"}}"
		.getBytes(StandardCharsets.UTF_8);

	private final PackLocationInfo location;
	@Override public PackLocationInfo location() {return location;}
	@SuppressWarnings("unchecked")
	@Override public <T> T getMetadataSection(net.minecraft.server.packs.metadata.MetadataSectionType<T> type) {
		if(type == net.minecraft.server.packs.metadata.pack.PackMetadataSection.forPackType(PackType.CLIENT_RESOURCES)) {var version=SharedConstants.getCurrentVersion().packVersion(PackType.CLIENT_RESOURCES);return (T)new net.minecraft.server.packs.metadata.pack.PackMetadataSection(net.minecraft.network.chat.Component.literal("Fluid proxies"),new net.minecraft.util.InclusiveRange<>(version,version));}
		return null;
	}

	FluidProxyModelPack(PackLocationInfo location) {
		this.location = location;
	}

	@Override
	public IoSupplier<InputStream> getRootResource(String... elements) {
		if (elements.length != 1 || !elements[0].equals(PACK_META)) {
			return null;
		}
		return null;
	}

	@Override
	public IoSupplier<InputStream> getResource(PackType type, Identifier location) {
		if (type != PackType.CLIENT_RESOURCES || !location.getNamespace().equals(FluidLogistics.MODID)
			|| !location.getPath().startsWith("items/fluid_proxy/")
			|| !location.getPath().endsWith(".json")) {
			return null;
		}
		String path = location.getPath();
		Identifier itemId = FluidLogistics.asResource(path.substring("items/".length(), path.length() - 5));
		if (!(BuiltInRegistries.ITEM.getValue(itemId) instanceof FluidProxyItem)) {
			return null;
		}
		return () -> new ByteArrayInputStream(MODEL);
	}

	@Override
	public void listResources(PackType type, String namespace, String path, ResourceOutput output) {
		if (type != PackType.CLIENT_RESOURCES || !namespace.equals(FluidLogistics.MODID)) {
			return;
		}
		for (Item item : FluidProxyRegistry.items()) {
			Identifier itemId = BuiltInRegistries.ITEM.getKey(item);
			Identifier model = itemId.withPath("items/" + itemId.getPath() + ".json");
			if (path.isEmpty() || model.getPath().startsWith(path + "/")) {
				output.accept(model, () -> new ByteArrayInputStream(MODEL));
			}
		}
	}

	@Override
	public Set<String> getNamespaces(PackType type) {
		return type == PackType.CLIENT_RESOURCES ? Set.of(FluidLogistics.MODID) : Set.of();
	}

	@Override
	public void close() {
	}
}
