package gym.model;

/**
 * Enum representing available gym membership tiers and their base monthly fees (BDT).
 */
public enum MembershipPlan {
    BASIC("Basic", 1500.0),
    STANDARD("Standard", 2500.0),
    PREMIUM("Premium", 4000.0);

    private final String displayName;
    private final double monthlyFee;

    MembershipPlan(String displayName, double monthlyFee) {
        this.displayName = displayName;
        this.monthlyFee = monthlyFee;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getMonthlyFee() {
        return monthlyFee;
    }

    @Override
    public String toString() {
        return displayName + " (BDT " + String.format("%.0f", monthlyFee) + "/mo)";
    }
}
