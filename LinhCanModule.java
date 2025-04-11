package dev.xianxia.modules;

import dev.xianxia.XianxiaPlugin;
import dev.xianxia.modules.Module;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.util.*;

public class LinhCanModule implements Module, Listener {

    private XianxiaPlugin plugin;
    private YamlConfiguration config;
    private final Random random = new Random();

    public enum LinhCanType {
        KIM(2), MOC(2), THUY(2), HOA(2), THO(2),
        PHONG(3), LUOI(3), AM(3), DUONG(3),
        THIEN(4), MA(4), HAN_BANG(4), LUA_DO(4),
        HOANG_KIM(5), LINH_QUANG(5), HUYEN_AM(5), CUONG_HOA(5),
        TU_LUYEN_CAO(6), VO_CAN(0);

        private final int powerLevel;
        LinhCanType(int powerLevel) { this.powerLevel = powerLevel; }
        public int getPowerLevel() { return powerLevel; }
    }

    private final Map<UUID, LinhCanType> playerLinhCan = new HashMap<>();
    private final Map<UUID, Integer> linhCanLevel = new HashMap<>();
    private final Set<UUID> selectedPlayers = new HashSet<>();

    @Override
    public void enable(XianxiaPlugin plugin) {
        this.plugin = plugin;

        File file = new File(plugin.getDataFolder(), "config/config-linh-can.yml");
        if (!file.exists()) plugin.saveResource("config/config-linh-can.yml", false);
        config = YamlConfiguration.loadConfiguration(file);

        Bukkit.getPluginManager().registerEvents(this, plugin);
        plugin.getLogger().info("§a[LinhCan] Module linh căn đã được bật.");
    }

    @Override
    public void disable() {
        playerLinhCan.clear();
        plugin.getLogger().info("§c[LinhCan] Module linh căn đã được tắt.");
    }

    @Override
    public String getName() {
        return "Linh Căn";
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (!playerLinhCan.containsKey(uuid) && !selectedPlayers.contains(uuid)) {
            selectedPlayers.add(uuid);
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (!playerLinhCan.containsKey(uuid)) {
                    LinhCanType randomType = getRandomLinhCan();
                    playerLinhCan.put(uuid, randomType);
                    linhCanLevel.put(uuid, 1);
                    player.sendMessage("§d[LinhCăn] Linh căn của bạn là: §e" + formatLinhCanName(randomType));
                    applyBuff(player, randomType, 1);
                }
            }, 20L * 3);
        } else {
            LinhCanType type = playerLinhCan.get(uuid);
            applyBuff(player, type, getLinhCanLevel(uuid));
            player.sendMessage("§d[LinhCăn] Linh căn của bạn là: §e" + formatLinhCanName(type));
        }
    }

    private LinhCanType getRandomLinhCan() {
        List<LinhCanType> types = new ArrayList<>(Arrays.asList(LinhCanType.values()));
        types.remove(LinhCanType.VO_CAN);
        types.sort(Comparator.comparingInt(LinhCanType::getPowerLevel));

        int totalWeight = types.stream().mapToInt(type -> 10 - type.getPowerLevel()).sum();
        int roll = random.nextInt(totalWeight);

        int cumulative = 0;
        for (LinhCanType type : types) {
            cumulative += 10 - type.getPowerLevel();
            if (roll < cumulative) return type;
        }
        return LinhCanType.KIM; // fallback
    }

    private int getLinhCanLevel(UUID uuid) {
        return linhCanLevel.getOrDefault(uuid, 1);
    }

    private void applyBuff(Player player, LinhCanType type, int level) {
        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }

        int duration = Integer.MAX_VALUE;
        int amp = Math.max(0, level - 1);

        switch (type) {
            case KIM -> player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, duration, amp));
            case MOC -> player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, duration, amp));
            case THUY -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, duration, 0));
                player.addPotionEffect(new PotionEffect(PotionEffectType.DOLPHINS_GRACE, duration, 0));
            }
            case HOA, LUA_DO -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, duration, amp));
                player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH
                        , duration, amp));
            }
            case THO -> player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, duration, amp));
            case PHONG -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, duration, amp));
                player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, duration, amp));
            }
            case LUOI -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, duration, amp));
                player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 20 * 10, amp));
            }
            case AM -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, duration, 0));
                player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, duration, 0));
            }
            case DUONG -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, duration, amp));
                player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, duration, amp));
            }
            case THIEN -> player.addPotionEffect(new PotionEffect(PotionEffectType.LUCK, duration, amp));
            case MA -> player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, duration, amp));
            case HAN_BANG -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, duration, 0));
                player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, duration, amp));
            }
            case HOANG_KIM -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, duration, amp));
                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, duration, amp));
                player.addPotionEffect(new PotionEffect(PotionEffectType.HEALTH_BOOST, duration, amp));
            }
            case LINH_QUANG -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, duration, 0));
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, duration, amp));
            }
            case HUYEN_AM -> player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, duration, 0));
            case CUONG_HOA -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.HEALTH_BOOST, duration, amp));
                player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, duration, amp));
            }
            case TU_LUYEN_CAO -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, duration, 0));
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, duration, amp));
            }
        }
    }

    private String formatLinhCanName(LinhCanType type) {
        return type.name().replace("_", " ").toUpperCase();
    }
}