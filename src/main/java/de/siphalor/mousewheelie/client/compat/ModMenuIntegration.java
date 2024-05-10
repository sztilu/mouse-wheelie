package de.siphalor.mousewheelie.client.compat;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import de.siphalor.mousewheelie.MWConfig;

public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory()
    {
        return MWConfig::createConfigScreen;
    }
}
