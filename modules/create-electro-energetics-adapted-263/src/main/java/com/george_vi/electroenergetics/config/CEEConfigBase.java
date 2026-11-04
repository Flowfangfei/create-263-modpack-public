package com.george_vi.electroenergetics.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.zurrtum.create.catnip.config.ConfigBase;
import com.zurrtum.create.catnip.config.DoubleRawValue;
import java.util.LinkedHashMap;
import java.util.Map;

/** Create Fly config values with bounded doubles and a non-persistent server overlay. */
public abstract class CEEConfigBase extends ConfigBase {
    private final Map<String, SyncedValue> syncedValues = new LinkedHashMap<>();

    private interface SyncedValue {
        JsonElement json();
        void remote(JsonElement value);
    }

    public JsonObject snapshot() {
        JsonObject result = new JsonObject();
        syncedValues.forEach((name, value) -> result.add(name, value.json()));
        for (ConfigBase child : children)
            result.add(child.getName(), ((CEEConfigBase) child).snapshot());
        return result;
    }

    public void applyRemote(JsonObject values) {
        syncedValues.forEach((name, value) -> value.remote(values == null ? null : values.get(name)));
        for (ConfigBase child : children) {
            JsonElement value = values == null ? null : values.get(child.getName());
            ((CEEConfigBase) child).applyRemote(value != null && value.isJsonObject() ? value.getAsJsonObject() : null);
        }
    }

    public void prepareReload() {
        depth = 0;
        for (ConfigBase child : children) ((CEEConfigBase) child).prepareReload();
    }

    @Override
    protected ConfigBool b(boolean current, String name, String... comments) {
        return new ConfigBool(name, current, comments);
    }

    @Override
    protected ConfigFloat f(float current, float min, float max, String name, String... comments) {
        return new ConfigFloat(name, current, min, max, comments);
    }

    @Override
    protected ConfigFloat f(float current, float min, String name, String... comments) {
        return f(current, min, Float.MAX_VALUE, name, comments);
    }

    @Override
    protected ConfigInt i(int current, int min, int max, String name, String... comments) {
        return new ConfigInt(name, current, min, max, comments);
    }

    @Override
    protected ConfigInt i(int current, int min, String name, String... comments) {
        return i(current, min, Integer.MAX_VALUE, name, comments);
    }

    @Override
    protected ConfigInt i(int current, String name, String... comments) {
        return i(current, Integer.MIN_VALUE, Integer.MAX_VALUE, name, comments);
    }

    protected ConfigDouble d(double current, double min, String name, String... comments) {
        return new ConfigDouble(name, current, min, Double.MAX_VALUE, comments);
    }

    public class ConfigBool extends ConfigBase.ConfigBool implements SyncedValue {
        private Boolean remote;
        ConfigBool(String name, boolean current, String... comments) {
            super(name, current, comments);
            syncedValues.put(name, this);
        }
        @Override public Boolean get() { return remote == null ? super.get() : remote; }
        @Override public JsonElement json() { return new com.google.gson.JsonPrimitive(get()); }
        @Override public void remote(JsonElement value) { remote = value == null ? null : value.getAsBoolean(); }
    }

    public class ConfigInt extends ConfigBase.ConfigInt implements SyncedValue {
        private final int min, max;
        private Integer remote;
        ConfigInt(String name, int current, int min, int max, String... comments) {
            super(name, current, min, max, comments);
            this.min = min; this.max = max;
            syncedValues.put(name, this);
        }
        @Override public Integer get() { return Math.clamp(remote == null ? super.get() : remote, min, max); }
        @Override public JsonElement json() { return new com.google.gson.JsonPrimitive(get()); }
        @Override public void remote(JsonElement value) { remote = value == null ? null : Math.clamp(value.getAsInt(), min, max); }
    }

    public class ConfigFloat extends ConfigBase.ConfigFloat implements SyncedValue {
        private final float min, max;
        private Float remote;
        ConfigFloat(String name, float current, float min, float max, String... comments) {
            super(name, current, min, max, comments);
            this.min = min; this.max = max;
            syncedValues.put(name, this);
        }
        @Override public Float get() { return Math.clamp(remote == null ? super.get() : remote, min, max); }
        @Override public JsonElement json() { return new com.google.gson.JsonPrimitive(get()); }
        @Override public void remote(JsonElement value) {
            Float parsed = value == null ? null : value.getAsFloat();
            if (parsed != null && !Float.isFinite(parsed)) throw new IllegalArgumentException("Non-finite config value");
            remote = parsed;
        }
    }

    public class ConfigDouble implements SyncedValue {
        private final double initial, min, max;
        private DoubleRawValue persistent;
        private Double remote;
        ConfigDouble(String name, double current, double min, double max, String... comments) {
            this.initial = current; this.min = min; this.max = max;
            new CValue<Void>(name, builder -> {
                persistent = builder.define(name, current);
                return null;
            }, comments);
            syncedValues.put(name, this);
        }
        public Double get() {
            double number = remote != null ? remote : persistent == null ? initial : persistent.get();
            return Double.isFinite(number) ? Math.clamp(number, min, max) : initial;
        }
        public void set(double value) {
            persistent.set(Math.clamp(value, min, max));
            persistent.save();
        }
        @Override public JsonElement json() { return new com.google.gson.JsonPrimitive(get()); }
        @Override public void remote(JsonElement value) {
            Double parsed = value == null ? null : value.getAsDouble();
            if (parsed != null && !Double.isFinite(parsed)) throw new IllegalArgumentException("Non-finite config value");
            remote = parsed;
        }
    }
}
