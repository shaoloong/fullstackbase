package com.example.base.core;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 功能注册表（扩展入口 · 后端侧）
 * ============================================================
 * 这是「新增功能」在后端唯一的登记点，通过 GET /api/features 暴露给前端。
 * 免构建预览页与 Vite 前端的菜单都从这里取，避免两个前端各维护一份。
 *
 * ⚠️ 与前端 src/config/features.js 的 key 必须一一对应；
 *    scripts/check-features.js 会做一致性校验（key 集合不等即报错）。
 *
 * scripts/new-module.js 会在 @NEW_FEATURE 锚点前自动插入一行，勿删锚点。
 *
 * 为什么用 Java 常量而不是数据库表：
 *   功能是否「上线」属于代码级决策，跟代码一起 review、一起回滚最稳；
 *   等真需要运行期开关时，再下沉到配置中心/参数表，不要一上来就做动态化。
 */
@Component
public class FeatureRegistry {

    /**
     * 单条功能定义。
     *
     * @param key   全局唯一标识，必须与前端 features.js 的 key 一致（小写域名，如 user / order）
     * @param title 菜单显示名
     * @param path  前端路由路径
     * @param api   该功能的后端接口前缀（仅用于展示与自检）
     * @param icon  菜单图标（emoji，零依赖）
     * @param ready 页面是否已实现：false 时前端显示「已注册，待实现」占位卡片，
     *              这正是「先占位、后填功能」的扩展方式
     */
    public record Feature(String key, String title, String path, String api, String icon, boolean ready) {
    }

    // 用静态块逐行 add：生成器只需在锚点前插一行，不会出现尾随逗号的语法坑
    private static final List<Feature> FEATURES = new ArrayList<>();

    static {
        FEATURES.add(new Feature("user", "用户管理", "/users", "/api/users", "👤", true));
        FEATURES.add(new Feature("product", "产品管理", "/products", "/api/products", "🧩", true));
        // @NEW_FEATURE
    }

    /** 返回不可变快照：调用方拿不到内部引用，改不了注册表内容 */
    public List<Feature> list() {
        return List.copyOf(FEATURES);
    }
}
