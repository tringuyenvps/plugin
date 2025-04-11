package dev.xianxia.modules;

import dev.xianxia.XianxiaPlugin;
import dev.xianxia.modules.Module;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.*;

public class TongMonModule implements Module, Listener {

    private XianxiaPlugin plugin;
    private YamlConfiguration config;

    private final Map<UUID, String> playerFaction = new HashMap<>();
    private final Map<String, Integer> factionLevels = new HashMap<>();

    @Override
    public void enable(XianxiaPlugin plugin) {
        this.plugin = plugin;

        File file = new File(plugin.getDataFolder(), "config/config-tongmon.yml");
        if (!file.exists()) plugin.saveResource("config/config-tongmon.yml", false);
        config = YamlConfiguration.loadConfiguration(file);

        Bukkit.getPluginManager().registerEvents(this, plugin);
        plugin.getLogger().info("§a[TongMon] Module tông môn đã được bật.");
    }

    @Override
    public void disable() {
        plugin.getLogger().info("§c[TongMon] Module tông môn đã được tắt.");
    }

    @Override
    public String getName() {
        return "Tông Môn";
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item.getType() == Material.BOOK && item.getItemMeta() != null &&
                item.getItemMeta().getDisplayName().equals("§6Sổ Tông Môn")) {
            openFactionGUI(player);
            event.setCancelled(true);
        }
    }

    public void openFactionGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, "§9Tông Môn");

        gui.setItem(10, createItem(Material.PAPER, "§eThông tin tông môn"));
        gui.setItem(11, createItem(Material.ANVIL, "§bNâng cấp tông môn"));
        gui.setItem(12, createItem(Material.GOLD_INGOT, "§6Đóng góp tài nguyên"));
        gui.setItem(13, createItem(Material.PLAYER_HEAD, "§aThành viên"));
        gui.setItem(14, createItem(Material.BARRIER, "§cRời tông môn"));

        player.openInventory(gui);
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
    }

    @EventHandler
    public void onGUIClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals("§9Tông Môn")) {
            event.setCancelled(true);
            Player player = (Player) event.getWhoClicked();

            switch (event.getSlot()) {
                case 10 -> player.sendMessage("§e[TM] Thông tin tông môn của bạn đang phát triển!");
                case 11 -> player.sendMessage("§b[TM] Hệ thống nâng cấp sẽ sớm được mở.");
                case 12 -> player.sendMessage("§6[TM] Chức năng đóng góp sẽ mở sau.");
                case 13 -> player.sendMessage("§a[TM] Danh sách thành viên hiện tại đang trống.");
                case 14 -> {
                    player.sendMessage("§c[TM] Bạn đã rời khỏi tông môn.");
                    playerFaction.remove(player.getUniqueId());
                }
            }
        }
    }

    private ItemStack createItem(Material mat, String name) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            item.setItemMeta(meta);
        }
        return item;
    }
}
