package dev.xianxia.modules;

import dev.xianxia.XianxiaPlugin;
import dev.xianxia.modules.Module;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class CanhGioiModule implements Module, Listener {

    public enum CanhGioi {
        PHAM_NHAN(0, 1000, 0, 0, 0, 0),
        LUYEN_KHI(1, 2000, 2, 0, 0, 0),
        TRUC_CO(2, 4000, 4, 1, 0, 0),
        KIM_DAN(3, 8000, 6, 1, 1, 0),
        NGUYEN_ANH(4, 16000, 10, 2, 1, 0),
        HOA_THAN(5, 30000, 14, 2, 2, 1),
        PHAN_THAN(6, 50000, 18, 3, 2, 1),
        HOP_THE(7, 100000, 24, 3, 3, 2),
        DAI_THUA(8, 200000, 30, 4, 3, 2),
        HOA_HU(9, 400000, 40, 5, 4, 3),
        TU_CHANH(10, 800000, 50, 6, 5, 3),
        TIEN_NHAN(11, 1600000, 60, 7, 6, 4),
        TIEN_VUONG(12, 3200000, 80, 8, 8, 6),
        TIEN_TON(13, 6400000, 100, 10, 10, 8);

        private final int level;
        private final int tuViRequired;
        private final double bonusHP;
        private final int bonusStrength;
        private final int bonusSpeed;
        private final int bonusRegen;

        CanhGioi(int level, int tuViRequired, double bonusHP, int bonusStrength, int bonusSpeed, int bonusRegen) {
            this.level = level;
            this.tuViRequired = tuViRequired;
            this.bonusHP = bonusHP;
            this.bonusStrength = bonusStrength;
            this.bonusSpeed = bonusSpeed;
            this.bonusRegen = bonusRegen;
        }

        public int getLevel() { return level; }
        public int getTuViRequired() { return tuViRequired; }
        public double getBonusHP() { return bonusHP; }
        public int getBonusStrength() { return bonusStrength; }
        public int getBonusSpeed() { return bonusSpeed; }
        public int getBonusRegen() { return bonusRegen; }
    }

    private XianxiaPlugin plugin;
    private final Map<UUID, CanhGioi> playerCanhGioi = new HashMap<>();
    private final Random random = new Random();

    @Override
    public void enable(XianxiaPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        plugin.getLogger().info("§a[CanhGioi] Module cảnh giới đã được bật.");
    }

    @Override
    public void disable() {
        playerCanhGioi.clear();
        plugin.getLogger().info("§c[CanhGioi] Module cảnh giới đã được tắt.");
    }

    @Override
    public String getName() {
        return "Cảnh Giới";
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        playerCanhGioi.putIfAbsent(player.getUniqueId(), CanhGioi.PHAM_NHAN);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            applyBuffs(player, getCanhGioi(player));
        }, 20L);
    }

    public CanhGioi getCanhGioi(Player player) {
        return playerCanhGioi.getOrDefault(player.getUniqueId(), CanhGioi.PHAM_NHAN);
    }

    public void tryBreakthrough(Player player, int currentTuVi) {
        CanhGioi current = getCanhGioi(player);
        CanhGioi[] values = CanhGioi.values();
        if (current.getLevel() + 1 < values.length) {
            CanhGioi next = values[current.getLevel() + 1];
            if (currentTuVi >= next.getTuViRequired()) {
                player.sendMessage("§a[Đột Phá] Bạn đã đột phá cảnh giới: §e" + next.name().replace("_", " "));
                playerCanhGioi.put(player.getUniqueId(), next);
                applyBuffs(player, next);
            }
        }
    }

    public void openCanhGioiGUI(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "§9Cảnh Giới");

        for (CanhGioi cg : CanhGioi.values()) {
            ItemStack item = new ItemStack(Material.ENCHANTED_BOOK);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName("§6" + cg.name().replace("_", " "));
            List<String> lore = new ArrayList<>();
            lore.add("§7Yêu cầu tu vi: §e" + cg.getTuViRequired());
            lore.add("§7+ Máu: §a+" + cg.getBonusHP());
            lore.add("§7+ Sức mạnh: §c+" + cg.getBonusStrength());
            lore.add("§7+ Tốc độ: §b+" + cg.getBonusSpeed());
            lore.add("§7+ Hồi phục: §d+" + cg.getBonusRegen());
            meta.setLore(lore);
            item.setItemMeta(meta);
            inv.addItem(item);
        }

        player.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (e.getView().getTitle().equals("§9Cảnh Giới")) {
            e.setCancelled(true);
        }
    }

    private void applyBuffs(Player player, CanhGioi cg) {
        player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20 + cg.getBonusHP());
        player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, Integer.MAX_VALUE, cg.getBonusStrength()));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, cg.getBonusSpeed()));
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, Integer.MAX_VALUE, cg.getBonusRegen()));
    }
}
