package com.example.util;

import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class LocationUtil {
    private final JavaPlugin plugin;
    private final File spawnPointsFile;
    private final File loserSpotFile;
    private final File winnerSpotFile;
    
    private List<Location> spawnPoints;
    private Location loserSpot;
    private Location winnerSpot;
    
    public LocationUtil(JavaPlugin plugin) {
        this.plugin = plugin;
        
        // Initialize files
        spawnPointsFile = new File(plugin.getDataFolder(), "spawnpoints.json");
        loserSpotFile = new File(plugin.getDataFolder(), "loserspot.json");
        winnerSpotFile = new File(plugin.getDataFolder(), "winnerspot.json");
        
        // Load locations from files
        loadSpawnPoints();
        loadLoserSpot();
        loadWinnerSpot();
    }
    
    // Methods for spawn points
    public void addSpawnPoint(Location location) {
        if (spawnPoints == null) {
            spawnPoints = new ArrayList<>();
        }
        spawnPoints.add(location);
        saveSpawnPoints();
    }
    
    public List<Location> getSpawnPoints() {
        return spawnPoints;
    }
    
    /**
     * Calculates the middle point from the spawn points
     * Returns null if there are no spawn points
     */
    public Location getMiddlePoint() {
        if (spawnPoints == null || spawnPoints.isEmpty()) {
            return null;
        }
        
        // Initialize min/max coordinates
        double minX = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE;
        double minZ = Double.MAX_VALUE;
        double maxZ = Double.MIN_VALUE;
        double avgY = 0;
        
        // Find the min/max X and Z coordinates
        for (Location spawn : spawnPoints) {
            minX = Math.min(minX, spawn.getX());
            maxX = Math.max(maxX, spawn.getX());
            minZ = Math.min(minZ, spawn.getZ());
            maxZ = Math.max(maxZ, spawn.getZ());
            avgY += spawn.getY();
        }
        
        // Calculate average Y
        avgY /= spawnPoints.size();
        
        // Middle point is average of min and max
        double midX = (minX + maxX) / 2;
        double midZ = (minZ + maxZ) / 2;
        
        // Create the middle point location in the same world as the spawn points
        return new Location(spawnPoints.get(0).getWorld(), midX, avgY, midZ);
    }
    
    // Methods for loser spot
    public void setLoserSpot(Location location) {
        loserSpot = location;
        saveLoserSpot();
    }
    
    public Location getLoserSpot() {
        return loserSpot;
    }
    
    // Methods for winner spot
    public void setWinnerSpot(Location location) {
        winnerSpot = location;
        saveWinnerSpot();
    }
    
    public Location getWinnerSpot() {
        return winnerSpot;
    }
    
    // Check if all required locations are set
    public boolean areAllLocationsSet() {
        return spawnPoints != null && !spawnPoints.isEmpty() 
                && loserSpot != null 
                && winnerSpot != null;
    }
    
    // Reset all locations
    public boolean resetAllLocations() {
        try {
            // Clear in-memory locations
            spawnPoints = new ArrayList<>();
            loserSpot = null;
            winnerSpot = null;
            
            // Delete files
            boolean spawnDeleted = !spawnPointsFile.exists() || spawnPointsFile.delete();
            boolean loserDeleted = !loserSpotFile.exists() || loserSpotFile.delete();
            boolean winnerDeleted = !winnerSpotFile.exists() || winnerSpotFile.delete();
            
            return spawnDeleted && loserDeleted && winnerDeleted;
        } catch (Exception e) {
            plugin.getLogger().severe("Error resetting locations: " + e.getMessage());
            return false;
        }
    }
    
    // Saving methods
    @SuppressWarnings("unchecked")
    private void saveSpawnPoints() {
        JSONArray jsonArray = new JSONArray();
        
        for (Location location : spawnPoints) {
            JSONObject jsonLocation = new JSONObject();
            jsonLocation.put("world", location.getWorld().getName());
            jsonLocation.put("x", location.getX());
            jsonLocation.put("y", location.getY());
            jsonLocation.put("z", location.getZ());
            jsonLocation.put("yaw", location.getYaw());
            jsonLocation.put("pitch", location.getPitch());
            
            jsonArray.add(jsonLocation);
        }
        
        try (FileWriter file = new FileWriter(spawnPointsFile)) {
            file.write(jsonArray.toJSONString());
            file.flush();
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save spawn points: " + e.getMessage());
        }
    }
    
    @SuppressWarnings("unchecked")
    private void saveLoserSpot() {
        JSONObject jsonLocation = new JSONObject();
        jsonLocation.put("world", loserSpot.getWorld().getName());
        jsonLocation.put("x", loserSpot.getX());
        jsonLocation.put("y", loserSpot.getY());
        jsonLocation.put("z", loserSpot.getZ());
        jsonLocation.put("yaw", loserSpot.getYaw());
        jsonLocation.put("pitch", loserSpot.getPitch());
        
        try (FileWriter file = new FileWriter(loserSpotFile)) {
            file.write(jsonLocation.toJSONString());
            file.flush();
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save loser spot: " + e.getMessage());
        }
    }
    
    @SuppressWarnings("unchecked")
    private void saveWinnerSpot() {
        JSONObject jsonLocation = new JSONObject();
        jsonLocation.put("world", winnerSpot.getWorld().getName());
        jsonLocation.put("x", winnerSpot.getX());
        jsonLocation.put("y", winnerSpot.getY());
        jsonLocation.put("z", winnerSpot.getZ());
        jsonLocation.put("yaw", winnerSpot.getYaw());
        jsonLocation.put("pitch", winnerSpot.getPitch());
        
        try (FileWriter file = new FileWriter(winnerSpotFile)) {
            file.write(jsonLocation.toJSONString());
            file.flush();
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save winner spot: " + e.getMessage());
        }
    }
    
    // Loading methods
    private void loadSpawnPoints() {
        spawnPoints = new ArrayList<>();
        
        if (!spawnPointsFile.exists()) {
            return;
        }
        
        JSONParser parser = new JSONParser();
        
        try (FileReader reader = new FileReader(spawnPointsFile)) {
            JSONArray jsonArray = (JSONArray) parser.parse(reader);
            
            for (Object obj : jsonArray) {
                JSONObject jsonLocation = (JSONObject) obj;
                
                String worldName = (String) jsonLocation.get("world");
                double x = (double) jsonLocation.get("x");
                double y = (double) jsonLocation.get("y");
                double z = (double) jsonLocation.get("z");
                float yaw = ((Number) jsonLocation.get("yaw")).floatValue();
                float pitch = ((Number) jsonLocation.get("pitch")).floatValue();
                
                Location location = new Location(plugin.getServer().getWorld(worldName), x, y, z, yaw, pitch);
                spawnPoints.add(location);
            }
        } catch (IOException | ParseException e) {
            plugin.getLogger().severe("Could not load spawn points: " + e.getMessage());
        }
    }
    
    private void loadLoserSpot() {
        if (!loserSpotFile.exists()) {
            return;
        }
        
        JSONParser parser = new JSONParser();
        
        try (FileReader reader = new FileReader(loserSpotFile)) {
            JSONObject jsonLocation = (JSONObject) parser.parse(reader);
            
            String worldName = (String) jsonLocation.get("world");
            double x = (double) jsonLocation.get("x");
            double y = (double) jsonLocation.get("y");
            double z = (double) jsonLocation.get("z");
            float yaw = ((Number) jsonLocation.get("yaw")).floatValue();
            float pitch = ((Number) jsonLocation.get("pitch")).floatValue();
            
            loserSpot = new Location(plugin.getServer().getWorld(worldName), x, y, z, yaw, pitch);
        } catch (IOException | ParseException e) {
            plugin.getLogger().severe("Could not load loser spot: " + e.getMessage());
        }
    }
    
    private void loadWinnerSpot() {
        if (!winnerSpotFile.exists()) {
            return;
        }
        
        JSONParser parser = new JSONParser();
        
        try (FileReader reader = new FileReader(winnerSpotFile)) {
            JSONObject jsonLocation = (JSONObject) parser.parse(reader);
            
            String worldName = (String) jsonLocation.get("world");
            double x = (double) jsonLocation.get("x");
            double y = (double) jsonLocation.get("y");
            double z = (double) jsonLocation.get("z");
            float yaw = ((Number) jsonLocation.get("yaw")).floatValue();
            float pitch = ((Number) jsonLocation.get("pitch")).floatValue();
            
            winnerSpot = new Location(plugin.getServer().getWorld(worldName), x, y, z, yaw, pitch);
        } catch (IOException | ParseException e) {
            plugin.getLogger().severe("Could not load winner spot: " + e.getMessage());
        }
    }
}