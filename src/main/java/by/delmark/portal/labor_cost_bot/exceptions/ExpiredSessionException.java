package by.delmark.portal.labor_cost_bot.exceptions;

public class ExpiredSessionException extends RuntimeException {
    public ExpiredSessionException(String message) {
        super(message);
    }
}
