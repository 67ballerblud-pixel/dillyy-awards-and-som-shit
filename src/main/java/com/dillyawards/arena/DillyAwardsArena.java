package com.dillyawards.arena;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public final class DillyAwardsArena extends JavaPlugin {
    private final Map<String, String> originals = new LinkedHashMap<>();
    private File snapshotFile;
    private World buildWorld;
    private int originX, originY, originZ;
    private Vector right, forward;

    @Override
    public void onEnable() {
        snapshotFile = new File(getDataFolder(), "arena-snapshot.yml");
        if (!getDataFolder().exists()) getDataFolder().mkdirs();
        loadSnapshot();
        getLogger().info("DillyAwardsArena enabled.");
    }

    @Override
    public void onDisable() {
        saveSnapshot();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("dillyawards.admin")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sender.sendMessage(ChatColor.AQUA + "Dilly Awards Arena commands:");
            sender.sendMessage(ChatColor.WHITE + "/awards build " + ChatColor.GRAY + "- build around you, facing forward");
            sender.sendMessage(ChatColor.WHITE + "/awards clear " + ChatColor.GRAY + "- restore the original blocks");
            return true;
        }
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command must be run by a player.");
            return true;
        }
        if (args[0].equalsIgnoreCase("build")) {
            if (!originals.isEmpty()) {
                player.sendMessage(ChatColor.RED + "An arena snapshot already exists. Run /awards clear before building again.");
                return true;
            }
            buildArena(player);
            player.sendMessage(ChatColor.GREEN + "Dilly Awards arena built! Face the stage from your starting position.");
            return true;
        }
        if (args[0].equalsIgnoreCase("clear")) {
            if (originals.isEmpty()) {
                player.sendMessage(ChatColor.YELLOW + "There is no saved arena to clear.");
                return true;
            }
            restoreArena();
            player.sendMessage(ChatColor.GREEN + "Arena removed and original blocks restored.");
            return true;
        }
        player.sendMessage(ChatColor.RED + "Unknown command. Use /awards help.");
        return true;
    }

    private void buildArena(Player player) {
        Location loc = player.getLocation();
        buildWorld = player.getWorld();
        originX = loc.getBlockX();
        originY = loc.getBlockY();
        originZ = loc.getBlockZ();

        double yaw = Math.toRadians(loc.getYaw());
        forward = new Vector(-Math.sin(yaw), 0, Math.cos(yaw)).normalize();
        right = new Vector(forward.getZ(), 0, -forward.getX()).normalize();

        // Ground platform: 43 blocks wide x 49 blocks deep, extending forward.
        for (int z = 0; z <= 48; z++) {
            for (int x = -21; x <= 21; x++) {
                Material floor = (Math.abs(x) <= 1 && z < 34) ? Material.RED_CARPET
                        : ((x + z) % 8 == 0 ? Material.DEEPSLATE_TILES : Material.POLISHED_BLACKSTONE);
                put(x, 0, z, floor);
                put(x, 1, z, Material.AIR);
                put(x, 2, z, Material.AIR);
                put(x, 3, z, Material.AIR);
                put(x, 4, z, Material.AIR);
                put(x, 5, z, Material.AIR);
                put(x, 6, z, Material.AIR);
                put(x, 7, z, Material.AIR);
                put(x, 8, z, Material.AIR);
                put(x, 9, z, Material.AIR);
                put(x, 10, z, Material.AIR);
            }
        }

        // Electric-blue perimeter trim.
        for (int z = 0; z <= 48; z++) {
            put(-21, 1, z, Material.CYAN_CONCRETE);
            put(21, 1, z, Material.CYAN_CONCRETE);
            if (z % 4 == 0) {
                put(-21, 2, z, Material.SEA_LANTERN);
                put(21, 2, z, Material.SEA_LANTERN);
            }
        }
        for (int x = -21; x <= 21; x++) {
            put(x, 1, 0, Material.CYAN_CONCRETE);
            put(x, 1, 48, Material.CYAN_CONCRETE);
        }

        // Audience seating: six rows, seats facing the stage.
        for (int row = 0; row < 6; row++) {
            int z = 7 + row * 4;
            for (int x = -17; x <= 17; x += 4) {
                if (Math.abs(x) <= 2) continue; // preserve the red-carpet aisle
                put(x, 1, z, Material.BLUE_CONCRETE);
                put(x, 2, z, Material.LIGHT_BLUE_CARPET);
                put(x, 1, z - 1, Material.BLACK_CONCRETE);
            }
        }

        // Raised main stage.
        for (int z = 35; z <= 46; z++) {
            for (int x = -18; x <= 18; x++) {
                put(x, 1, z, Material.BLACK_CONCRETE);
                if (z == 35 || z == 46 || x == -18 || x == 18) {
                    put(x, 2, z, Material.CYAN_CONCRETE);
                } else {
                    put(x, 2, z, Material.POLISHED_BLACKSTONE);
                }
            }
        }
        // Stage front steps.
        for (int level = 0; level < 3; level++) {
            for (int x = -7; x <= 7; x++) {
                put(x, 1 + level, 34 - level, level == 2 ? Material.CYAN_CONCRETE : Material.BLUE_CONCRETE);
            }
        }

        // Giant screen and frame at the rear of the stage.
        for (int x = -15; x <= 15; x++) {
            for (int y = 3; y <= 11; y++) {
                put(x, y, 46, Material.BLACK_CONCRETE);
            }
        }
        for (int x = -16; x <= 16; x++) {
            put(x, 3, 46, Material.CYAN_CONCRETE);
            put(x, 11, 46, Material.CYAN_CONCRETE);
        }
        for (int y = 3; y <= 11; y++) {
            put(-16, y, 46, Material.CYAN_CONCRETE);
            put(16, y, 46, Material.CYAN_CONCRETE);
        }
        // Pixel-art D emblem in lime green on the screen's left side.
        int[][] dPixels = {
            {1,1,1,1,0}, {1,0,0,0,1}, {1,0,0,0,1}, {1,0,0,0,1},
            {1,0,0,0,1}, {1,0,0,0,1}, {1,1,1,1,0}
        };
        for (int yy = 0; yy < dPixels.length; yy++) {
            for (int xx = 0; xx < dPixels[yy].length; xx++) {
                if (dPixels[yy][xx] == 1) put(-11 + xx, 5 + (6 - yy), 45, Material.LIME_CONCRETE);
            }
        }
        // Screen center glow panel.
        for (int x = -3; x <= 12; x++) {
            for (int y = 5; y <= 9; y++) {
                put(x, y, 45, Material.BLUE_STAINED_GLASS);
            }
        }
        for (int x = -4; x <= 13; x++) {
            put(x, 4, 45, Material.LIGHT_BLUE_CONCRETE);
            put(x, 10, 45, Material.LIGHT_BLUE_CONCRETE);
        }

        // Host podium and two side plinths.
        pillar(0, 3, 38, Material.LIME_CONCRETE, Material.BLACK_CONCRETE);
        put(0, 4, 38, Material.LIGHT_BLUE_STAINED_GLASS);
        pillar(-10, 3, 40, Material.CYAN_CONCRETE, Material.BLACK_CONCRETE);
        pillar(10, 3, 40, Material.CYAN_CONCRETE, Material.BLACK_CONCRETE);

        // Lighting towers around the stage and audience.
        for (int z : new int[]{5, 18, 33, 44}) {
            for (int x : new int[]{-19, 19}) {
                for (int y = 1; y <= 7; y++) put(x, y, z, y == 7 ? Material.SEA_LANTERN : Material.CYAN_CONCRETE);
                put(x + (x < 0 ? 1 : -1), 7, z, Material.SEA_LANTERN);
            }
        }

        // Backstage divider and waiting bays.
        for (int x = -17; x <= 17; x++) {
            put(x, 1, 47, Material.BLUE_CONCRETE);
            put(x, 2, 47, Material.BLUE_CONCRETE);
        }
        for (int x : new int[]{-12, -6, 6, 12}) {
            for (int y = 1; y <= 3; y++) put(x, y, 42, Material.LIGHT_BLUE_STAINED_GLASS);
        }

        saveSnapshot();
    }

    private void pillar(int x, int height, int z, Material trim, Material body) {
        for (int y = 1; y <= height; y++) {
            put(x, y, z, y == height ? trim : body);
            put(x - 1, y, z, body);
            put(x + 1, y, z, body);
        }
    }

    private void put(int lateral, int up, int depth, Material material) {
        Vector offset = right.clone().multiply(lateral).add(forward.clone().multiply(depth));
        int x = originX + (int) Math.round(offset.getX());
        int z = originZ + (int) Math.round(offset.getZ());
        int y = originY + up;
        Block block = buildWorld.getBlockAt(x, y, z);
        String key = x + "," + y + "," + z;
        originals.putIfAbsent(key, block.getBlockData().getAsString());
        block.setType(material, false);
    }

    private void restoreArena() {
        for (Map.Entry<String, String> entry : originals.entrySet()) {
            String[] parts = entry.getKey().split(",");
            int x = Integer.parseInt(parts[0]);
            int y = Integer.parseInt(parts[1]);
            int z = Integer.parseInt(parts[2]);
            try {
                BlockData data = Bukkit.createBlockData(entry.getValue());
                if (buildWorld != null) buildWorld.getBlockAt(x, y, z).setBlockData(data, false);
            } catch (IllegalArgumentException ex) {
                getLogger().warning("Could not restore block " + entry.getKey() + ": " + ex.getMessage());
            }
        }
        originals.clear();
        if (snapshotFile.exists()) snapshotFile.delete();
    }

    private void saveSnapshot() {
        if (originals.isEmpty()) return;
        org.bukkit.configuration.file.YamlConfiguration yaml = new org.bukkit.configuration.file.YamlConfiguration();
        yaml.set("world", buildWorld == null ? null : buildWorld.getName());
        yaml.set("blocks", originals);
        try {
            yaml.save(snapshotFile);
        } catch (IOException ex) {
            getLogger().severe("Could not save arena snapshot: " + ex.getMessage());
        }
    }

    private void loadSnapshot() {
        if (!snapshotFile.exists()) return;
        org.bukkit.configuration.file.YamlConfiguration yaml = org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(snapshotFile);
        String worldName = yaml.getString("world");
        buildWorld = worldName == null ? null : Bukkit.getWorld(worldName);
        if (buildWorld == null) {
            getLogger().warning("Saved arena world is not loaded. Snapshot retained; load that world before /awards clear.");
            return;
        }
        if (yaml.isConfigurationSection("blocks")) {
            for (String key : yaml.getConfigurationSection("blocks").getKeys(false)) {
                originals.put(key, yaml.getString("blocks." + key));
            }
        }
    }
}
