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

package plus.dragons.createdragonsplus.util;

import com.google.common.base.Preconditions;
import java.util.Optional;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

public class PersistentDataHelper {
    private static final String EXCEPTION_EMPTY_PATH = "Path must not be empty";

    public static CompoundTag getOrCreate(CompoundTag nbt, String... path) {
        Preconditions.checkArgument(path.length > 0, EXCEPTION_EMPTY_PATH);
        if (path.length == 1) {
            String key = path[path.length - 1];
            if (!nbt.contains(key))
                nbt.put(key, new CompoundTag());
            return nbt.getCompoundOrEmpty(key);
        }
        for (String key : path) {
            if (!nbt.contains(key))
                nbt.put(key, new CompoundTag());
            nbt = nbt.getCompoundOrEmpty(key);
        }
        return nbt;
    }

    public static ListTag getOrCreateList(CompoundTag nbt, int type, String... path) {
        Preconditions.checkArgument(path.length > 0, EXCEPTION_EMPTY_PATH);
        String key = path[path.length - 1];
        if (path.length > 1)
            nbt = getParent(nbt, path);
        ListTag list;
        list = (ListTag) nbt.get(key);
        if (list == null || (!list.isEmpty() && list.stream().anyMatch(entry -> entry.getId() != type))) {
            list = new ListTag();
            nbt.put(key, list);
        }
        return list;
    }

    public static void put(CompoundTag nbt, Tag value, String... path) {
        Preconditions.checkArgument(path.length > 0, EXCEPTION_EMPTY_PATH);
        if (path.length == 1) {
            nbt.put(path[0], value);
            return;
        }
        int lastIndex = path.length - 1;
        for (int index = 0; index < lastIndex; index++) {
            String key = path[index];
            if (!nbt.contains(key))
                nbt.put(key, new CompoundTag());
            nbt = nbt.getCompoundOrEmpty(key);
        }
        nbt.put(path[path.length - 1], value);
    }

    private static CompoundTag getParent(CompoundTag nbt, String... path) {
        int lastIndex = path.length - 1;
        for (int index = 0; index < lastIndex; index++) {
            String key = path[index];
            if (!nbt.contains(key))
                nbt.put(key, new CompoundTag());
            nbt = nbt.getCompoundOrEmpty(key);
        }
        return nbt;
    }

    public static byte getByte(CompoundTag nbt, String... path) {
        Preconditions.checkArgument(path.length > 0, EXCEPTION_EMPTY_PATH);
        if (path.length == 1)
            return nbt.getByteOr(path[0], (byte) 0);
        return getParent(nbt, path).getByteOr(path[path.length - 1], (byte)0);
    }

    public static void putByte(CompoundTag nbt, byte value, String... path) {
        put(nbt, ByteTag.valueOf(value), path);
    }

    public static byte addByte(CompoundTag nbt, byte value, String... path) {
        Preconditions.checkArgument(path.length > 0, EXCEPTION_EMPTY_PATH);
        String key = path[path.length - 1];
        if (path.length > 1)
            nbt = getParent(nbt, path);
        value = (byte) (nbt.getByteOr(key, (byte) 0) + value);
        nbt.putByte(key, value);
        return value;
    }

    public static short getShort(CompoundTag nbt, String... path) {
        Preconditions.checkArgument(path.length > 0, EXCEPTION_EMPTY_PATH);
        if (path.length == 1)
            return nbt.getShortOr(path[0], (short) 0);
        return getParent(nbt, path).getShortOr(path[path.length - 1], (short)0);
    }

    public static void putShort(CompoundTag nbt, short value, String... path) {
        put(nbt, ShortTag.valueOf(value), path);
    }

    public static short addShort(CompoundTag nbt, short value, String... path) {
        Preconditions.checkArgument(path.length > 0, EXCEPTION_EMPTY_PATH);
        String key = path[path.length - 1];
        if (path.length > 1)
            nbt = getParent(nbt, path);
        value += nbt.getShortOr(key, (short) 0);
        nbt.putShort(key, value);
        return value;
    }

    public static int getInt(CompoundTag nbt, String... path) {
        Preconditions.checkArgument(path.length > 0, EXCEPTION_EMPTY_PATH);
        if (path.length == 1) return nbt.getIntOr(path[0], 0);
        return getParent(nbt, path).getIntOr(path[path.length - 1], 0);
    }

    public static void putInt(CompoundTag nbt, int value, String... path) {
        put(nbt, IntTag.valueOf(value), path);
    }

    public static int addInt(CompoundTag nbt, int value, String... path) {
        Preconditions.checkArgument(path.length > 0, EXCEPTION_EMPTY_PATH);
        String key = path[path.length - 1];
        if (path.length > 1)
            nbt = getParent(nbt, path);
        value += nbt.getIntOr(key, 0);
        nbt.putInt(key, value);
        return value;
    }

    public static long getLong(CompoundTag nbt, String... path) {
        Preconditions.checkArgument(path.length > 0, EXCEPTION_EMPTY_PATH);
        if (path.length == 1)
            return nbt.getLongOr(path[0], 0L);
        return getParent(nbt, path).getLongOr(path[path.length - 1], 0L);
    }

    public static void putLong(CompoundTag nbt, long value, String... path) {
        put(nbt, LongTag.valueOf(value), path);
    }

    public static long addLong(CompoundTag nbt, long value, String... path) {
        Preconditions.checkArgument(path.length > 0, EXCEPTION_EMPTY_PATH);
        String key = path[path.length - 1];
        if (path.length > 1)
            nbt = getParent(nbt, path);
        value += nbt.getLongOr(key, 0L);
        nbt.putLong(key, value);
        return value;
    }

    public static float getFloat(CompoundTag nbt, String... path) {
        Preconditions.checkArgument(path.length > 0, EXCEPTION_EMPTY_PATH);
        if (path.length == 1)
            return nbt.getFloatOr(path[0], 0.0F);
        return getParent(nbt, path).getFloatOr(path[path.length - 1], 0f);
    }

    public static void putFloat(CompoundTag nbt, float value, String... path) {
        put(nbt, FloatTag.valueOf(value), path);
    }

    public static float addFloat(CompoundTag nbt, float value, String... path) {
        Preconditions.checkArgument(path.length > 0, EXCEPTION_EMPTY_PATH);
        String key = path[path.length - 1];
        if (path.length > 1)
            nbt = getParent(nbt, path);
        value += nbt.getFloatOr(key, 0.0F);
        nbt.putFloat(key, value);
        return value;
    }

    public static double getDouble(CompoundTag nbt, String... path) {
        Preconditions.checkArgument(path.length > 0, EXCEPTION_EMPTY_PATH);
        if (path.length == 1)
            return nbt.getDoubleOr(path[0], 0.0);
        return getParent(nbt, path).getDoubleOr(path[path.length - 1], 0d);
    }

    public static void putDouble(CompoundTag nbt, double value, String... path) {
        put(nbt, DoubleTag.valueOf(value), path);
    }

    public static double addDouble(CompoundTag nbt, double value, String... path) {
        Preconditions.checkArgument(path.length > 0, EXCEPTION_EMPTY_PATH);
        String key = path[path.length - 1];
        if (path.length > 1)
            nbt = getParent(nbt, path);
        value += nbt.getDoubleOr(key, 0.0);
        nbt.putDouble(key, value);
        return value;
    }

    public static String getString(CompoundTag nbt, String... path) {
        Preconditions.checkArgument(path.length > 0, EXCEPTION_EMPTY_PATH);
        if (path.length == 1)
            return nbt.getStringOr(path[0], "");
        return getParent(nbt, path).getStringOr(path[path.length - 1], "");
    }

    public static Optional<String> getOptionalString(CompoundTag nbt, String... path) {
        Preconditions.checkArgument(path.length > 0, EXCEPTION_EMPTY_PATH);
        String key = path[path.length - 1];
        if (path.length > 1)
            nbt = getParent(nbt, path);
        if (nbt.contains(key))
            //noinspection DataFlowIssue
            return nbt.getString(key);
        return Optional.empty();
    }

    public static void putString(CompoundTag nbt, String value, String... path) {
        put(nbt, StringTag.valueOf(value), path);
    }
}
