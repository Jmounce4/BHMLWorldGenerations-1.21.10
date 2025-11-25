package me.bhml.bhmlworldgenerations.Listeners;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.EditSession;
//import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.bukkit.BukkitAdapter;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;


import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.IronGolem;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.entity.Villager;
import org.bukkit.entity.EntityType;

import java.util.Random;

import static org.bukkit.Bukkit.getServer;

public class WorldGenerations implements Listener {

    /*

    private boolean isFlatGrassArea(Location origin, int width, int length) {
        World world = origin.getWorld();
        int startX = origin.getBlockX();
        int startZ = origin.getBlockZ();
        int y = origin.getBlockY() - 1; // one block *below* the paste position

        for (int dx = 0; dx < width; dx++) {
            for (int dz = 0; dz < length; dz++) {
                Block block = world.getBlockAt(startX + dx, y, startZ + dz); // +X and +Z
                if (block.getType() != Material.GRASS_BLOCK) {
                    return false; // or log info here if debugging
                }
            }
        }

        return true; // All blocks under schematic are grass
    }

     */

    public Location findFlatArea(Chunk chunk) {
        World world = chunk.getWorld();
        int chunkX = chunk.getX() << 4;
        int chunkZ = chunk.getZ() << 4;



        for (int x = 0; x <= 16 - 11; x++) {
            for (int z = 0; z <= 16 - 9; z++) {
                int baseX = chunkX + x;
                int baseZ = chunkZ + z;
                int baseY = world.getHighestBlockYAt(baseX, baseZ);
                Block checkBlock = world.getBlockAt(baseX, baseY, baseZ);
                Material checkBlockType = checkBlock.getType();
                if (!checkBlockType.isSolid() || checkBlockType == Material.WATER){
                    break;
                }



                boolean flat = true;


                for (int dx = 0; dx < 11 && flat; dx++) {

                    if (!flat){
                        break;
                    }
                    for (int dz = 0; dz < 9 && flat; dz++) {
                        int checkX = baseX + dx;
                        int checkZ = baseZ + dz;
                        int groundY = world.getHighestBlockYAt(checkX, checkZ);

                        if (groundY != baseY) {
                            flat = false;
                            break;
                        }
                        Block block = world.getBlockAt(checkX, groundY, checkZ);
                        Material type = block.getType();
                        if (!(type.isSolid()) || type == Material.WATER || type == Material.LAVA ||!(type == Material.SAND || type == Material.GRASS_BLOCK || type == Material.DIRT)) {
                            //getServer().getLogger().info("Blocked by " + type + " at " + checkX + "," + (baseY - 1) + "," + checkZ);
                            flat = false;
                            break;

                        }
                    }
                }

                if (flat) {
                    getServer().getLogger().info("Found valid area at " + baseX + "," + baseY + "," + baseZ + " in " + world.getBiome(baseX, baseY, baseZ));
                    return new Location(world, baseX, baseY, baseZ); // NW corner
                }
            }
        }

        return null;
    }



    public void pasteSchematic(Location loc, String schematicName) {
        File schematicFile = new File("plugins/WorldEdit/schematics/" + schematicName + ".schem");
        if (!schematicFile.exists()) {
            Bukkit.getLogger().warning("Schematic file not found: " + schematicFile.getPath());
            return;
        }

        try {
            ClipboardFormat format = ClipboardFormats.findByFile(schematicFile);
            if (format == null) {
                Bukkit.getLogger().warning("Unsupported schematic format for: " + schematicFile.getName());
                return;
            }

            try (ClipboardReader reader = format.getReader(new FileInputStream(schematicFile))) {
                Clipboard clipboard = reader.read();

                int width = clipboard.getDimensions().x();
                int height = clipboard.getDimensions().y();
                int length = clipboard.getDimensions().z();

                com.sk89q.worldedit.world.World adaptedWorld = BukkitAdapter.adapt(loc.getWorld());

                try (EditSession editSession = WorldEdit.getInstance().newEditSession(adaptedWorld)) {
                    Operation operation = new ClipboardHolder(clipboard)
                            .createPaste(editSession)
                            .to(BlockVector3.at(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()))
                            .ignoreAirBlocks(true)
                            .build();

                    Operations.complete(operation);

                }

                //Scanning Build for
                for (int dx = 0; dx < width; dx++) {
                    for (int dy = 0; dy < height; dy++) {
                        for (int dz = 0; dz < length; dz++) {
                            Location checkLoc = loc.clone().add(dx, dy, dz);
                            Block block = checkLoc.getBlock();
                            if (schematicName.equals("Tent_Camp") || schematicName.equals("Tent_Camp_Snow") || schematicName.equals("Tent_Camp_Desert")) {
                                if (block.getType() == Material.CHEST) {
                                    Chest chest = (Chest) block.getState();
                                    chest.setLootTable(Bukkit.getLootTable(new NamespacedKey("minecraft", "chests/village/village_butcher")));
                                    chest.update();
                                }
                            }
                        }
                    }
                }

                if (schematicName.equals("Tent_Camp") || schematicName.equals("Tent_Camp_Snow") || schematicName.equals("Tent_Camp_Desert")) {
                    World world = loc.getWorld();
                    Location baseLoc = loc.clone();
                    Location villagerLoc1 = baseLoc.clone().add(12, 0, 6);
                    Location villagerLoc2 = baseLoc.clone().add(14, 0, 2);
                    Location ironGolemLoc = baseLoc.clone().add(16, 0, 6);

                    // Spawn two villagers
                    Villager villager1 = (Villager) world.spawnEntity(villagerLoc1, org.bukkit.entity.EntityType.VILLAGER);
                    Villager villager2 = (Villager) world.spawnEntity(villagerLoc2, EntityType.VILLAGER);
                    IronGolem ironGolem1 = (IronGolem) world.spawnEntity(ironGolemLoc, EntityType.IRON_GOLEM);




                }


                Bukkit.getLogger().info("Pasted schematic at: " + loc);
            }

        } catch (IOException | WorldEditException e) {
            e.printStackTrace();
        }
    }
    private final Random random = new Random();

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        if (!event.isNewChunk()) return;
        Chunk chunk = event.getChunk();
        World world = chunk.getWorld();

        // 2.3 For testing, use a fairly high spawn chance (e.g. 1 in 4)
        //      so you can see tents pop up often.
        if (random.nextInt(35) != 0) return; // chance per new chunk, base 1/35


        Location flatLoc = findFlatArea(chunk);
        if (flatLoc == null) return;
        // 2.5 Check the biome at that top‐of‐terrain spot
        Biome biome = flatLoc.getBlock().getBiome();
        if (biome == Biome.PLAINS
                && biome == Biome.SUNFLOWER_PLAINS
                && biome == Biome.MEADOW
                && biome == Biome.DESERT)
        {
            if (random.nextInt(3) != 0) return; //Reduces odds in generally flat biomes by 66%
        }

        if (biome == Biome.SNOWY_PLAINS){
            if (random.nextInt(5) != 0) return;
        }



        if (biome == Biome.SAVANNA
                && biome == Biome.SAVANNA_PLATEAU
                && biome == Biome.SAVANNA){
            if (random.nextInt(2) != 0) return; //Reduces odds in Savannahs by 50%

        }


        // 2.6 Paste the schematic at this location
        Location pasteLoc = flatLoc.clone().add(0, 1, 0);
        if (biome == Biome.DESERT){
            pasteSchematic(pasteLoc,  "Tent_Camp_Desert");
            getServer().getLogger().info("Spawning tent at chunk " + chunk.getX() + "," + chunk.getZ());
            return;
        }
        if (biome == Biome.SNOWY_PLAINS || biome == Biome.SNOWY_BEACH || biome == Biome.SNOWY_SLOPES){
            pasteSchematic(pasteLoc,  "Tent_Camp_Snow");
            getServer().getLogger().info("Spawning tent at chunk " + chunk.getX() + "," + chunk.getZ());
            return;
        }
        pasteSchematic(pasteLoc,  "Tent_Camp");
        getServer().getLogger().info("Spawning tent at chunk " + chunk.getX() + "," + chunk.getZ());
    }


}
