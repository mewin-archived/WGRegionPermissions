package com.mewin.WGRegionPermissions;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 *
 * @author mewin
 */
public final class Utils {
    
    public static void setPermissionsForPlayer(WorldGuardPlugin wgp, Map<String, Boolean> perms, Player player, Set<String> newPerms, Set<String> removePerms)
    {
        Location loc = player.getLocation();
        //remove perms that have been added earlier (will be readded if still available)
        for (String newPerm : newPerms)
        {
            perms.remove(newPerm);
        }
        
        //give the player the perms back that have been removed (will be removed again if still denied)
        for (String removePerm : removePerms)
        {
            if (!newPerms.contains(removePerm)) //only if it wasn't added due to the region
            {
                perms.put(removePerm, Boolean.TRUE);
            }
        }
        
        newPerms.clear(); //these will be refilled
        removePerms.clear();
        
        RegionManager rm = wgp.getRegionManager(loc.getWorld());
        
        if (rm == null)
        {
            return; //WorldGuard disabled for this world
        }
        
        ApplicableRegionSet regions = rm.getApplicableRegions(loc);
        
        for (ProtectedRegion region : regions)
        {
            if (player.hasPermission("region.permissions.ignore") || player.hasPermission("region.permissions.ignore." + region.getId()))
            {
                continue;
            }
            Set<String> addPerms = (Set<String>) region.getFlag(WGRegionPermissionsPlugin.ADD_PERMISSIONS_FLAG);
            
            //add possible new permissions
            if (addPerms == null)
            {
                continue;
            }
            
            for(String perm : addPerms)
            {
                if (!perms.containsKey(perm) || perms.get(perm) == Boolean.FALSE)
                {
                    perms.put(perm, Boolean.TRUE);
                
                    newPerms.add(perm);
                }
            }
        }
        
        //remove all permissions that are not allowed in the region
        for (Entry<String, Boolean> entry : perms.entrySet())
        {
            if (entry.getValue() == Boolean.FALSE)
            {
                continue;
            }
            
            if (!permissionAllowedAtLocation(wgp, entry.getKey(), loc, player))
            {
                perms.put(entry.getKey(), Boolean.FALSE);
                removePerms.add(entry.getKey());
            }
        }
    }
    
    public static boolean permissionAllowedAtLocation(WorldGuardPlugin wgp, String perm, Location loc, Player player)
    {
        RegionManager rm = wgp.getRegionManager(loc.getWorld());
        if (rm == null)
        {
            return true;
        }
        ApplicableRegionSet regions = rm.getApplicableRegions(loc);
        Iterator<ProtectedRegion> itr = regions.iterator();
        Map<ProtectedRegion, Boolean> regionsToCheck = new HashMap<>();
        Set<ProtectedRegion> ignoredRegions = new HashSet<>();
        
        while(itr.hasNext())
        {
            ProtectedRegion region = itr.next();
            
            if (player.hasPermission("region.permissions.ignore") || player.hasPermission("region.permissions.ignore." + region.getId()))
            {
                ignoredRegions.add(region);
            }
            
            if (ignoredRegions.contains(region))
            {
                continue;
            }
            
            Object allowed = permissionAllowedInRegion(region, perm);
            
            if (allowed != null)
            {
                ProtectedRegion parent = region.getParent();
                
                while(parent != null)
                {
                    ignoredRegions.add(parent);
                    
                    parent = parent.getParent();
                }
                
                regionsToCheck.put(region, (boolean) allowed);
            }
        }
        
        if (regionsToCheck.size() >= 1)
        {
            Iterator<Map.Entry<ProtectedRegion, Boolean>> itr2 = regionsToCheck.entrySet().iterator();
            
            while(itr2.hasNext())
            {
                Map.Entry<ProtectedRegion, Boolean> entry = itr2.next();
                
                ProtectedRegion region = entry.getKey();
                boolean value = entry.getValue();
                
                if (ignoredRegions.contains(region))
                {
                    continue;
                }
                
                if (value) // allow > deny
                {
                    return true;
                }
            }
            
            return false;
        }
        else
        {
            Object allowed = permissionAllowedInRegion(rm.getRegion("__global__"), perm);
            
            if (allowed != null)
            {
                return (boolean) allowed;
            }
            else
            {
                return true;
            }
        }
    }
    
    public static Object permissionAllowedInRegion(ProtectedRegion region, String perm)
    {
        HashSet<String> addedPermissions = (HashSet<String>) region.getFlag(WGRegionPermissionsPlugin.ADD_PERMISSIONS_FLAG);
        HashSet<String> blockedPermissions = (HashSet<String>) region.getFlag(WGRegionPermissionsPlugin.REMOVE_PERMISSIONS_FLAG);
        
        if (addedPermissions != null && addedPermissions.contains(perm))
        {
            return true;
        }
        else if(blockedPermissions != null && blockedPermissions.contains(perm))
        {
            return false;
        }
        else
        {
            return null;
        }
    }
}
