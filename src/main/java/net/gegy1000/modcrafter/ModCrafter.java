package net.gegy1000.modcrafter;

import net.gegy1000.modcrafter.common.proxy.CommonProxy;
import net.gegy1000.modcrafter.mod.sprite.SpriteDefItem;
import net.gegy1000.modcrafter.script.ScriptDefHatTest;
import net.gegy1000.modcrafter.script.ScriptDefManager;
import net.gegy1000.modcrafter.script.ScriptDefPrintConsole;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = ModCrafter.modid, name = ModCrafter.name, version = ModCrafter.version, dependencies = "required-after:llibrary@[0.1.0-1.7.10,)")
public class ModCrafter
{
    public static final String modid = "modcrafter";
    public static final String name = "Mod Crafter";
    public static final String version = "0.0.1";

    @Instance(ModCrafter.modid)
    public static ModCrafter instance;

    @SidedProxy(serverSide = "net.gegy1000.modcrafter.common.proxy.CommonProxy", clientSide = "net.gegy1000.modcrafter.client.proxy.ClientProxy")
    public static CommonProxy proxy;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        proxy.preInit();

        ScriptDefManager.init();
        ModCrafterAPI.registerSpriteDef(new SpriteDefItem());
    }

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        proxy.init();
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
        proxy.postInit();
    }
}
