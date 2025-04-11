package dev.xianxia.modules;

import dev.xianxia.XianxiaPlugin;

public interface Module {

    /**
     * Gọi khi plugin được bật để khởi tạo module
     */
    void enable(XianxiaPlugin plugin);

    /**
     * Gọi khi plugin bị tắt để giải phóng tài nguyên
     */
    void disable();

    /**
     * Tên hiển thị của module (dùng để debug/log)
     */
    String getName();
}
