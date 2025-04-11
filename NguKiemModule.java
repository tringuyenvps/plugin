package dev.xianxia.modules;

import dev.xianxia.XianxiaPlugin;
import dev.xianxia.modules.Module;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.CommandExecutor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class NguKiemModule implements Module, Listener, CommandExecutor {

    private XianxiaPlugin plugin;
    private YamlConfiguration config;
    private final Set<UUID> flyingPlayers = new HashSet<>();

    @Override
    public void enable(XianxiaPlugin plugin) {
        this.plugin = plugin;

        File file = new File(plugin.getDataFolder(), "config/config-ngu-kiem.yml");
        if (!file.exists()) plugin.saveResource("config/config-ngu-kiem.yml", false);
        config = YamlConfiguration.loadConfiguration(file);

        Bukkit.getPluginCommand("ngukiem").setExecutor(this);
        Bukkit.getPluginManager().registerEvents(this, plugin);
        plugin.getLogger().info("§a[NguKiem] Module ngự kiếm phi hành đã được bật.");
    }

    @Override
    public void disable() {
        flyingPlayers.clear();
        plugin.getLogger().info("§c[NguKiem] Module ngự kiếm phi hành đã được tắt.");
    }

    @Override
    public String getName() {
        return "Ngự Kiếm";
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) return false;

        UUID uuid = player.getUniqueId();

        if (flyingPlayers.contains(uuid)) {
            flyingPlayers.remove(uuid);
            player.setAllowFlight(false);
            player.setFlying(false);
            player.sendMessage("§c[NgựKiếm] Bạn đã hạ kiếm.");
        } else {
            if (!player.getInventory().contains(Material.DIAMOND_SWORD)) {
                player.sendMessage("§c[NgựKiếm] Bạn cần một kiếm để ngự!");
                return true;
            }

            flyingPlayers.add(uuid);
            player.setAllowFlight(true);
            player.setFlying(true);
            player.sendMessage("§a[NgựKiếm] Bạn đã ngự kiếm phi hành!");
            player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 1.0f, 1.0f);
        }

        return true;
    }
}
