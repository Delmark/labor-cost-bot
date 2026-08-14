package by.delmark.portal.labor_cost_bot.telegram.callbacks;

public final class DayLaborCostCallbacks {

    public static final String LC_CALLBACK_PREFIX = "lc-";

    public static final String INFO = LC_CALLBACK_PREFIX + "info";

    public static final String FILL = LC_CALLBACK_PREFIX + "fill";

    public static final String SET_PREFIX = LC_CALLBACK_PREFIX + "set:";

    public static final String NAV_PREV = LC_CALLBACK_PREFIX + "nav:prev";
    public static final String NAV_NEXT = LC_CALLBACK_PREFIX + "nav:next";
    public static final String EXIT = LC_CALLBACK_PREFIX + "nav:exit";

    public static final String NOOP = LC_CALLBACK_PREFIX + "noop";

    private DayLaborCostCallbacks() {
    }
}
