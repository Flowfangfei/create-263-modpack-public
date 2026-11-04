/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2026 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.railwayteam.railways.config;

import com.google.gson.JsonObject;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import net.neoforged.neoforge.common.ModConfigSpec;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.createmod.catnip.api.config.ConfigBase;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public class CServer extends ConfigBase {

    public final ConfigGroup misc = group(0, "misc", Comments.misc);

    public final ConfigBool strictCoupler = b(false, "strictCoupler", Comments.strictCoupler);
    public final ConfigBool flipDistantSwitches = b(true, "flipDistantSwitches", Comments.flipDistantSwitches);
    public final ConfigInt switchPlacementRange = i(64, 16, 128, "switchPlacementRange", Comments.switchPlacementRange);
    public final ConfigBool explosiveTrackDamage = b(false, "creeperTrackDamage", Comments.explosiveTrackDamage);
    public final ConfigFloat handcarHungerMultiplier = f(.01f, 0, 1, "handcarHungerMultiplier", Comments.handcarHungerMultiplier);
    public final ConfigBool rollersClearSnow = b(true, "rollersClearSnow", Comments.rollersClearSnow);
    public final ConfigBool unlimitedCreativeRelocation = b(false, "unlimitedCreativeRelocation", Comments.unlimitedCreativeRelocation, "[@cui:RequiresRelog]");

    public final CSemaphores semaphores = nested(0, CSemaphores::new, Comments.semaphores);
    public final CConductors conductors = nested(0, CConductors::new, Comments.conductors);
    public final CRealism realism = nested(0, CRealism::new, Comments.realism);

    private JsonObject localValues;
    private JsonObject exportedValues;

    public String getName() {
        return "server";
    }

    /** Exports only this server spec, retaining the existing {value: ...} packet format. */
    public JsonObject getValues() {
        if (specification == null)
            return null;
        if (localValues != null)
            return localValues;
        JsonObject current = encode(specification.getValues());
        if (exportedValues == null)
            exportedValues = current;
        else {
            exportedValues.keySet().clear();
            current.entrySet().forEach(entry -> exportedValues.add(entry.getKey(), entry.getValue()));
        }
        return exportedValues;
    }

    /** Validates the complete server spec before mutating any value; never saves remote values to disk. */
    public void reload(@Nullable JsonObject synced) {
        if (specification == null)
            throw new IllegalStateException("Server config has not been registered");
        if (synced == null) {
            if (localValues == null)
                return;
            if (specification.isLoaded())
                apply(decode(localValues));
            localValues = null;
            return;
        }
        Map<ModConfigSpec.ConfigValue<?>, Object> decoded = decode(synced);
        JsonObject local = localValues == null ? getValues() : localValues;
        apply(decoded);
        localValues = local;
    }

    private JsonObject encode(UnmodifiableConfig values) {
        JsonObject object = new JsonObject();
        for (var entry : values.entrySet()) {
            Object raw = entry.getValue();
            if (raw instanceof UnmodifiableConfig nested) {
                object.add(entry.getKey(), encode(nested));
            } else if (raw instanceof ModConfigSpec.ConfigValue<?> config) {
                Object value = specification.isLoaded() ? config.get() : config.getDefault();
                JsonObject wrapped = new JsonObject();
                wrapped.add("value", value instanceof Boolean bool ? new JsonPrimitive(bool)
                    : value instanceof Number number ? new JsonPrimitive(number)
                    : new JsonPrimitive(value.toString()));
                object.add(entry.getKey(), wrapped);
            }
        }
        return object;
    }

    private Map<ModConfigSpec.ConfigValue<?>, Object> decode(JsonObject source) {
        Map<ModConfigSpec.ConfigValue<?>, Object> values = new LinkedHashMap<>();
        collectDecoded(specification.getValues(), source, values);
        return values;
    }

    private void collectDecoded(UnmodifiableConfig spec, JsonObject source,
                                Map<ModConfigSpec.ConfigValue<?>, Object> values) {
        for (var entry : spec.entrySet()) {
            Object raw = entry.getValue();
            JsonElement supplied = source.get(entry.getKey());
            if (supplied == null || !supplied.isJsonObject())
                throw new IllegalArgumentException("Missing config group/value " + entry.getKey());
            JsonObject object = supplied.getAsJsonObject();
            if (raw instanceof UnmodifiableConfig nested) {
                collectDecoded(nested, object, values);
            } else if (raw instanceof ModConfigSpec.ConfigValue<?> config) {
                JsonElement element = object.get("value");
                if (element == null || !element.isJsonPrimitive())
                    throw new IllegalArgumentException("Missing/null config value " + config.getPath());
                Object value = parse(element.getAsJsonPrimitive(), config.getDefault());
                if (!config.getSpec().test(value))
                    throw new IllegalArgumentException("Invalid config value " + config.getPath() + ": " + element);
                values.put(config, value);
            }
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static Object parse(JsonPrimitive value, Object template) {
        if (template instanceof Boolean && value.isBoolean())
            return value.getAsBoolean();
        if (template instanceof Integer && value.isNumber())
            return value.getAsBigDecimal().intValueExact();
        if (template instanceof Long && value.isNumber())
            return value.getAsBigDecimal().longValueExact();
        if (template instanceof Double && value.isNumber()) {
            double number = value.getAsDouble();
            if (Double.isFinite(number))
                return number;
        }
        if (template instanceof Enum enumeration && value.isString())
            return Enum.valueOf(enumeration.getDeclaringClass(), value.getAsString());
        if (template instanceof String && value.isString())
            return value.getAsString();
        throw new IllegalArgumentException("Wrong config value type: " + value);
    }

    private void apply(Map<ModConfigSpec.ConfigValue<?>, Object> values) {
        Map<ModConfigSpec.ConfigValue<?>, Object> previous = new LinkedHashMap<>();
        values.keySet().forEach(value -> previous.put(value, value.get()));
        try {
            values.forEach(CServer::set);
        } catch (RuntimeException exception) {
            previous.forEach(CServer::set);
            throw exception;
        }
        onReload();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void set(ModConfigSpec.ConfigValue value, Object replacement) {
        value.set(replacement);
    }

    private static class Comments {
        static String misc = "Miscellaneous settings";

        static String strictCoupler = "Coupler will require points to be on the same or adjacent track edge, this will prevent the coupler from working if there is any form of junction in between the two points.";
        static String flipDistantSwitches = "Allow controlling Brass Switches remotely when approaching them on a train";
        static String switchPlacementRange = "Max distance between targeted track and placed switch stand";
        static String explosiveTrackDamage = "Allow creepers and ghast fireballs to damage tracks";
        static String handcarHungerMultiplier = "Multiplier used for calculating exhaustion from speed when a handcar is used.";
        static String rollersClearSnow = "Rollers clear snow-encased tracks when rolling over them.";
        static String unlimitedCreativeRelocation = "Allow creative mode players to relocate trains without distance restrictions.";

        static String semaphores = "Semaphore settings";
        static String conductors = "Conductor settings";
        static String realism = "Realism Settings";
    }
}
