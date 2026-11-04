package com.george_vi.electroenergetics;

import com.george_vi.electroenergetics.content.wire.WireAttachmentType;
import com.george_vi.electroenergetics.content.wire.attachments.BannerAttachmentType;
import com.george_vi.electroenergetics.content.wire.attachments.BuntingAttachmentType;
import com.george_vi.electroenergetics.content.wire.attachments.DamperAttachmentType;
import com.george_vi.electroenergetics.registry.DeferredRegister;
import com.george_vi.electroenergetics.registry.RegistryEntry;

public class CEEWireAttachments {
    private static final DeferredRegister<WireAttachmentType> WIRE_TYPES =
            DeferredRegister.create(CEERegistries.WIRE_ATTACHMENT_TYPE, CreateElectroEnergetics.ID);

    public static final RegistryEntry<WireAttachmentType, WireAttachmentType> EMPTY =
            WIRE_TYPES.register("empty", WireAttachmentType.Empty::new);
    public static final RegistryEntry<WireAttachmentType, WireAttachmentType> BANNER =
            WIRE_TYPES.register("banner", BannerAttachmentType::new);
    public static final RegistryEntry<WireAttachmentType, WireAttachmentType> BUNTING =
            WIRE_TYPES.register("bunting", BuntingAttachmentType::new);
    public static final RegistryEntry<WireAttachmentType, WireAttachmentType> DAMPER =
            WIRE_TYPES.register("damper", DamperAttachmentType::new);

    public static void register() {
        WIRE_TYPES.register();
    }
}
