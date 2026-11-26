package gym.ui;

import java.awt.Color;
import java.awt.Font;

/**
 * Centralized visual design tokens (colors and typography) for the Swing UI.
 */
public final class Theme {
    private Theme() {
    }

    // Core Surfaces
    public static final Color SIDEBAR_BG = new Color(15, 23, 42);       // Deep Obsidian Slate
    public static final Color SIDEBAR_HOVER = new Color(30, 41, 59);
    public static final Color SIDEBAR_ACTIVE = new Color(37, 99, 235);  // Electric Royal Blue
    public static final Color CANVAS_BG = new Color(241, 245, 249);     // Crisp Cool Gray
    public static final Color CARD_BG = Color.WHITE;
    public static final Color BORDER_LIGHT = new Color(226, 232, 240);
    public static final Color BORDER_FOCUS = new Color(59, 130, 246);

    // Typography Colors
    public static final Color TEXT_PRIMARY = new Color(15, 23, 42);
    public static final Color TEXT_SECONDARY = new Color(100, 116, 139);
    public static final Color TEXT_MUTED = new Color(148, 163, 184);

    // Brand & Semantic Accents
    public static final Color PRIMARY = new Color(37, 99, 235);
    public static final Color INDIGO = new Color(79, 70, 229);
    public static final Color EMERALD = new Color(16, 185, 129);
    public static final Color AMBER = new Color(245, 158, 11);
    public static final Color ROSE = new Color(225, 29, 72);
    public static final Color VIOLET = new Color(124, 58, 237);
    public static final Color SLATE_BTN = new Color(71, 85, 105);

    // Table Zebra / Selection
    public static final Color TABLE_HEADER_BG = new Color(248, 250, 252);
    public static final Color TABLE_ZEBRA = new Color(248, 250, 252);
    public static final Color TABLE_SELECT_BG = new Color(219, 234, 254);

    // Fonts
    private static final String FONT_FAMILY = "Segoe UI";
    public static final Font FONT_BRAND = new Font(FONT_FAMILY, Font.BOLD, 18);
    public static final Font FONT_PAGE_TITLE = new Font(FONT_FAMILY, Font.BOLD, 21);
    public static final Font FONT_SECTION = new Font(FONT_FAMILY, Font.BOLD, 14);
    public static final Font FONT_KPI_VAL = new Font(FONT_FAMILY, Font.BOLD, 24);
    public static final Font FONT_KPI_LABEL = new Font(FONT_FAMILY, Font.BOLD, 11);
    public static final Font FONT_LABEL = new Font(FONT_FAMILY, Font.BOLD, 12);
    public static final Font FONT_BODY = new Font(FONT_FAMILY, Font.PLAIN, 13);
    public static final Font FONT_SMALL = new Font(FONT_FAMILY, Font.PLAIN, 11);
    public static final Font FONT_NAV = new Font(FONT_FAMILY, Font.BOLD, 13);
    public static final Font FONT_BTN = new Font(FONT_FAMILY, Font.BOLD, 12);
    public static final Font FONT_MONO = new Font("Consolas", Font.PLAIN, 12);
}
