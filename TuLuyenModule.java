package dev.xianxia.modules;

import dev.xianxia.XianxiaPlugin;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.*;

import java.util.*;

public class TuLuyenModule implements Module, Listener {

    private XianxiaPlugin plugin;
    private final Map<UUID, Integer> tuViMap = new HashMap<>();
    private final Map<UUID, Realm> realmMap = new HashMap<>();

    private final int MAX_TU_VI = 10000;

    public enum Realm {
        PHAM_NHAN(0, 0),
        LUYEN_KHI(100, 0),
        TRUC_CO(300, 1),
        KIM_DAN(800, 1),
        NGUYEN_ANH(1500, 2),
        HOA_THAN(2500, 2),
        PHI_THANG(4000, 3),
        CHAN_TIEN(6000, 3),
        TIEN_TON(8000, 4),
        TIEN_VUONG(10000, 5);

        public final int requiredTuVi;
        public final int buffLevel;

        Realm(int requiredTuVi, int buffLevel) {
            this.requiredTuVi = requiredTuVi;
            this.buffLevel = buffLevel;
        }
    }

    @Override
    public void enable(XianxiaPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        plugin.getLogger().info("§a[TuLuyen] Module tu luyện đã được bật.");
    }

    @Override
    public void disable() {
        plugin.getLogger().info("§c[TuLuyen] Module tu luyện đã được tắt.");
    }

    @Override
    public String getName() {
        return "Tu Luyện";
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        tuViMap.putIfAbsent(uuid, 0);
        realmMap.putIfAbsent(uuid, Realm.PHAM_NHAN);
        applyBuff(event.getPlayer());
    }

    public void addTuVi(Player player, int amount) {
        UUID uuid = player.getUniqueId();
        int current = tuViMap.getOrDefault(uuid, 0);
        current += amount;
        current = Math.min(current, MAX_TU_VI);
        tuViMap.put(uuid, current);
        player.sendMessage("§b[TuLuyện] Tu vi hiện tại: §d" + current);

        tryBreakthrough(player);
    }

    public void tryBreakthrough(Player player) {
        UUID uuid = player.getUniqueId();
        int currentTuVi = tuViMap.getOrDefault(uuid, 0);
        Realm currentRealm = realmMap.getOrDefault(uuid, Realm.PHAM_NHAN);

        for (Realm realm : Realm.values()) {
            if (currentTuVi >= realm.requiredTuVi && realm.ordinal() > currentRealm.ordinal()) {
                realmMap.put(uuid, realm);
                player.sendMessage("§6[ĐỘT PHÁ] Bạn đã đột phá đến cảnh giới: §e" + realm.name());
                callHeavenlyTribulation(player);
                applyBuff(player);
            }
        }
    }

    public void callHeavenlyTribulation(Player player) {
        Location loc = player.getLocation();
        loc.getWorld().strikeLightningEffect(loc);
        loc.getWorld().playSound(loc, Sound.ENTITY_ENDER_DRAGON_GROWL, 2f, 1f);
        player.sendTitle("§c⚡ Thiên Kiếp ⚡", "§eĐột phá bắt đầu!", 10, 40, 10);
    }

    public void openTuViGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, "§dTu Luyện");

        // Slot 11: Info
        ItemStack info = new ItemStack(Material.EXPERIENCE_BOTTLE);
        ItemMeta meta = info.getItemMeta();
        meta.setDisplayName("§aTu Vi: §b" + tuViMap.getOrDefault(player.getUniqueId(), 0));
        meta.setLore(List.of(
                "§7Cảnh giới hiện tại: §d" + realmMap.get(player.getUniqueId()).name(),
                "§7Click vào đan dược để tu luyện nhanh"
        ));
        info.setItemMeta(meta);
        gui.setItem(11, info);

        // Slot 13: Đan dược
        ItemStack dan = new ItemStack(Material.GLOW_BERRIES);
        ItemMeta danMeta = dan.getItemMeta();
        danMeta.setDisplayName("§6Đan Tu Luyện (+200 tu vi)");
        dan.setItemMeta(danMeta);
        gui.setItem(13, dan);

        player.openInventory(gui);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals("§dTu Luyện")) return;
        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();
        if (event.getRawSlot() == 13) {
            addTuVi(player, 200);
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
        }
    }

    public void applyBuff(Player player) {
        UUID uuid = player.getUniqueId();
        Realm realm = realmMap.getOrDefault(uuid, Realm.PHAM_NHAN);

        // Xoá buff cũ
        player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));

        int level = realm.buffLevel;

        if (level > 0) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.HEALTH_BOOST, Integer.MAX_VALUE, level));
            player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, Integer.MAX_VALUE, level));
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, level - 1));
        }

        // Tăng máu base nếu muốn
        AttributeInstance attr = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (attr != null) attr.setBaseValue(20.0 + level * 4);
    }
}
