package it.digisin.collabroute.travel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 *
 */
public class TravelContent {

    /**
     * An array of sample (Travel) items.
     */
    public static List<TravelItem> ITEMS = new ArrayList<TravelItem>();

    /**
     * A map of sample (Travel) items, by ID.
     */
    public static Map<String, TravelItem> ITEM_MAP = new HashMap<String, TravelItem>();


    public static void addItem(TravelItem item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    /**
     * A Travel item representing a piece of content.
     */
    public static class TravelItem {
        public String id;
        public String content;

        public TravelItem(String id, String content) {
            this.id = id;
            this.content = content;
        }

        @Override
        public String toString() {
            return content;
        }
    }
}
