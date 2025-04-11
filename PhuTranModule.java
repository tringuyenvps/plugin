package dev.xianxia.modules;

import dev.xianxia.XianxiaPlugin;
import dev.xianxia.modules.Module;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class PhuTranModule implements Module, Listener {

    private XianxiaPlugin plugin;
    private final Map<UUID, List<ItemStack>> playerPhuTrans = new HashMap<>();

    @Override
    public void enable(XianxiaPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        plugin.getLogger().info("§a[PhùTrận] Module Phù Trận đã bật.");
    }

    @Override
    public void disable() {
        playerPhuTrans.clear();
        plugin.getLogger().info("§c[PhùTrận] Module Phù Trận đã tắt.");
    }

    @Override
    public String getName() {
        return "Phù Trận";
    }

    public void openPhuTranCraftGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, "§6Chế Tạo Phù Trận");

        gui.setItem(11, createItem(Material.PAPER, "§aNguyên Liệu 1"));
        gui.setItem(13, createItem(Material.PAPER, "§aNguyên Liệu 2"));
        gui.setItem(15, createItem(Material.ENCHANTED_BOOK, "§bChế Tạo"));

        player.openInventory(gui);
    }

    public void openPhuTranUpgradeGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, "§eNâng Cấp Phù Trận");

        gui.setItem(11, createItem(Material.ENCHANTED_BOOK, "§aPhù Trận Hiện Tại"));
        gui.setItem(13, createItem(Material.EXPERIENCE_BOTTLE, "§aKinh Nghiệm / Nguyên Liệu Nâng Cấp"));
        gui.setItem(15, createItem(Material.ANVIL, "§bNâng Cấp"));

        player.openInventory(gui);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals("§6Chế Tạo Phù Trận")) {
            event.setCancelled(true);
            Player player = (Player) event.getWhoClicked();
            if (event.getRawSlot() == 15) {
                // Giả lập chế tạo phù trận
                ItemStack created = createPhuTran("Phù Trận Sơ Cấp", 1);
                player.getInventory().addItem(created);
                player.sendMessage("§aBạn đã chế tạo thành công một Phù Trận!");
                player.closeInventory();
            }
        } else if (event.getView().getTitle().equals("§eNâng Cấp Phù Trận")) {
            event.setCancelled(true);
            Player player = (Player) event.getWhoClicked();
            if (event.getRawSlot() == 15) {
                // Giả lập nâng cấp
                ItemStack upgraded = createPhuTran("Phù Trận Trung Cấp", 2);
                player.getInventory().addItem(upgraded);
                player.sendMessage("§aPhù Trận đã được nâng cấp thành công!");
                player.closeInventory();
            }
        }
    }

    @EventHandler
    public void onBossKillDropSkillBook(EntityDeathEvent event) {
        if (event.getEntity().getCustomName() != null && event.getEntity().getCustomName().contains("Boss")) {
            if (event.getEntity().getKiller() != null) {
                Player killer = event.getEntity().getKiller();
                ItemStack skillBook = createItem(Material.BOOK, "§dSách Kỹ Năng Phù Trận", "§7Dùng để học kỹ năng phù trận đặc biệt.");
                killer.getInventory().addItem(skillBook);
                killer.sendMessage("§dBạn đã nhận được §fSách Kỹ Năng Phù Trận §dtừ boss!");
            }
        }
    }

    private ItemStack createPhuTran(String name, int level) {
        ItemStack item = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§b" + name + " §7[Lv." + level + "]");
        meta.setLore(List.of("§7Một phù trận có thể kích hoạt hiệu ứng đặc biệt.", "§fCấp độ: " + level));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createItem(Material mat, String name, String... loreLines) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        if (loreLines.length > 0) meta.setLore(Arrays.asList(loreLines));
        item.setItemMeta(meta);
        return item;
    }
}
