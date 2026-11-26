package gym.ui;

import gym.model.MembershipPlan;
import gym.service.GymService;

import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Arc2D;
import java.awt.geom.RoundRectangle2D;

/**
 * Custom Java2D anti-aliased analytics charts for the Executive Dashboard.
 * Provides live Financial Comparison Bar Chart and Membership Distribution Donut Chart.
 */
public final class AnalyticsChartPanel {
    private AnalyticsChartPanel() {
    }

    /**
     * Live Financial Bar Chart comparing Monthly Revenue, Trainer Payroll, and Plan Breakdown.
     */
    public static class FinancialBarChart extends JPanel {
        private final GymService gymService;

        public FinancialBarChart(GymService gymService) {
            this.gymService = gymService;
            setOpaque(false);
            setPreferredSize(new Dimension(420, 220));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            double revenue = gymService.calculateTotalMonthlyRevenue();
            double payroll = gymService.calculateTotalTrainerPayroll();
            double volumeScaled = gymService.getTotalVolumeAcrossGym();

            String[] labels = {"Membership Revenue", "Trainer Payroll", "Lifted Volume (kg)"};
            double[] values = {revenue, payroll, volumeScaled};
            String[] displayVals = {
                    String.format("BDT %,.0f", revenue),
                    String.format("BDT %,.0f", payroll),
                    String.format("%,.0f kg", volumeScaled)
            };
            Color[] colors = {Theme.EMERALD, Theme.ROSE, Theme.PRIMARY};

            int w = getWidth();
            int h = getHeight();
            int leftPad = 24;
            int rightPad = 24;
            int topPad = 28;
            int bottomPad = 38;

            int chartW = Math.max(100, w - leftPad - rightPad);
            int chartH = Math.max(80, h - topPad - bottomPad);

            // Grid lines
            g2.setColor(Theme.BORDER_LIGHT);
            g2.setStroke(new BasicStroke(1f));
            for (int i = 0; i <= 3; i++) {
                int y = topPad + (chartH * i) / 3;
                g2.drawLine(leftPad, y, w - rightPad, y);
            }

            double maxVal = 1.0;
            for (double v : values) {
                if (v > maxVal) maxVal = v;
            }

            int slotW = chartW / labels.length;
            int barW = Math.min(72, Math.max(36, slotW - 44));

            for (int i = 0; i < labels.length; i++) {
                int barH = (int) Math.round((values[i] / maxVal) * (chartH - 12));
                barH = Math.max(barH, 6);
                int x = leftPad + (slotW * i) + (slotW - barW) / 2;
                int y = topPad + chartH - barH;

                // Soft bar backdrop
                g2.setColor(new Color(colors[i].getRed(), colors[i].getGreen(), colors[i].getBlue(), 26));
                g2.fill(new RoundRectangle2D.Double(x, topPad, barW, chartH, 10, 10));

                // Actual bar
                g2.setColor(colors[i]);
                g2.fill(new RoundRectangle2D.Double(x, y, barW, barH, 10, 10));

                // Value label above bar
                g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
                g2.setColor(Theme.TEXT_PRIMARY);
                int valW = g2.getFontMetrics().stringWidth(displayVals[i]);
                g2.drawString(displayVals[i], x + (barW - valW) / 2, Math.max(16, y - 7));

                // Category label below axis
                g2.setFont(Theme.FONT_SMALL);
                g2.setColor(Theme.TEXT_SECONDARY);
                int lblW = g2.getFontMetrics().stringWidth(labels[i]);
                g2.drawString(labels[i], x + (barW - lblW) / 2, topPad + chartH + 22);
            }
            g2.dispose();
        }
    }

    /**
     * Live Membership Tier Distribution Donut Chart with dynamic legend.
     */
    public static class PlanDonutChart extends JPanel {
        private final GymService gymService;

        public PlanDonutChart(GymService gymService) {
            this.gymService = gymService;
            setOpaque(false);
            setPreferredSize(new Dimension(340, 220));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int basic = gymService.getPlanCount(MembershipPlan.BASIC);
            int standard = gymService.getPlanCount(MembershipPlan.STANDARD);
            int premium = gymService.getPlanCount(MembershipPlan.PREMIUM);
            int total = basic + standard + premium;

            int[] counts = {premium, standard, basic};
            String[] names = {"Premium", "Standard", "Basic"};
            Color[] colors = {Theme.VIOLET, Theme.PRIMARY, Theme.AMBER};

            int h = getHeight();
            int diameter = Math.min(155, Math.max(110, h - 44));
            int cx = 24;
            int cy = (h - diameter) / 2;

            if (total == 0) {
                g2.setColor(Theme.BORDER_LIGHT);
                g2.setStroke(new BasicStroke(20f));
                g2.drawOval(cx + 10, cy + 10, diameter - 20, diameter - 20);
            } else {
                double startAngle = 90.0;
                for (int i = 0; i < counts.length; i++) {
                    if (counts[i] == 0) continue;
                    double sweep = -360.0 * counts[i] / total;
                    g2.setColor(colors[i]);
                    g2.fill(new Arc2D.Double(cx, cy, diameter, diameter, startAngle, sweep, Arc2D.PIE));
                    startAngle += sweep;
                }
                // Inner cutout for donut ring
                int inner = (int) (diameter * 0.58);
                int offset = (diameter - inner) / 2;
                g2.setColor(Theme.CARD_BG);
                g2.fillOval(cx + offset, cy + offset, inner, inner);
            }

            // Center total label
            g2.setFont(new Font("Segoe UI", Font.BOLD, 22));
            g2.setColor(Theme.TEXT_PRIMARY);
            String totalStr = String.valueOf(total);
            int tw = g2.getFontMetrics().stringWidth(totalStr);
            g2.drawString(totalStr, cx + (diameter - tw) / 2, cy + diameter / 2 + 4);

            g2.setFont(Theme.FONT_SMALL);
            g2.setColor(Theme.TEXT_SECONDARY);
            String sub = "Members";
            int sw = g2.getFontMetrics().stringWidth(sub);
            g2.drawString(sub, cx + (diameter - sw) / 2, cy + diameter / 2 + 19);

            // Right-side Legend
            int lx = cx + diameter + 28;
            int ly = cy + 28;
            for (int i = 0; i < names.length; i++) {
                int pct = total == 0 ? 0 : (int) Math.round(100.0 * counts[i] / total);
                g2.setColor(colors[i]);
                g2.fill(new RoundRectangle2D.Double(lx, ly - 10, 12, 12, 4, 4));

                g2.setFont(Theme.FONT_LABEL);
                g2.setColor(Theme.TEXT_PRIMARY);
                g2.drawString(names[i], lx + 20, ly);

                g2.setFont(Theme.FONT_SMALL);
                g2.setColor(Theme.TEXT_SECONDARY);
                g2.drawString(counts[i] + " members (" + pct + "%)", lx + 20, ly + 15);
                ly += 42;
            }
            g2.dispose();
        }
    }
}
