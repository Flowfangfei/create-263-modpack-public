/*
 * Copyright (C) 2025  DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package plus.dragons.createdragonsplus.registry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.resources.Identifier;
import plus.dragons.createdragonsplus.CreateDragonsPlus;

public final class CDPAttachments {
    public static final AttachmentType<ColoringProgress> COLORING_PROGRESS = AttachmentRegistry.createPersistent(
            CreateDragonsPlus.id("coloring_progress"), ColoringProgress.CODEC);
    public static final AttachmentType<ContactColoring> CONTACT_COLORING = AttachmentRegistry.createPersistent(
            CreateDragonsPlus.id("contact_coloring"), ContactColoring.CODEC);
    public static final AttachmentType<Boolean> DRAGON_BREATH_CLOUD = AttachmentRegistry.createPersistent(
            CreateDragonsPlus.id("dragon_breath_cloud"), Codec.BOOL);

    private CDPAttachments() {}

    public static void register() {}

    public record ColoringProgress(Identifier color, int lastProcessTick, int remainingTicks) {
        public static final Codec<ColoringProgress> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Identifier.CODEC.fieldOf("color").forGetter(ColoringProgress::color),
                Codec.INT.fieldOf("last_process_tick").forGetter(ColoringProgress::lastProcessTick),
                Codec.INT.fieldOf("remaining_ticks").forGetter(ColoringProgress::remainingTicks))
                .apply(instance, ColoringProgress::new));
    }

    public record ContactColoring(Identifier color, int lastProcessTick) {
        public static final Codec<ContactColoring> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Identifier.CODEC.fieldOf("color").forGetter(ContactColoring::color),
                Codec.INT.fieldOf("last_process_tick").forGetter(ContactColoring::lastProcessTick))
                .apply(instance, ContactColoring::new));
    }
}
