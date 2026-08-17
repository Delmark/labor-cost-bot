package by.delmark.portal.labor_cost_bot.telegram.callbacks;

public final class DayLaborCostCallbacks {

    public static final String PREFIX = "lc-";

    public static final String INFO = PREFIX + "info";

    public static final String FILL = PREFIX + "fill";

    public static final String SET_PREFIX = PREFIX + "set:";

    public static final String NAV_PREV = PREFIX + "nav:prev";
    public static final String NAV_NEXT = PREFIX + "nav:next";
    public static final String EXIT = PREFIX + "nav:exit";

    public static final String NOOP = PREFIX + "noop";

    private DayLaborCostCallbacks() {
    }
}
