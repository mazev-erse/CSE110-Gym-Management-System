package gym.ui;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * Pure Java2D reusable modern Swing UI widgets (zero external library dependencies).
 */
public final class ModernComponents {
    private ModernComponents() {
    }

    /**
     * Anti-aliased rounded card panel with subtle border shadow.
     */
    public static class RoundedPanel extends JPanel {
        private final int radius;
        private final Color fillColor;
        private final Color borderColor;

        public RoundedPanel(int radius, Color fillColor, Color borderColor) {
            this.radius = radius;
            this.fillColor = fillColor;
            this.borderColor = borderColor;
            setOpaque(false);
        }

        public RoundedPanel(int radius) {
            this(radius, Theme.CARD_BG, Theme.BORDER_LIGHT);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            // Soft drop shadow
            g2.setColor(new Color(15, 23, 42, 12));
            g2.fill(new RoundRectangle2D.Double(2, 3, w - 4, h - 4, radius, radius));

            // Card fill
            g2.setColor(fillColor);
            g2.fill(new RoundRectangle2D.Double(0, 0, w - 3, h - 3, radius, radius));

            // Subtle outline
            if (borderColor != null) {
                g2.setColor(borderColor);
                g2.setStroke(new BasicStroke(1f));
                g2.draw(new RoundRectangle2D.Double(0.5, 0.5, w - 4, h - 4, radius, radius));
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /**
     * Rounded button with smooth hover and press states.
     */
    public static class ModernButton extends JButton {
        private final Color baseColor;
        private final Color hoverColor;
        private final Color pressColor;
        private Color currentColor;
        private final int radius;

        public ModernButton(String text, Color baseColor) {
            this(text, baseColor, 10);
        }

        public ModernButton(String text, Color baseColor, int radius) {
            super(text);
            this.baseColor = baseColor;
            this.hoverColor = adjustBrightness(baseColor, 1.12f);
            this.pressColor = adjustBrightness(baseColor, 0.86f);
            this.currentColor = baseColor;
            this.radius = radius;

            setFont(Theme.FONT_BTN);
            setForeground(Color.WHITE);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(9, 15, 9, 15));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    currentColor = hoverColor;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    currentColor = baseColor;
                    repaint();
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    currentColor = pressColor;
                    repaint();
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    currentColor = getBounds().contains(e.getPoint()) ? hoverColor : baseColor;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(currentColor);
            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), radius, radius));
            g2.dispose();
            super.paintComponent(g);
        }

        private static Color adjustBrightness(Color c, float factor) {
            int r = Math.min(255, Math.max(0, Math.round(c.getRed() * factor)));
            int g = Math.min(255, Math.max(0, Math.round(c.getGreen() * factor)));
            int b = Math.min(255, Math.max(0, Math.round(c.getBlue() * factor)));
            return new Color(r, g, b);
        }
    }

    /**
     * Sidebar navigation item with custom Java2D vector icon, active pill background, and left accent bar.
     */
    public static class SidebarNavButton extends JButton {
        private final String iconKey;
        private boolean active = false;
        private boolean hovered = false;

        public SidebarNavButton(String iconKey, String label) {
            super(label);
            this.iconKey = iconKey;
            setFont(Theme.FONT_NAV);
            setForeground(Theme.TEXT_MUTED);
            setHorizontalAlignment(SwingConstants.LEFT);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(12, 46, 12, 16));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hovered = false;
                    repaint();
                }
            });
        }

        public void setActive(boolean active) {
            this.active = active;
            setForeground(active ? Color.WHITE : Theme.TEXT_MUTED);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();
            if (active) {
                g2.setColor(new Color(37, 99, 235, 55));
                g2.fill(new RoundRectangle2D.Double(10, 2, w - 20, h - 4, 10, 10));
                g2.setColor(Theme.PRIMARY);
                g2.fill(new RoundRectangle2D.Double(0, 8, 4, h - 16, 4, 4));
            } else if (hovered) {
                g2.setColor(Theme.SIDEBAR_HOVER);
                g2.fill(new RoundRectangle2D.Double(10, 2, w - 20, h - 4, 10, 10));
            }

            // Paint crisp custom Java2D vector icon
            g2.setColor(active ? new Color(96, 165, 250) : Theme.TEXT_MUTED);
            g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int ix = 21;
            int iy = (h - 14) / 2;

            switch (iconKey) {
                case "DASHBOARD":
                    g2.fillRoundRect(ix, iy, 6, 6, 2, 2);
                    g2.fillRoundRect(ix + 8, iy, 6, 6, 2, 2);
                    g2.fillRoundRect(ix, iy + 8, 6, 6, 2, 2);
                    g2.fillRoundRect(ix + 8, iy + 8, 6, 6, 2, 2);
                    break;
                case "MEMBERS":
                    g2.drawOval(ix + 3, iy, 7, 7);
                    g2.drawArc(ix, iy + 8, 13, 10, 0, 180);
                    break;
                case "TRAINERS":
                    g2.drawOval(ix + 3, iy, 7, 7);
                    g2.drawLine(ix + 6, iy + 9, ix + 6, iy + 14);
                    g2.drawLine(ix + 2, iy + 11, ix + 11, iy + 11);
                    break;
                case "WORKOUTS":
                    g2.fillRoundRect(ix, iy + 3, 3, 8, 1, 1);
                    g2.fillRoundRect(ix + 11, iy + 3, 3, 8, 1, 1);
                    g2.drawLine(ix + 3, iy + 7, ix + 11, iy + 7);
                    break;
                default: // OOP_LAB
                    g2.drawPolyline(new int[]{ix + 4, ix, ix + 4}, new int[]{iy + 2, iy + 7, iy + 12}, 3);
                    g2.drawPolyline(new int[]{ix + 10, ix + 14, ix + 10}, new int[]{iy + 2, iy + 7, iy + 12}, 3);
                    g2.drawLine(ix + 8, iy + 1, ix + 6, iy + 13);
                    break;
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /**
     * Padded text input field with rounded outline and focus-ring highlight.
     */
    public static class ModernTextField extends JTextField {
        private boolean focused = false;

        public ModernTextField() {
            this("");
        }

        public ModernTextField(String text) {
            super(text);
            setFont(Theme.FONT_BODY);
            setForeground(Theme.TEXT_PRIMARY);
            setBackground(Color.WHITE);
            setOpaque(false);
            setBorder(new EmptyBorder(7, 11, 7, 11));
            setPreferredSize(new Dimension(0, 34));

            addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    focused = true;
                    repaint();
                }

                @Override
                public void focusLost(FocusEvent e) {
                    focused = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.WHITE);
            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, 8, 8));
            g2.setColor(focused ? Theme.BORDER_FOCUS : Theme.BORDER_LIGHT);
            g2.setStroke(new BasicStroke(focused ? 1.8f : 1.0f));
            g2.draw(new RoundRectangle2D.Double(0.8, 0.8, getWidth() - 2.6, getHeight() - 2.6, 8, 8));
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /**
     * Brand header painted with a crisp custom Java2D dumbbell + heartbeat mark.
     */
    public static class BrandLogoPanel extends JPanel {
        public BrandLogoPanel() {
            setOpaque(false);
            setPreferredSize(new Dimension(220, 74));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 74));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            // Gradient icon tile
            g2.setColor(Theme.PRIMARY);
            g2.fill(new RoundRectangle2D.Double(18, 16, 40, 40, 12, 12));

            // Pulse line inside logo tile
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(2.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int[] xs = {24, 31, 35, 41, 45, 52};
            int[] ys = {37, 37, 26, 46, 33, 37};
            g2.drawPolyline(xs, ys, xs.length);

            // Brand Name & Subtitle
            g2.setFont(Theme.FONT_BRAND);
            g2.setColor(Color.WHITE);
            g2.drawString("FitPulse Pro", 68, 35);

            g2.setFont(Theme.FONT_SMALL);
            g2.setColor(Theme.TEXT_MUTED);
            g2.drawString("CSE110 Gym Manager", 68, 51);
            g2.dispose();
        }
    }

    /**
     * Executive KPI Card with subtle top color indicator, title, large value, and sub-caption.
     */
    public static RoundedPanel createKpiCard(String title, JLabel valueLabel, JLabel subLabel, Color accent) {
        RoundedPanel card = new RoundedPanel(14);
        card.setLayout(new BorderLayout(0, 6));
        card.setBorder(new EmptyBorder(14, 16, 14, 16));

        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);

        JLabel titleLbl = new JLabel(title.toUpperCase());
        titleLbl.setFont(Theme.FONT_KPI_LABEL);
        titleLbl.setForeground(Theme.TEXT_SECONDARY);

        JPanel dot = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 45));
                g2.fillOval(0, 0, 18, 18);
                g2.setColor(accent);
                g2.fillOval(5, 5, 8, 8);
                g2.dispose();
            }
        };
        dot.setOpaque(false);
        dot.setPreferredSize(new Dimension(18, 18));

        topRow.add(titleLbl, BorderLayout.WEST);
        topRow.add(dot, BorderLayout.EAST);

        valueLabel.setFont(Theme.FONT_KPI_VAL);
        valueLabel.setForeground(Theme.TEXT_PRIMARY);

        subLabel.setFont(Theme.FONT_SMALL);
        subLabel.setForeground(Theme.TEXT_SECONDARY);

        card.add(topRow, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        card.add(subLabel, BorderLayout.SOUTH);
        return card;
    }

    /**
     * Creates a clean zebra-striped JTable with subtle borders and generous row height.
     */
    public static JTable createModernTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setFont(Theme.FONT_BODY);
        table.setRowHeight(34);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(Theme.BORDER_LIGHT);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setSelectionBackground(Theme.TABLE_SELECT_BG);
        table.setSelectionForeground(Theme.TEXT_PRIMARY);
        table.setFillsViewportHeight(true);

        DefaultTableCellRenderer zebra = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object val, boolean selected,
                                                           boolean focus, int row, int col) {
                Component c = super.getTableCellRendererComponent(tbl, val, selected, focus, row, col);
                setBorder(new EmptyBorder(0, 7, 0, 7));
                if (!selected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : Theme.TABLE_ZEBRA);
                    c.setForeground(Theme.TEXT_PRIMARY);
                }
                return c;
            }
        };
        table.setDefaultRenderer(Object.class, zebra);

        JTableHeader header = table.getTableHeader();
        header.setFont(Theme.FONT_LABEL);
        header.setBackground(Theme.TABLE_HEADER_BG);
        header.setForeground(Theme.TEXT_SECONDARY);
        header.setPreferredSize(new Dimension(0, 36));
        header.setReorderingAllowed(false);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER_LIGHT));
        ((DefaultTableCellRenderer) header.getDefaultRenderer()).setHorizontalAlignment(SwingConstants.LEFT);
        ((DefaultTableCellRenderer) header.getDefaultRenderer()).setBorder(new EmptyBorder(0, 10, 0, 10));
        return table;
    }

    /**
     * Pill-shaped colored status badge cell renderer for Plans, Workout Categories, BMI, and Subclasses.
     */
    public static class BadgeCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            String text = value == null ? "" : value.toString();
            Color bgColor = isSelected ? Theme.TABLE_SELECT_BG : (row % 2 == 0 ? Color.WHITE : Theme.TABLE_ZEBRA);
            Color pillBg = new Color(226, 232, 240);
            Color pillFg = Theme.TEXT_PRIMARY;

            String upper = text.toUpperCase();
            if (upper.contains("PREMIUM") || upper.contains("TRAINER")) {
                pillBg = new Color(237, 233, 254);
                pillFg = new Color(109, 40, 217);
            } else if (upper.contains("STANDARD") || upper.contains("STRENGTH")) {
                pillBg = new Color(219, 234, 254);
                pillFg = new Color(29, 78, 216);
            } else if (upper.contains("BASIC")) {
                pillBg = new Color(241, 245, 249);
                pillFg = new Color(71, 85, 105);
            } else if (upper.contains("CARDIO")) {
                pillBg = new Color(255, 237, 213);
                pillFg = new Color(194, 65, 12);
            } else if (upper.contains("NORMAL") || upper.contains("MEMBER")) {
                pillBg = new Color(209, 250, 229);
                pillFg = new Color(4, 120, 87);
            } else if (upper.contains("OVERWEIGHT") || upper.contains("UNDERWEIGHT")) {
                pillBg = new Color(254, 243, 199);
                pillFg = new Color(180, 83, 9);
            } else if (upper.contains("OBESE")) {
                pillBg = new Color(255, 228, 230);
                pillFg = new Color(190, 18, 60);
            }

            final Color finalRowBg = bgColor;
            final Color finalPillBg = pillBg;
            final Color finalPillFg = pillFg;

            return new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                    g2.setColor(finalRowBg);
                    g2.fillRect(0, 0, getWidth(), getHeight());

                    Font badgeFont = new Font("Segoe UI", Font.BOLD, 11);
                    g2.setFont(badgeFont);
                    int textW = g2.getFontMetrics().stringWidth(text);
                    int pillW = Math.min(getWidth() - 12, textW + 18);
                    int pillH = 22;
                    int px = 10;
                    int py = (getHeight() - pillH) / 2;

                    g2.setColor(finalPillBg);
                    g2.fill(new RoundRectangle2D.Double(px, py, pillW, pillH, 12, 12));

                    g2.setColor(finalPillFg);
                    int textY = py + ((pillH - g2.getFontMetrics().getHeight()) / 2) + g2.getFontMetrics().getAscent();
                    g2.drawString(text, px + 9, textY);
                    g2.dispose();
                }
            };
        }
    }

    public static void styleComboBox(JComboBox<?> combo) {
        combo.setFont(Theme.FONT_BODY);
        combo.setBackground(Color.WHITE);
        combo.setForeground(Theme.TEXT_PRIMARY);
        combo.setPreferredSize(new Dimension(0, 34));
    }
}
