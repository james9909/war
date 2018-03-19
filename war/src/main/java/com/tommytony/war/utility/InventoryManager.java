package com.tommytony.war.utility;

import com.tommytony.war.War;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Behavior adapted from MobArena's own InventoryManager
 */
public class InventoryManager {

    private HashMap<String, ItemStack[]> items;
    private HashMap<String, ItemStack[]> armor;

    private File dir;

    public InventoryManager() {
        this.dir = new File(War.war.getDataFolder(), "inventories");

        this.items = new HashMap<>();
        this.armor = new HashMap<>();
    }

    public void saveInventory(Player player) throws IOException {
        ItemStack[] items = player.getInventory().getContents();
        ItemStack[] armor = player.getInventory().getArmorContents();
        ItemStack offhand = player.getInventory().getItemInOffHand();

        File file = new File(dir, player.getName());
        YamlConfiguration config = new YamlConfiguration();
        config.set("items", items);
        config.set("armor", armor);
        config.set("offhand", offhand);

        config.save(file);
    }

    public void restoreInventory(Player player) throws IOException, InvalidConfigurationException {
        ItemStack[] items = this.items.get(player.getName());
        ItemStack[] armor = this.armor.get(player.getName());

        // If we can't restore from memory, restore from file
        if (items == null || armor == null) {
            File file = new File(dir, player.getName());

            YamlConfiguration config = new YamlConfiguration();
            config.load(file);

            List<?> itemsList = config.getList("items");
            List<?> armorList = config.getList("armor");

            items = itemsList.toArray(new ItemStack[itemsList.size()]);
            armor = armorList.toArray(new ItemStack[armorList.size()]);
            file.delete();
        }

        player.getInventory().setContents(items);
        player.getInventory().setArmorContents(armor);
    }

}
