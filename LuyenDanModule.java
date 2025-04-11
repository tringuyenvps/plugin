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
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.*;

public class LuyenDanModule implements Module, Listener {

    private XianxiaPlugin plugin;
    private List<DanCongThuc> danCongThucList = new ArrayList<>();
    private final Random random = new Random();

    public static class DanCongThuc {
        public List<Material> nguyenLieu;
        public ItemStack ketQua;
        public double tiLeThanhCong;

        public DanCongThuc(List<Material> nguyenLieu, ItemStack ketQua, double tiLeThanhCong) {
            this.nguyenLieu = nguyenLieu;
            this.ketQua = ketQua;
            this.tiLeThanhCong = tiLeThanhCong;
        }
    }

    @Override
    public void enable(XianxiaPlugin plugin) {
        this.plugin = plugin;

        File file = new File(plugin.getDataFolder(), "config/config-luyen-dan.yml");
        if (!file.exists()) plugin.saveResource("config/config-luyen-dan.yml", false);

        Bukkit.getPluginManager().registerEvents(this, plugin);
        loadConfig();
        plugin.getLogger().info("§a[LuyenDan] Module luyện đan đã được bật.");
    }

    @Override
    public void disable() {
        plugin.getLogger().info("§c[LuyenDan] Module luyện đan đã được tắt.");
    }

    @Override
    public String getName() {
        return "Luyện Đan";
    }

    private void loadConfig() {
        danCongThucList.clear();

        // Example: tự thêm một công thức - có thể đọc từ file sau
        danCongThucList.add(new DanCongThuc(
                Arrays.asList(Material.BLAZE_POWDER, Material.SUGAR, Material.GHAST_TEAR),
                createDan("Đan Dược Hồi Máu", Material.POTION, 1),
                0.7
        ));

        danCongThucList.add(new DanCongThuc(
                Arrays.asList(Material.NETHER_WART, Material.GOLD_NUGGET, Material.SPIDER_EYE),
                createDan("Đan Dược Tăng Sức Mạnh", Material.POTION, 2),
                0.5
        ));
    }

    private ItemStack createDan(String name, Material type, int level) {
        ItemStack item = new ItemStack(type);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§d" + name + " §7[Cấp " + level + "]");
        item.setItemMeta(meta);
        return item;
    }

    public void openGiaoDienLuyenDan(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, "§cLò Luyện Đan");

        gui.setItem(11, new ItemStack(Material.BLAZE_POWDER)); // Nguyên liệu 1
        gui.setItem(12, new ItemStack(Material.SUGAR));        // Nguyên liệu 2
        gui.setItem(13, new ItemStack(Material.GHAST_TEAR));   // Nguyên liệu 3
        gui.setItem(15, new ItemStack(Material.BREWING_STAND)); // Nút Luyện

        player.openInventory(gui);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (e.getView().getTitle().equals("§cLò Luyện Đan")) {
            e.setCancelled(true);
            if (e.getRawSlot() == 15 && e.getWhoClicked() instanceof Player) {
                Player player = (Player) e.getWhoClicked();
                Inventory inv = e.getInventory();

                List<Material> nguyenLieu = Arrays.asList(
                        getMaterial(inv.getItem(11)),
                        getMaterial(inv.getItem(12)),
                        getMaterial(inv.getItem(13))
                );

                Optional<DanCongThuc> match = danCongThucList.stream()
                        .filter(d -> new HashSet<>(d.nguyenLieu).equals(new HashSet<>(nguyenLieu)))
                        .findFirst();

                if (match.isPresent()) {
                    DanCongThuc congThuc = match.get();
                    if (random.nextDouble() <= congThuc.tiLeThanhCong) {
                        player.getInventory().addItem(congThuc.ketQua.clone());
                        player.sendMessage("§aBạn đã luyện đan thành công!");
                        player.playSound(player.getLocation(), Sound.BLOCK_BREWING_STAND_BREW, 1f, 1f);
                    } else {
                        player.sendMessage("§cLuyện đan thất bại!");
                        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1f, 0.5f);
                    }
                    player.closeInventory();
                } else {
                    player.sendMessage("§eKhông có công thức phù hợp.");
                }
            }
        }
    }

    private Material getMaterial(ItemStack item) {
        return item == null ? Material.AIR : item.getType();
    }
}
