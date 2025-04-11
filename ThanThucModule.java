package dev.xianxia.modules;

import dev.xianxia.XianxiaPlugin;
import dev.xianxia.modules.Module;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class ThanThucModule implements Module, Listener {

    private XianxiaPlugin plugin;
    private final Map<UUID, Integer> thanThucLevel = new HashMap<>();

    @Override
    public void enable(XianxiaPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        plugin.getLogger().info("§a[ThanThuc] Module Thần Thức đã được bật.");
    }

    @Override
    public void disable() {
        thanThucLevel.clear();
        plugin.getLogger().info("§c[ThanThuc] Module Thần Thức đã được tắt.");
    }

    @Override
    public String getName() {
        return "Thần Thức";
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();
        thanThucLevel.putIfAbsent(uuid, 1);
    }

    @EventHandler
    public void onUseThuc(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item != null && item.getType() == Material.ENDER_EYE) {
            int level = thanThucLevel.getOrDefault(player.getUniqueId(), 1);
            int range = 10 + level * 5;

            player.sendMessage("§b[Thần Thức] Đang quét khu vực xung quanh...");

            int found = 0;
            for (Entity entity : player.getNearbyEntities(range, range, range)) {
                if (entity instanceof LivingEntity living && !(living instanceof Player)) {
                    living.setGlowing(true);
                    found++;
                }
            }

            player.sendMessage("§a[Thần Thức] Đã phát hiện " + found + " thực thể.");
            player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.0f);
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onOpenGUI(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getItem() != null && event.getItem().getType() == Material.BOOK) {
            openThanThucGUI(player);
        }
    }

    private void openThanThucGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, "§dThần Thức");

        ItemStack levelInfo = new ItemStack(Material.ENDER_EYE);
        ItemMeta meta = levelInfo.getItemMeta();
        meta.setDisplayName("§eCấp độ Thần Thức: §a" + thanThucLevel.getOrDefault(player.getUniqueId(), 1));
        meta.setLore(List.of("§7Sử dụng Ender Eye để quét thực thể.", "§7Cấp độ càng cao, phạm vi càng rộng."));
        levelInfo.setItemMeta(meta);

        ItemStack upgradeItem = new ItemStack(Material.NETHER_STAR);
        ItemMeta upMeta = upgradeItem.getItemMeta();
        upMeta.setDisplayName("§6Nâng cấp Thần Thức");
        upMeta.setLore(List.of("§7Click để nâng cấp (tốn 1 Nether Star)"));
        upgradeItem.setItemMeta(upMeta);

        gui.setItem(11, levelInfo);
        gui.setItem(15, upgradeItem);

        player.openInventory(gui);
    }

    @EventHandler
    public void onClickGUI(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals("§dThần Thức")) return;

        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();

        if (clicked == null || !clicked.hasItemMeta()) return;

        if (clicked.getType() == Material.NETHER_STAR) {
            if (player.getInventory().containsAtLeast(new ItemStack(Material.NETHER_STAR), 1)) {
                player.getInventory().removeItem(new ItemStack(Material.NETHER_STAR, 1));
                int current = thanThucLevel.getOrDefault(player.getUniqueId(), 1);
                thanThucLevel.put(player.getUniqueId(), current + 1);
                player.sendMessage("§a[Thần Thức] Đã nâng cấp Thần Thức lên cấp §b" + (current + 1));
                player.closeInventory();
            } else {
                player.sendMessage("§c[Thần Thức] Bạn không có đủ Nether Star để nâng cấp.");
            }
        }
    }

    public int getThanThucLevel(Player player) {
        return thanThucLevel.getOrDefault(player.getUniqueId(), 1);
    }
}
