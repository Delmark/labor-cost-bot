package by.delmark.portal.labor_cost_bot.telegram.callbacks;

public final class DayLaborCostCallbacks {

    public static final String PREFIX = "lc";
    public static final String SEPARATED_PREFIX = PREFIX + "-";

    public static final String INFO = SEPARATED_PREFIX + "info";

    public static final String FILL = SEPARATED_PREFIX + "fill";

    public static final String SET_PREFIX = SEPARATED_PREFIX + "set:";

    public static final String NAV_PREV = SEPARATED_PREFIX + "nav:prev";
    public static final String NAV_NEXT = SEPARATED_PREFIX + "nav:next";
    public static final String EXIT = SEPARATED_PREFIX + "nav:exit";

    public static final String NOOP = SEPARATED_PREFIX + "noop";

    private DayLaborCostCallbacks() {
    }
}
