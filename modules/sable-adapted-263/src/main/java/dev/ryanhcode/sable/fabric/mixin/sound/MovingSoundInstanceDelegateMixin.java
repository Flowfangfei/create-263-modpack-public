package dev.ryanhcode.sable.fabric.mixin.sound;

import dev.ryanhcode.sable.sound.MovingSoundInstanceDelegate;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.client.resources.sounds.Sound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.concurrent.CompletableFuture;

@Mixin(MovingSoundInstanceDelegate.class)
public abstract class MovingSoundInstanceDelegateMixin implements SoundInstance {

    @Shadow
    public SoundInstance instance;

    @Override
    public CompletableFuture<AudioStream> getStream(final SoundBufferLibrary loader, final Sound sound, final boolean repeatInstantly) {
        return this.instance.getStream(loader, sound, repeatInstantly);
    }

}
