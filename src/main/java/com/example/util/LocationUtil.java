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
    private final File middlePointFile;
    private final File loserSpotFile;
    private final File winnerSpotFile;
    
    private List<Location> spawnPoints;
    private Location middlePoint;
    private Location loserSpot;
    private Location winnerSpot;
    
    public LocationUtil(JavaPlugin plugin) {
        this.plugin = plugin;
        
        // Initialize files
        spawnPointsFile = new File(plugin.getDataFolder(), "spawnpoints.json");
        middlePointFile = new File(plugin.getDataFolder(), "middlepoint.json");
        loserSpotFile = new File(plugin.getDataFolder(), "loserspot.json");
        winnerSpotFile = new File(plugin.getDataFolder(), "winnerspot.json");
        
        // Load locations from files
        loadSpawnPoints();
        loadMiddlePoint();
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
    
    // Methods for middle point
    public void setMiddlePoint(Location location) {
        middlePoint = location;
        saveMiddlePoint();
    }
    
    public Location getMiddlePoint() {
        return middlePoint;
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
                && middlePoint != null 
                && loserSpot != null 
                && winnerSpot != null;
    }
    
    // Reset all locations
    public boolean resetAllLocations() {
        try {
            // Clear in-memory locations
            spawnPoints = new ArrayList<>();
            middlePoint = null;
            loserSpot = null;
            winnerSpot = null;
            
            // Delete files
            boolean spawnDeleted = !spawnPointsFile.exists() || spawnPointsFile.delete();
            boolean middleDeleted = !middlePointFile.exists() || middlePointFile.delete();
            boolean loserDeleted = !loserSpotFile.exists() || loserSpotFile.delete();
            boolean winnerDeleted = !winnerSpotFile.exists() || winnerSpotFile.delete();
            
            return spawnDeleted && middleDeleted && loserDeleted && winnerDeleted;
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
    private void saveMiddlePoint() {
        JSONObject jsonLocation = new JSONObject();
        jsonLocation.put("world", middlePoint.getWorld().getName());
        jsonLocation.put("x", middlePoint.getX());
        jsonLocation.put("y", middlePoint.getY());
        jsonLocation.put("z", middlePoint.getZ());
        
        try (FileWriter file = new FileWriter(middlePointFile)) {
            file.write(jsonLocation.toJSONString());
            file.flush();
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save middle point: " + e.getMessage());
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
    
    private void loadMiddlePoint() {
        if (!middlePointFile.exists()) {
            return;
        }
        
        JSONParser parser = new JSONParser();
        
        try (FileReader reader = new FileReader(middlePointFile)) {
            JSONObject jsonLocation = (JSONObject) parser.parse(reader);
            
            String worldName = (String) jsonLocation.get("world");
            double x = (double) jsonLocation.get("x");
            double y = (double) jsonLocation.get("y");
            double z = (double) jsonLocation.get("z");
            
            middlePoint = new Location(plugin.getServer().getWorld(worldName), x, y, z);
        } catch (IOException | ParseException e) {
            plugin.getLogger().severe("Could not load middle point: " + e.getMessage());
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