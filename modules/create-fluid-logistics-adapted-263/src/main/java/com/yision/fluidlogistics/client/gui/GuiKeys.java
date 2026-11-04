package com.yision.fluidlogistics.client.gui;
import com.mojang.blaze3d.platform.InputConstants;
public final class GuiKeys {
    private GuiKeys() {}
    private static boolean down(int key) { return InputConstants.isKeyDown(key); }
    public static boolean shift() { return down(InputConstants.KEY_LSHIFT) || down(InputConstants.KEY_RSHIFT); }
    public static boolean control() { return down(InputConstants.KEY_LCONTROL) || down(InputConstants.KEY_RCONTROL); }
    public static boolean alt() { return down(InputConstants.KEY_LALT) || down(InputConstants.KEY_RALT); }
}
