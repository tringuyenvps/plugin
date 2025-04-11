package dev.xianxia.modules;

import dev.xianxia.XianxiaPlugin;
import dev.xianxia.modules.Module;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class BiCanhSuKienModule implements Module, Listener {

    private XianxiaPlugin plugin;
    private final Random random = new Random();
    private final Map<UUID, Long> cooldowns = new HashMap<>();

    public enum BiCanhType {
        PHAM_NHAN, TU_CHAN, HOANG_GIA, THAN_TIEN, DI_GIOI
    }

    public record BiCanhBoss(String name, EntityType type, double health, List<ItemStack> loot) {}

    private final Map<BiCanhType, List<BiCanhBoss>> bossMap = new HashMap<>();

    @Override
    public void enable(XianxiaPlugin plugin) {
        this.plugin = plugin;
        loadBossData();
        Bukkit.getPluginManager().registerEvents(this, plugin);
        plugin.getLogger().info("§a[BiCanh] Module Bí Cảnh Sự Kiện đã được bật.");
    }

    @Override
    public void disable() {
        plugin.getLogger().info("§c[BiCanh] Module Bí Cảnh Sự Kiện đã được tắt.");
    }

    @Override
    public String getName() {
        return "Bí Cảnh Sự Kiện";
    }

    @EventHandler
    public void onEnterEvent(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (cooldowns.containsKey(player.getUniqueId()) && cooldowns.get(player.getUniqueId()) > System.currentTimeMillis()) {
            player.sendMessage("§cBạn cần chờ thêm trước khi vào bí cảnh tiếp theo!");
            return;
        }

        BiCanhType type = getRandomBiCanh();
        spawnBiCanh(player, type);
        cooldowns.put(player.getUniqueId(), System.currentTimeMillis() + 60_000); // 1 phút cooldown
    }

    private BiCanhType getRandomBiCanh() {
        BiCanhType[] types = BiCanhType.values();
        return types[random.nextInt(types.length)];
    }

    private void spawnBiCanh(Player player, BiCanhType type) {
        Location loc = player.getLocation().add(0, 5, 0);
        BiCanhBoss boss = getRandomBoss(type);

        player.sendMessage("§d[Bí Cảnh] Bạn đã tiến vào bí cảnh §e" + type.name());
        player.sendMessage("§6Boss: §c" + boss.name() + " §7(§f" + boss.type().name() + "§7)");

        Entity entity = player.getWorld().spawnEntity(loc, boss.type());
        if (entity instanceof LivingEntity living) {
            living.setCustomName("§c" + boss.name());
            living.setCustomNameVisible(true);
            living.setMaxHealth(boss.health());
            living.setHealth(boss.health());

            new BukkitRunnable() {
                @Override
                public void run() {
                    if (living.isDead()) {
                        player.sendMessage("§aBạn đã tiêu diệt boss §c" + boss.name());
                        for (ItemStack item : boss.loot()) {
                            player.getWorld().dropItemNaturally(player.getLocation(), item);
                        }
                        cancel();
                    }
                }
            }.runTaskTimer(plugin, 0L, 20L);
        }
    }

    private BiCanhBoss getRandomBoss(BiCanhType type) {
        List<BiCanhBoss> list = bossMap.get(type);
        return list.get(random.nextInt(list.size()));
    }

    private void loadBossData() {
        bossMap.put(BiCanhType.PHAM_NHAN, List.of(
                new BiCanhBoss("Phàm Nhân Tà Linh", EntityType.ZOMBIE, 80, List.of(new ItemStack(Material.IRON_SWORD)))
        ));
        bossMap.put(BiCanhType.TU_CHAN, List.of(
                new BiCanhBoss("Tu Chân Quỷ Vương", EntityType.SKELETON, 120, List.of(new ItemStack(Material.DIAMOND), new ItemStack(Material.GOLDEN_APPLE)))
        ));
        bossMap.put(BiCanhType.HOANG_GIA, List.of(
                new BiCanhBoss("Hoàng Kim Cự Nhân", EntityType.IRON_GOLEM, 250, List.of(new ItemStack(Material.GOLD_BLOCK)))
        ));
        bossMap.put(BiCanhType.THAN_TIEN, List.of(
                new BiCanhBoss("Thần Hỏa Kỳ Lân", EntityType.BLAZE, 300, List.of(new ItemStack(Material.NETHER_STAR)))
        ));
        bossMap.put(BiCanhType.DI_GIOI, List.of(
                new BiCanhBoss("Ma Ảnh Hư Không", EntityType.ENDER_DRAGON, 1000, List.of(new ItemStack(Material.DRAGON_EGG)))
        ));
    }
}
