package dev.xianxia.modules;

import dev.xianxia.XianxiaPlugin;
import dev.xianxia.modules.Module;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class TienKiepModule implements Module, Listener {

    private XianxiaPlugin plugin;
    private final Map<UUID, Integer> playerTuVi = new HashMap<>();
    private final Map<UUID, Integer> playerCanhGioi = new HashMap<>();

    @Override
    public void enable(XianxiaPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        plugin.getLogger().info("§b[ThienKiep] Module thiên kiếp đột phá đã bật.");
    }

    @Override
    public void disable() {
        plugin.getLogger().info("§c[ThienKiep] Module thiên kiếp đột phá đã tắt.");
    }

    @Override
    public String getName() {
        return "ThienKiepDotPha";
    }

    public int getTuVi(Player player) {
        return playerTuVi.getOrDefault(player.getUniqueId(), 0);
    }

    public void setTuVi(Player player, int amount) {
        playerTuVi.put(player.getUniqueId(), amount);
    }

    public void tryDotPha(Player player) {
        int currentTuVi = getTuVi(player);
        int currentCanhGioi = playerCanhGioi.getOrDefault(player.getUniqueId(), 0);

        if (currentTuVi >= getRequiredTuVi(currentCanhGioi)) {
            startThienKiep(player, currentCanhGioi);
        } else {
            player.sendMessage("§c[Đột Phá] Tu vi của bạn chưa đủ để đột phá!");
        }
    }

    private int getRequiredTuVi(int canhGioi) {
        return (canhGioi + 1) * 1000;
    }

    private void startThienKiep(Player player, int canhGioi) {
        player.sendMessage("§6[Thiên Kiếp] Bạn đang tiến hành đột phá cảnh giới " + (canhGioi + 1) + "!");
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 20 * 10, 2));
        player.getWorld().strikeLightningEffect(player.getLocation());

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            boolean success = new Random().nextDouble() < 0.85;
            if (success) {
                player.sendMessage("§a[Đột Phá] Thành công! Bạn đã lên cảnh giới mới!");
                playerCanhGioi.put(player.getUniqueId(), canhGioi + 1);
                buffCanhGioi(player, canhGioi + 1);
            } else {
                player.sendMessage("§c[Đột Phá] Thất bại! Bạn cần tích lũy tu vi thêm.");
            }
        }, 20 * 5);
    }

    private void buffCanhGioi(Player player, int canhGioi) {
        int healthBonus = 2 * canhGioi;
        int strengthLevel = Math.min(5, canhGioi);
        int speedLevel = canhGioi >= 3 ? 1 : 0;

        player.addPotionEffect(new PotionEffect(PotionEffectType.HEALTH_BOOST, Integer.MAX_VALUE, healthBonus));
        player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, Integer.MAX_VALUE, strengthLevel));
        if (speedLevel > 0) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, speedLevel));
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!playerTuVi.containsKey(player.getUniqueId())) {
            setTuVi(player, 0);
        }
    }
}
