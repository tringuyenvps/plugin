package dev.xianxia.modules;

import dev.xianxia.XianxiaPlugin;
import dev.xianxia.modules.*;

import java.util.ArrayList;
import java.util.List;

public class ModuleManager {

    private final List<Module> modules = new ArrayList<>();

    public void registerModules(XianxiaPlugin plugin) {
        modules.add(new TuLuyenModule());
        modules.add(new CanhGioiModule());
        modules.add(new TienKiepModule());
        modules.add(new PhapBaoModule());
        modules.add(new BiCanhSuKienModule());
        modules.add(new LuyenDanModule());
        modules.add(new LinhMachModule());
        modules.add(new PhuTranModule());
        modules.add(new ThanThucModule());
        modules.add(new TongMonModule());
        modules.add(new NhiemVuModule());
        modules.add(new TheGioiModule());
        modules.add(new NguKiemModule());
        modules.add(new LinhCanModule());

        for (Module module : modules) {
            module.enable(plugin);
            plugin.getLogger().info("§a[ModuleManager] Đã kích hoạt module: " + module.getName());
        }
    }

    public void disableModules() {
        for (Module module : modules) {
            module.disable();
        }
        modules.clear();
    }
}
