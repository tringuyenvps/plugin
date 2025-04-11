package dev.xianxia.modules;

import dev.xianxia.XianxiaPlugin;
import dev.xianxia.modules.Module;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class LinhMachModule implements Module, Listener {

    private XianxiaPlugin plugin;
    private final Map<UUID, LinhMachType> playerLinhMach = new HashMap<>();
    private final Map<UUID, Integer> linhMachLevel = new HashMap<>();

    public enum LinhMachType {
        PHAM_PHAM(1), DIA_PHAM(2), THIEN_PHAM(3), THAN_PHAM(4), TIEN_PHAM(5);

        private final int tier;
        LinhMachType(int tier) { this.tier = tier; }
        public int getTier() { return tier; }
    }

    @Override
    public void enable(XianxiaPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        plugin.getLogger().info("§a[LinhMach] Module linh mạch đã được bật.");
    }

    @Override
    public void disable() {
        playerLinhMach.clear();
        linhMachLevel.clear();
        plugin.getLogger().info("§c[LinhMach] Module linh mạch đã được tắt.");
    }

    @Override
    public String getName() {
        return "Linh Mạch";
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (!playerLinhMach.containsKey(uuid)) {
            LinhMachType randomType = getRandomLinhMach();
            playerLinhMach.put(uuid, randomType);
            linhMachLevel.put(uuid, 1);
            player.sendMessage("§d[LinhMạch] Bạn nhận được linh mạch: §e" + formatLinhMachName(randomType));
        }
    }

    public void openLinhMachGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, "§dLinh Mạch của bạn");

        LinhMachType type = playerLinhMach.getOrDefault(player.getUniqueId(), LinhMachType.PHAM_PHAM);
        int level = linhMachLevel.getOrDefault(player.getUniqueId(), 1);

        ItemStack info = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = info.getItemMeta();
        meta.setDisplayName("§eLinh Mạch: §f" + formatLinhMachName(type));
        meta.setLore(Arrays.asList(
                "§7Phẩm chất: §6" + type.getTier(),
                "§7Cấp độ: §a" + level,
                "§7Tăng cường tu luyện và hỗ trợ đột phá."
        ));
        info.setItemMeta(meta);

        gui.setItem(13, info);
        player.openInventory(gui);
    }

    private LinhMachType getRandomLinhMach() {
        List<LinhMachType> list = new ArrayList<>(Arrays.asList(LinhMachType.values()));
        list.sort(Comparator.comparingInt(LinhMachType::getTier));
        int roll = new Random().nextInt(100);
        if (roll < 40) return LinhMachType.PHAM_PHAM;
        else if (roll < 70) return LinhMachType.DIA_PHAM;
        else if (roll < 90) return LinhMachType.THIEN_PHAM;
        else if (roll < 98) return LinhMachType.THAN_PHAM;
        return LinhMachType.TIEN_PHAM;
    }

    private String formatLinhMachName(LinhMachType type) {
        return type.name().replace("_", " ").toUpperCase();
    }
}
