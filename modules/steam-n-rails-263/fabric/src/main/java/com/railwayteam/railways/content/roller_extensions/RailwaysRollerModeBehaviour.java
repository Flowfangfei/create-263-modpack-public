package com.railwayteam.railways.content.roller_extensions;


import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsBoard;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsFormatter.ScrollOptionSettingsFormatter;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollOptionBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.INamedIconOptions;
import com.simibubi.create.foundation.gui.AllIcons;
import com.railwayteam.railways.registry.CRIcons;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import java.util.List;

public final class RailwaysRollerModeBehaviour extends ScrollOptionBehaviour<RailwaysRollerModeBehaviour.Options> {
 public RailwaysRollerModeBehaviour(Component label,SmartBlockEntity be,ValueBoxTransform slot) { super(Options.class,label,be,slot);between(0,3); }
 public INamedIconOptions selectedIcon() { return Options.values()[Math.min(3,getValue())]; }
 @Override public ValueSettingsBoard createBoard(Player player,BlockHitResult hit) { return new ValueSettingsBoard(label,3,1,List.of(Component.literal("Select")),new ScrollOptionSettingsFormatter(Options.values())); }
 public enum Options implements INamedIconOptions {
  TUNNEL_PAVE(AllIcons.I_ROLLER_PAVE,"tunnel_pave"),STRAIGHT_FILL(AllIcons.I_ROLLER_FILL,"straight_fill"),WIDE_FILL(AllIcons.I_ROLLER_WIDE_FILL,"wide_fill"),TRACK_REPLACE(CRIcons.I_SWAP_TRACKS,"track_replace");
  private final AllIcons icon;private final String key;
  Options(AllIcons icon,String key) { this.icon=icon;this.key=key; }
  @Override public AllIcons getIcon() { return icon; }
  @Override public String getTranslationKey() { return "create.contraptions.roller_mode."+key; }
 }
}
