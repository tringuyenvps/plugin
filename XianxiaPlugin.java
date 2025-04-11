package dev.xianxia;

import dev.xianxia.modules.Module;
import dev.xianxia.modules.*;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class XianxiaPlugin extends JavaPlugin {

    private final List<Module> modules = new ArrayList<>();

    @Override
    public void onEnable() {
        getLogger().info("§a[Xianxia] Đang khởi động plugin tu tiên...");

        // Khởi tạo và bật các module
        registerModule(new TuLuyenModule());
        registerModule(new CanhGioiModule());
        registerModule(new TienKiepModule());
        registerModule(new PhapBaoModule());
        registerModule(new BiCanhSuKienModule());
        registerModule(new LuyenDanModule());
        registerModule(new LinhMachModule());
        registerModule(new PhuTranModule());
        registerModule(new ThanThucModule());
        registerModule(new TongMonModule());
        registerModule(new NhiemVuModule());
        registerModule(new TheGioiModule());
        registerModule(new NguKiemModule());
        registerModule(new LinhCanModule()); // Linh căn hỗ trợ tu luyện

        getLogger().info("§a[Xianxia] Tất cả module đã được bật thành công!");
    }

    @Override
    public void onDisable() {
        for (Module module : modules) {
            module.disable();
        }
        getLogger().info("§c[Xianxia] Plugin tu tiên đã được tắt.");
    }

    private void registerModule(Module module) {
        module.enable(this);
        modules.add(module);
        getLogger().info("§7- Đã bật module: §e" + module.getName());
    }
}
