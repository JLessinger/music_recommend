package recommender;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import kdtree.KDNode;
import kdtree.KDTree;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jonathan on 5/23/15.
 */

public class NearestSongsMain {
  public static final String USG_MSG = "usage: load_recommender.sh path_to_song_db";
  private static int NUM_REC;
  private static int NUM_META_FIELDS;
  private static int NUM_FEATURES;

  static {
    try {
      String jsonString = readFile("../global_config.json", Charsets.US_ASCII);
      JSONObject jsonObj = new JSONObject(jsonString);
      NUM_REC = jsonObj.getInt("NUM_RECOMMENDATIONS");
      NUM_META_FIELDS = jsonObj.getInt("NUM_META_FIELDS");
      NUM_FEATURES = (1 + jsonObj.getInt("POLY_ORDER")) * 12;
    } catch (JSONException | IOException e) {
      System.err.println("warning: error reading global config. Falling back to defaults");
      System.err.println("cause: " + e);
      NUM_REC = 5;
      NUM_META_FIELDS = 4;
      NUM_FEATURES = 36;
    }
  }


  private static String readFile(String path, Charset encoding) throws IOException {
    byte[] encoded = Files.readAllBytes(Paths.get(path));
    return new String(encoded, encoding);
  }

  private static void quit(int status){
    if(status!=0) {
      System.err.println(USG_MSG);
    }
    System.exit(0);
  }
  private static void quit(int status, String message){
    if(status==0) {
      System.out.println(message);
    } else {
      System.err.println(message);
    }
    System.exit(status);
  }


  public static void main(String[] args) {
    if (args.length != 1) {
      quit(-1);
    } else{
      try {
        // load songs
        List<Song> songList = new ArrayList<>();
        int songDim = populateSongList(args[0], songList, NUM_FEATURES);
        Map<String, Song> songsByID = getIDTable(songList);

        KDTree<Song, Double> songTree = new KDNode<>(songDim, songList);

        System.out.println("Successfully loaded songs. Entering recommend loop.");
        runQueryLoop(songsByID, songTree);
      } catch (NumberFormatException n) {
        quit(-1);
      } catch(IOException io){
        quit(-1, io.getMessage());
      }
    }
  }

  private static void runQueryLoop(Map<String, Song> songsByID, KDTree<Song, Double> songTree) throws IOException{
    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    String input;
    while ((input = br.readLine()) != null) {
      Song search = songsByID.get(input);
      if(search==null) {
        System.out.println("song not found");
      } else {
        System.out.print("Searching for ");
        printSongWithSection(search);
        // we have to get 1 more because it will always find the song itself
        for (Song s : songTree.getClosestN(NUM_REC+1, search)) {
          // dont recommend the input song
          if (!s.getID().equals(search.getID())) {
            printSongWithSection(s);
          }
        }
      }
    }
  }

  private static void printSongWithSection(Song s) {
    System.out.printf("(id=%s, song=%s, %s-%s)\n", s.getID(), s.getName(),
        s.getSectionStartTimePretty(), s.getSectionEndTimePretty());
  }

  /**
   *
   * @param songList
   * @return id -> coordinates
   */
  private static Map<String, Song> getIDTable(List<Song> songList) {
    Map<String, Song> table = new HashMap<>();
    for(Song s : songList){
      table.put(s.getID(), s);
    }
    return table;
  }

  /**
   *
   * @param path
   * @return dimension of each song (if they are all the same)
   */
  private static int populateSongList(String path, List<Song> toPopulate, int numFeatures) throws IOException{
    if (toPopulate == null) {
      throw new NullPointerException("cannot populate null song list");
    }
    int lineNum = 1;
    try (BufferedReader br = new BufferedReader(new FileReader(new File(path)))) {
      String line;
      while ((line = br.readLine()) != null) {
        String[] fields = line.split(",");
        if (numFeatures + NUM_META_FIELDS != fields.length) {
          throw new IOException("wrong number of fields in data file " + path + ": line " + lineNum);
        }
                /* csv so far is well-formed */
        ImmutableList.Builder<Double> ilb = new ImmutableList.Builder<>();
        for (int i = NUM_META_FIELDS; i < numFeatures + NUM_META_FIELDS; i++) {
          ilb.add(Double.parseDouble(fields[i]));
        }
        double startTime = Double.parseDouble(fields[2]);
        double endTime = Double.parseDouble(fields[3]);
        toPopulate.add(new Song(numFeatures, fields[0], fields[1], startTime, endTime, ilb.build()));
        lineNum++;
      }
    } catch (NumberFormatException n) {
      quit(-1, "number format problem in " + path + ": line " + lineNum);
    } catch (IOException e) {
      quit(-1, e.getMessage());
    }
    if (lineNum == 1) {
      // csv has no good lines
      throw new IOException("empty csv: " + path);
    }
    return numFeatures;
  }
}
