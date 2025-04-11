
package dev.xianxia.modules;

import dev.xianxia.XianxiaPlugin;
import dev.xianxia.modules.Module;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class NhiemVuModule implements Module, Listener {

    private XianxiaPlugin plugin;

    public static class NhiemVu {
        public final String ten;
        public final String moTa;
        public final Material icon;
        public final int mucTieu;

        public NhiemVu(String ten, String moTa, Material icon, int mucTieu) {
            this.ten = ten;
            this.moTa = moTa;
            this.icon = icon;
            this.mucTieu = mucTieu;
        }
    }

    private final Map<UUID, Map<NhiemVu, Integer>> tienDo = new HashMap<>();
    private final List<NhiemVu> danhSachNhiemVu = new ArrayList<>();

    @Override
    public void enable(XianxiaPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);

        // Thêm nhiệm vụ mẫu
        danhSachNhiemVu.add(new NhiemVu("Giết 10 Zombie", "Tiêu diệt 10 Zombie trong thế giới.", Material.ROTTEN_FLESH, 10));
        danhSachNhiemVu.add(new NhiemVu("Thu thập 5 Kim cương", "Đào 5 khối kim cương.", Material.DIAMOND, 5));

        plugin.getLogger().info("[NhiemVu] Module nhiệm vụ đã được bật.");
    }

    @Override
    public void disable() {
        plugin.getLogger().info("[NhiemVu] Module nhiệm vụ đã được tắt.");
    }

    @Override
    public String getName() {
        return "Nhiệm Vụ";
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        tienDo.putIfAbsent(player.getUniqueId(), new HashMap<>());
        for (NhiemVu nv : danhSachNhiemVu) {
            tienDo.get(player.getUniqueId()).putIfAbsent(nv, 0);
        }
    }

    public void moGuiNhiemVu(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, "§aNhiệm Vụ");

        Map<NhiemVu, Integer> tienDoNguoiChoi = tienDo.getOrDefault(player.getUniqueId(), new HashMap<>());

        for (int i = 0; i < danhSachNhiemVu.size() && i < gui.getSize(); i++) {
            NhiemVu nv = danhSachNhiemVu.get(i);
            ItemStack item = new ItemStack(nv.icon);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName("§e" + nv.ten);
            List<String> lore = new ArrayList<>();
            lore.add("§7" + nv.moTa);
            lore.add("§fTiến độ: §a" + tienDoNguoiChoi.getOrDefault(nv, 0) + "/" + nv.mucTieu);
            meta.setLore(lore);
            item.setItemMeta(meta);
            gui.setItem(i, item);
        }

        player.openInventory(gui);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals("§aNhiệm Vụ")) {
            event.setCancelled(true);
        }
    }

    public void capNhatTienDo(Player player, String tenNhiemVu, int soLuong) {
        Map<NhiemVu, Integer> tienDoNguoiChoi = tienDo.getOrDefault(player.getUniqueId(), new HashMap<>());

        for (NhiemVu nv : tienDoNguoiChoi.keySet()) {
            if (nv.ten.equalsIgnoreCase(tenNhiemVu)) {
                int moi = Math.min(nv.mucTieu, tienDoNguoiChoi.get(nv) + soLuong);
                tienDoNguoiChoi.put(nv, moi);
                if (moi == nv.mucTieu) {
                    player.sendMessage("§aBạn đã hoàn thành nhiệm vụ: §e" + nv.ten + "!");
                    // TODO: Thêm phần thưởng
                }
                break;
            }
        }
    }
}
