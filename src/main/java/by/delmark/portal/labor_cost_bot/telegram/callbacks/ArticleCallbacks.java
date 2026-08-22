package by.delmark.portal.labor_cost_bot.telegram.callbacks;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ArticleCallbacks {
    public final String PREFIX = "ac";
    public static final String SEPARATED_PREFIX = PREFIX + "-";

    // prefix-feed:page
    public final String ARTICLE_FEED = SEPARATED_PREFIX + "article_feed:";

    // refix-feed:article-internal-id
    public final String FULL_ARTICLE = SEPARATED_PREFIX + "article:";

    public final String EXIT_FEED = SEPARATED_PREFIX + "exit";
}
