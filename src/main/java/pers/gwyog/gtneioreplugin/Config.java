package pers.gwyog.gtneioreplugin;

import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import java.io.File;
import net.minecraftforge.common.config.Configuration;

public class Config {
    public Configuration tConfig;

    public Config(FMLPreInitializationEvent preinit, String cfgname) {
        File tFile = new File(preinit.getModConfigurationDirectory(), cfgname);
        tConfig = new Configuration(tFile);
        tConfig.load();
    }

    public void save() {
        if (tConfig.hasChanged()) tConfig.save();
    }
}
