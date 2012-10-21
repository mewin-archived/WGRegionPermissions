/*
 * Copyright (C) 2012 mewin <mewin001@hotmail.de>
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.mewin.WGRegionPermissions;

import com.mewin.WGCustomFlags.WGCustomFlagsPlugin;
import com.mewin.WGCustomFlags.flags.CustomSetFlag;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.StringFlag;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author mewin <mewin001@hotmail.de>
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
