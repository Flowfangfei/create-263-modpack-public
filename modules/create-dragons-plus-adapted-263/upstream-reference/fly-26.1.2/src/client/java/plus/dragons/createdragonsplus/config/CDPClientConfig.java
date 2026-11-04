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

package plus.dragons.createdragonsplus.config;

import com.zurrtum.create.catnip.config.Builder;
import com.zurrtum.create.catnip.config.ConfigBase;
import plus.dragons.createdragonsplus.CreateDragonsPlus;

public final class CDPClientConfig extends ConfigBase {
    private static CDPClientConfig instance;
    public final ConfigFloat dyeVisionMultiplier = f(1.0F, 1.0F, 256.0F, "dyeVisionMultiplier", "Dye-fluid vision-distance multiplier");
    public final ConfigFloat dragonBreathVisionMultiplier = f(
            1.0F, 1.0F, 256.0F, "dragonBreathVisionMultiplier", "Dragon's Breath vision-distance multiplier");

    public static void register() {
        instance = Builder.create(CDPClientConfig::new, CreateDragonsPlus.ID, "client");
    }

    public static CDPClientConfig get() {
        return instance;
    }

    @Override
    public String getName() {
        return "client";
    }
}
