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
import plus.dragons.createdragonsplus.CreateDragonsPlus;

public final class CDPConfig {
    private static volatile CDPCommonConfig common;
    private static volatile CDPServerConfig server;

    private CDPConfig() {}

    public static synchronized void registerCommonAndServer() {
        if (common != null) {
            return;
        }
        common = Builder.create(CDPCommonConfig::new, CreateDragonsPlus.ID, "common");
        server = Builder.create(CDPServerConfig::new, CreateDragonsPlus.ID, "server");
    }

    public static synchronized void reloadServer() {
        server = Builder.create(CDPServerConfig::new, CreateDragonsPlus.ID, "server");
    }

    public static CDPCommonConfig common() {
        return common;
    }

    public static CDPServerConfig server() {
        return server;
    }

    public static CDPFeaturesConfig features() {
        return common.features;
    }

    public static CDPRecipesConfig recipes() {
        return server.recipes;
    }

    public static CDPDyeFluidConfig dyeFluid() {
        return server.dyeFluid;
    }
}
