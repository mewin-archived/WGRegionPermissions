package com.mewin.WGRegionPermissions;

import com.mewin.WGCustomFlags.WGCustomFlagsPlugin;
import com.mewin.WGCustomFlags.flags.CustomSetFlag;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.StringFlag;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author mewin
 */
public class WGRegionPermissionsPlugin extends JavaPlugin {
    public static final StringFlag PERMISSIONS_FLAG = new StringFlag("permissions");
    public static final CustomSetFlag ADD_PERMISSIONS_FLAG = new CustomSetFlag("add-permissions", PERMISSIONS_FLAG);
    public static final CustomSetFlag REMOVE_PERMISSIONS_FLAG = new CustomSetFlag("remove-permissions", PERMISSIONS_FLAG);
    
    private WGRegionPermissionsListener listener;
    private WGCustomFlagsPlugin custPlugin;
    private WorldGuardPlugin wgPlugin;
    
    @Override
    public void onEnable()
    {
        getCustPlugin();
        getWgPlugin();
        
        
        listener = new WGRegionPermissionsListener(this, wgPlugin);
        getServer().getPluginManager().registerEvents(listener, this);
        
        custPlugin.addCustomFlag(ADD_PERMISSIONS_FLAG);
        custPlugin.addCustomFlag(REMOVE_PERMISSIONS_FLAG);
        
        listener.addAttachments();
    }
    
    @Override
    public void onDisable()
    {
        listener.clearAttachments();
    }
    
    private void getCustPlugin()
    {
        Plugin plug = getServer().getPluginManager().getPlugin("WGCustomFlags");
        
        if (plug == null || !(plug instanceof WGCustomFlagsPlugin))
        {
            getLogger().warning("Could not find custom flags plugin, disabling.");
            getServer().getPluginManager().disablePlugin(this);
        }
        
        custPlugin = (WGCustomFlagsPlugin) plug;
    }
    
    private void getWgPlugin()
    {
        Plugin plug = getServer().getPluginManager().getPlugin("WorldGuard");
        
        if (plug == null || !(plug instanceof WorldGuardPlugin))
        {
            getLogger().warning("Could not find World Guard plugin, disabling.");
            getServer().getPluginManager().disablePlugin(this);
        }
        
        wgPlugin = (WorldGuardPlugin) plug;
    }
}
