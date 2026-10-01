package com.school.security;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.inventory.PrepareSmithingEvent;
import org.bukkit.event.loot.LootGenerateEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class ItemRestrictionPlugin extends JavaPlugin implements Listener {

    private static final Map<Material, Material> tool_map = new EnumMap<>(Material.class);
    private static final Set<Material> armor_set = EnumSet.of(
        Material.NETHERITE_HELMET,
        Material.NETHERITE_CHESTPLATE,
        Material.NETHERITE_LEGGINGS,
        Material.NETHERITE_BOOTS
    );
    private static final Set<Material> spear_set = EnumSet.noneOf(Material.class);

    static {
        tool_map.put(Material.NETHERITE_SWORD,   Material.WOODEN_SWORD);
        tool_map.put(Material.NETHERITE_PICKAXE, Material.WOODEN_PICKAXE);
        tool_map.put(Material.NETHERITE_AXE,     Material.WOODEN_AXE);
        tool_map.put(Material.NETHERITE_SHOVEL,  Material.WOODEN_SHOVEL);
        tool_map.put(Material.NETHERITE_HOE,     Material.WOODEN_HOE);

        for (Material current_material : Material.values()) {
            if (current_material.name().contains("SPEAR")) {
                spear_set.add(current_material);
            }
        }
    }

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("schoolservernonetheritenospearnomace has been loaded successfully!");
    }

    @Override
    public void onDisable() {
        getLogger().info("schoolservernonetheritenospearnomace has been stopped.");
    }

    @EventHandler(ignoreCancelled = true)
    public void on_prepare_smithing(PrepareSmithingEvent event) {
        ItemStack result_item = event.getResult();
        if (result_item == null) return;

        Material item_type = result_item.getType();

        if (armor_set.contains(item_type)) {
            event.setResult(make_restricted_item(Material.ROTTEN_FLESH, "§r§6REAL NETHERITE ARMOR!"));
            return;
        }

        Material replacement_type = tool_map.get(item_type);
        if (replacement_type != null) {
            event.setResult(new ItemStack(replacement_type, 1));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void on_loot_generate(LootGenerateEvent event) {
        List<ItemStack> loot_list = event.getLoot();

        for (int i = 0, list_size = loot_list.size(); i < list_size; i++) {
            ItemStack current_item = loot_list.get(i);
            if (current_item != null && current_item.getType() == Material.HEAVY_CORE) {
                loot_list.set(i, make_restricted_item(Material.IRON_NUGGET, "§r§7heavy core (from Temu)"));
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void on_prepare_craft(PrepareItemCraftEvent event) {
        ItemStack current_result = event.getInventory().getResult();
        if (current_result == null) return;

        Material result_type = current_result.getType();

        if (result_type == Material.MACE || spear_set.contains(result_type)) {
            event.getInventory().setResult(new ItemStack(Material.AIR));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void on_inventory_click(InventoryClickEvent event) {
        ItemStack clicked_item = event.getCurrentItem();
        if (clicked_item == null) return;

        Material item_type = clicked_item.getType();

        boolean is_mace = item_type == Material.MACE;
        if (!is_mace && !spear_set.contains(item_type)) return;

        event.setCurrentItem(new ItemStack(Material.AIR));
        if (event.getWhoClicked() instanceof Player current_player) {
            current_player.sendMessage(is_mace
                ? "§c[School Server] The Mace is blocked on this server."
                : "§c[School Server] Spears are blocked on this server.");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void on_entity_death(EntityDeathEvent event) {
        List<ItemStack> drops = event.getDrops();
        if (drops.isEmpty()) return;
        drops.removeIf(drop_item -> drop_item != null && spear_set.contains(drop_item.getType()));
    }

    private ItemStack make_restricted_item(Material material, String item_name) {
        ItemStack new_item = new ItemStack(material, 1);
        ItemMeta item_meta = new_item.getItemMeta();
        if (item_meta != null) {
            item_meta.setDisplayName(item_name);
            new_item.setItemMeta(item_meta);
        }
        return new_item;
    }
}
