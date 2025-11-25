package me.bhml.bhmlworldgenerations.Listeners;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Chest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootTable;

import static org.bukkit.Bukkit.getServer;

public class WorldLoot implements Listener {
    @EventHandler
    public void onLootSpawn(LootGenerateEvent event) {
        InventoryHolder holder =  event.getInventoryHolder();
        if (!(holder instanceof Chest)) return;
        Inventory inventory = holder.getInventory();
        LootTable lootTable = event.getLootTable();
        if (lootTable == null) return;
        if (lootTable != null && lootTable.getKey() != null) {
            NamespacedKey key = lootTable.getKey();
            if(key == null) return;
            if (key.getNamespace().equals("minecraft") && key.getKey().contains("village")) {
                int amount = 1 + (int) (Math.random() * 10);
                ItemStack emeralds = new ItemStack(Material.EMERALD, amount);
                int inventoryCheck = 0;
                while (inventoryCheck == 0) {
                    int randomSlot = (int) (Math.random() * inventory.getSize());
                    ItemStack item = inventory.getItem(randomSlot);

                    // Null check — empty slots return null
                    if (item == null || item.getType() == Material.AIR) {
                        inventory.setItem(randomSlot, emeralds); // ← correct method!
                        getServer().getLogger().info("Villager Chest Editted!");
                        inventoryCheck = 1;
                    }
                }

            }
        }
    }


}
