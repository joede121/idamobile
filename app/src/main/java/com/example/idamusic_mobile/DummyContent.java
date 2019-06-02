package com.example.idamusic_mobile;

import android.util.Log;



import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class DummyContent {

    /**
     * An array of sample (dummy) items.
     */
    public static final List<DummyItem> ITEMS = new ArrayList<DummyItem>();
    public static final List<String> URIS = new ArrayList<>();
    public static Integer i;
    public static Player splayer;


    /**
     * A map of sample (dummy) items, by ID.
     */
    public static final Map<String, DummyItem> ITEM_MAP = new HashMap<String, DummyItem>();

    private static final int COUNT = 25;

    public static void refresh(Player player){
        URIS.clear();
        ITEMS.clear();
        i = new Integer(0);
        splayer = player;
        getPlayableItemsFromPlaylists("Ida_", player);
    }

    public static void refresh(){
        URIS.clear();
        ITEMS.clear();
        i = new Integer(0);
        getPlayableItemsFromPlaylists("Ida_", splayer);
    }

    static {

/*        URIS.add("3TNwLmVlMD5FH9lfreRTop");
        URIS.add("1eEKK5fA6KDqdvh5UVM1kw");
        URIS.add("3TNwLmVlMD5FH9lfreRTop");
        URIS.add("1eEKK5fA6KDqdvh5UVM1kw");
        URIS.add("3TNwLmVlMD5FH9lfreRTop");
        URIS.add("1eEKK5fA6KDqdvh5UVM1kw");
        URIS.add("1eEKK5fA6KDqdvh5UVM1kw");

        Integer i = 0;
        for (String uri: URIS){
            i++;
            PlayableItem pi = new PlayableItem(uri);
            addItem(new DummyItem(String.valueOf(i.toString()), pi.artist, pi.name, pi));
        }
        */
        //i = new Integer(0);
        //getPlayableItemsFromPlaylists("Ida_");
    }

    private static void addItem(DummyItem item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    private static String makeDetails(int position) {
        StringBuilder builder = new StringBuilder();
        builder.append("Details about Item: ").append(position);
        for (int i = 0; i < position; i++) {
            builder.append("\nMore details information here.");
        }
        return builder.toString();
    }

    private static void getPlayableItemsFromPlaylists(String prefix, Player player) {

        player.getPlayableItems(prefix, new PlayerListenerPlaylists() {
            @Override
            public void success(String uri) {
                PlayableItem pi = new PlayableItem(uri, player);
                i++;
                addItem(new DummyItem(String.valueOf(i.toString()), "pp", "tt", pi));
                Log.d("MainActivity", pi.artist + "Playable Item Playlist " + pi.name);

            }

            @Override
            public void error() {
                Log.d("MainActivity", "Error Fetch Playlist");
            }
        });
    }



      public static void sort() {
          Collections.sort(ITEMS, new Comparator<DummyItem>() {
              @Override
              public int compare(DummyItem first, DummyItem second) {
                  String one= new String (first.playable.artist + first.playable.name).toUpperCase();
                  String two= new String (second.playable.artist + second.playable.name).toUpperCase();
                  return one.compareTo(two);
              }
          });
          Integer y = new Integer(0);
          for(DummyItem d :ITEMS){
              y++;
              d.id = y.toString();
          }
      }



    /**
     * A dummy item representing a piece of content.
     */
    public static class DummyItem {
        public String id;
        public final String content;
        public final String details;
        public final PlayableItem playable;


        public DummyItem(String id, String content, String details, PlayableItem playable) {
            this.id = id;
            this.content = content;
            this.details = details;
            this.playable = playable;
        }

        public DummyItem(String id, String content, String details) {
            this.id = id;
            this.content = content;
            this.details = details;
            playable = null;
        }

        @Override
        public String toString() {
            return content;
        }
    }
}
