package dev.xianxia.modules;

import dev.xianxia.XianxiaPlugin;
import dev.xianxia.modules.Module;
import org.bukkit.*;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.*;

public class TheGioiModule implements Module, Listener {

    private XianxiaPlugin plugin;
    private YamlConfiguration config;
    private final Map<UUID, String> currentBosses = new HashMap<>();

    @Override
    public void enable(XianxiaPlugin plugin) {
        this.plugin = plugin;

        File file = new File(plugin.getDataFolder(), "config/config-the-gioi.yml");
        if (!file.exists()) plugin.saveResource("config/config-the-gioi.yml", false);
        config = YamlConfiguration.loadConfiguration(file);

        Bukkit.getPluginManager().registerEvents(this, plugin);
        plugin.getLogger().info("§a[TheGioi] Module Thế Giới đã được bật.");
    }

    @Override
    public void disable() {
        currentBosses.clear();
        plugin.getLogger().info("§c[TheGioi] Module Thế Giới đã được tắt.");
    }

    @Override
    public String getName() {
        return "The Gioi";
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        World newWorld = player.getWorld();
        String worldName = newWorld.getName();

        config.getConfigurationSection("bosses").getKeys(false).forEach(key -> {
            String targetWorld = config.getString("bosses." + key + ".world");
            if (worldName.equalsIgnoreCase(targetWorld)) {
                if (!currentBosses.containsValue(key)) {
                    Location loc = getSpawnLocation(config, key);
                    LivingEntity boss = spawnBoss(key, loc);
                    currentBosses.put(boss.getUniqueId(), key);
                    player.sendMessage("§c[Thế Giới] Một boss §e" + key + "§c đã xuất hiện ở chiều không gian này!");
                }
            }
        });
    }

    private Location getSpawnLocation(YamlConfiguration config, String key) {
        List<Integer> coords = config.getIntegerList("bosses." + key + ".spawn-location");
        String world = config.getString("bosses." + key + ".world");
        return new Location(Bukkit.getWorld(world), coords.get(0), coords.get(1), coords.get(2));
    }

    private LivingEntity spawnBoss(String key, Location location) {
        LivingEntity boss = location.getWorld().spawn(location, Zombie.class); // Bạn có thể thay bằng Wither, Enderman,...
        boss.setCustomName("§c" + key.toUpperCase());
        boss.setCustomNameVisible(true);
        boss.setMaxHealth(config.getDouble("bosses." + key + ".health"));
        boss.setHealth(config.getDouble("bosses." + key + ".health"));
        boss.setRemoveWhenFarAway(false);
        return boss;
    }

    @EventHandler
    public void onBossDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        UUID uuid = entity.getUniqueId();

        if (currentBosses.containsKey(uuid)) {
            String bossKey = currentBosses.remove(uuid);
            List<String> lootList = config.getStringList("bosses." + bossKey + ".loot");
            List<ItemStack> lootItems = parseLoot(lootList);
            event.getDrops().clear();
            event.getDrops().addAll(lootItems);

            if (entity.getKiller() != null) {
                Player killer = entity.getKiller();
                killer.sendMessage("§e[Thế Giới] Bạn đã tiêu diệt boss " + bossKey + " và nhận được phần thưởng!");
            }
        }
    }

    private List<ItemStack> parseLoot(List<String> rawList) {
        List<ItemStack> result = new ArrayList<>();
        for (String raw : rawList) {
            String[] parts = raw.split(":");
            Material mat = Material.getMaterial(parts[0]);
            int amount = Integer.parseInt(parts[1]);
            if (mat != null) {
                result.add(new ItemStack(mat, amount));
            }
        }
        return result;
    }
}
