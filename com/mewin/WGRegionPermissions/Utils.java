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

/**
 *
 * @author mewin
 */
public final class Utils {
    
    public static void setPermissionsForLocation(WorldGuardPlugin wgp, Map<String, Boolean> perms, Location loc, Set<String> newPerms, Set<String> removePerms)
    {
        for (String newPerm : newPerms)
        {
            perms.remove(newPerm);
        }
        
        for (String removePerm : removePerms)
        {
            if (!newPerms.contains(removePerm))
            {
                perms.put(removePerm, Boolean.TRUE);
            }
        }
        
        newPerms.clear();
        removePerms.clear();
        
        RegionManager rm = wgp.getRegionManager(loc.getWorld());
        
        if (rm == null)
        {
            return;
        }
        
        ApplicableRegionSet regions = rm.getApplicableRegions(loc);
        
        for (ProtectedRegion region : regions)
        {
            Set<String> addPerms = (Set<String>) region.getFlag(WGRegionPermissionsPlugin.ADD_PERMISSIONS_FLAG);
            
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
        
        for (Entry<String, Boolean> entry : perms.entrySet())
        {
            if (entry.getValue() == Boolean.FALSE)
            {
                continue;
            }
            
            if (!permissionAllowedAtLocation(wgp, entry.getKey(), loc))
            {
                perms.put(entry.getKey(), Boolean.FALSE);
                removePerms.add(entry.getKey());
            }
        }
    }
    
    public static Set<String> getAdditionalPermissionsForLocation(WorldGuardPlugin wgp, Location loc)
    {
        RegionManager rm = wgp.getRegionManager(loc.getWorld());
        
        if (rm == null)
        {
            return new HashSet<>();
        }
        
        ApplicableRegionSet regions = rm.getApplicableRegions(loc);
        
        HashSet<String> newPerms = new HashSet<>();
        
        for (ProtectedRegion region : regions)
        {
            Set<String> addPerms = (Set<String>) region.getFlag(WGRegionPermissionsPlugin.ADD_PERMISSIONS_FLAG);
            
            if (addPerms == null)
            {
                continue;
            }
            
            newPerms.addAll(addPerms);
        }
        
        return newPerms;
    }
    
    public static Set<String> getRemovePermissionsForLocation(WorldGuardPlugin wgp, Location loc)
    {
        RegionManager rm = wgp.getRegionManager(loc.getWorld());
        
        if (rm == null)
        {
            return new HashSet<>();
        }
        
        ApplicableRegionSet regions = rm.getApplicableRegions(loc);
        
        HashSet<String> newPerms = new HashSet<>();
        
        for (ProtectedRegion region : regions)
        {
            Set<String> addPerms = (Set<String>) region.getFlag(WGRegionPermissionsPlugin.REMOVE_PERMISSIONS_FLAG);
            
            if (addPerms == null)
            {
                continue;
            }
            
            newPerms.addAll(addPerms);
        }
        
        return newPerms;
    }
    
    public static boolean permissionAllowedAtLocation(WorldGuardPlugin wgp, String perm, Location loc)
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
