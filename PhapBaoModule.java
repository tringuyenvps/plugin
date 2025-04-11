package dev.xianxia.modules;

import dev.xianxia.XianxiaPlugin;
import dev.xianxia.modules.Module;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class PhapBaoModule implements Module, Listener {

    private XianxiaPlugin plugin;
    private final Map<UUID, ItemStack> equippedPhapBao = new HashMap<>();
    private final String GUI_TITLE = "§6Ống Độ Pháp Bảo";

    @Override
    public void enable(XianxiaPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        plugin.getLogger().info("§a[PhapBao] Module pháp bảo đã bật.");
    }

    @Override
    public void disable() {
        equippedPhapBao.clear();
        plugin.getLogger().info("§c[PhapBao] Module pháp bảo đã tắt.");
    }

    @Override
    public String getName() {
        return "Pháp Bảo";
    }

    public ItemStack createPhapBao(String name, List<String> lore, int level) {
        ItemStack item = new ItemStack(Material.BLAZE_ROD);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§6Pháp Bảo: §e" + name + " §7[Lv." + level + "]");
            lore.add("§7Cấp độ: §e" + level);
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    public void equipPhapBao(Player player, ItemStack item) {
        equippedPhapBao.put(player.getUniqueId(), item);
        player.sendMessage("§a[Pháp Bảo] Bạn đã trang bị pháp bảo: §e" + item.getItemMeta().getDisplayName());
        applyPhapBaoBuff(player, item);
    }

    private void applyPhapBaoBuff(Player player, ItemStack item) {
        int level = extractLevel(item);
        player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, Integer.MAX_VALUE, level));
    }

    private int extractLevel(ItemStack item) {
        if (item.hasItemMeta() && item.getItemMeta().hasLore()) {
            for (String line : item.getItemMeta().getLore()) {
                if (line.contains("Cấp độ:")) {
                    String[] parts = line.split(":");
                    try {
                        return Integer.parseInt(parts[1].trim());
                    } catch (Exception ignored) {}
                }
            }
        }
        return 1;
    }

    public void openUpgradeGui(Player player) {
        Inventory gui = Bukkit.createInventory(null, 9, GUI_TITLE);
        gui.setItem(3, new ItemStack(Material.BLAZE_ROD)); // chỗ đặt pháp bảo
        gui.setItem(5, new ItemStack(Material.NETHER_STAR)); // nguyên liệu nâng cấp
        player.openInventory(gui);
    }

    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item != null && item.hasItemMeta() && item.getItemMeta().getDisplayName().contains("Pháp Bảo")) {
            equipPhapBao(player, item);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals(GUI_TITLE)) {
            event.setCancelled(true);
            Player player = (Player) event.getWhoClicked();

            if (event.getSlot() == 8) {
                ItemStack phapBao = event.getInventory().getItem(3);
                ItemStack nguyenLieu = event.getInventory().getItem(5);

                if (phapBao != null && nguyenLieu != null && nguyenLieu.getType() == Material.NETHER_STAR) {
                    int level = extractLevel(phapBao);
                    if (level < 10) {
                        ItemStack upgraded = createPhapBao("Cường Hóa", new ArrayList<>(), level + 1);
                        player.getInventory().addItem(upgraded);
                        player.sendMessage("§a[Pháp Bảo] Nâng cấp thành công pháp bảo lên cấp §e" + (level + 1));
                    } else {
                        player.sendMessage("§c[Pháp Bảo] Đã đạt cấp tối đa!");
                    }
                } else {
                    player.sendMessage("§c[Pháp Bảo] Bạn cần đặt pháp bảo và nguyên liệu để nâng cấp!");
                }
            }
        }
    }
}
