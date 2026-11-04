package dev.simulated_team.simulated.content.display_sources;

import com.simibubi.create.foundation.gui.ModularGuiLineBuilder;
import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;

public interface ConfigurableDisplaySource {
    void initConfigurationWidgets(DisplayLinkContext context, ModularGuiLineBuilder builder, boolean isFirstLine);
}
