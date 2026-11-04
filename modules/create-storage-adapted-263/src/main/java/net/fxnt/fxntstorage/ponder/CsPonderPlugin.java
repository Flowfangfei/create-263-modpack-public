package net.fxnt.fxntstorage.ponder;

import net.createmod.ponder.api.client.registration.PonderPlugin;
import net.createmod.ponder.api.client.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.client.registration.PonderTagRegistrationHelper;
import net.fxnt.fxntstorage.FXNTStorage;
import net.minecraft.resources.Identifier;

public class CsPonderPlugin implements PonderPlugin {
    @Override
    public String getModId() {
        return FXNTStorage.MOD_ID;
    }

    @Override
    public void registerScenes(PonderSceneRegistrationHelper<Identifier> helper) {
        ModPonder.Scenes.register(helper);
    }

    @Override
    public void registerTags(PonderTagRegistrationHelper<Identifier> helper) {
        ModPonder.Tags.register(helper);
    }
}
