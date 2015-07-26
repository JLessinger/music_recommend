package recommender;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import kdtree.KDNode;
import kdtree.KDTree;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jonathan on 5/23/15.
 */

public class NearestSongsMain {
  public static final String USG_MSG = "usage: load_recommender.sh path_to_song_db";
  private static final String SELECT_STMT = "SELECT * FROM songs;";
  private static int NUM_REC;
  private static int NUM_FEATURES;

  static {
    try {
      Class.forName("org.sqlite.JDBC");

      String jsonString = readFile("../global_config.json", Charsets.US_ASCII);
      JSONObject jsonObj = new JSONObject(jsonString);
      NUM_REC = jsonObj.getInt("NUM_RECOMMENDATIONS");
      NUM_FEATURES = (1 + jsonObj.getInt("POLY_ORDER")) * 12;
    } catch (JSONException | IOException e) {
      System.err.println("warning: couldn't reading global config. Falling back to defaults");
      System.err.println("cause: " + e);
      NUM_REC = 5;
      NUM_FEATURES = 36;
    } catch (ClassNotFoundException e) {
      System.err.println("fatal: could not load sqlite jdbc driver");
      System.exit(-1);
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
        populateSongList(args[0], songList, NUM_FEATURES);
        Map<String, Song> songsByID = getIDTable(songList);

        KDTree<Song, Double> songTree = new KDNode<>(NUM_FEATURES, songList);

        System.out.println("Successfully loaded songs. Entering recommend loop.");
        runQueryLoop(songsByID, songTree);
      } catch(IOException io){
        quit(-1, "fatal: IOExeption: " + io.getMessage());
      } catch (SQLException e) {
        quit(-1, "fatal: SQLException:" + e.getMessage());
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

    /*
  /**
   *
   * @param path
   */
  private static void populateSongList(String dbPath, List<Song> toPopulate, int numFeatures)
      throws SQLException {
    if (toPopulate == null) {
      throw new NullPointerException("cannot populate null song list");
    }
    Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
    Statement stat = conn.createStatement();
    ResultSet rs = stat.executeQuery(SELECT_STMT);
    while (rs.next()) {
      String name = rs.getString(1);
      String artist = rs.getString(2);
      String id = rs.getString(3);
      double startTime = rs.getDouble(4);
      double endTime = rs.getDouble(5);
      String[] featureArray = rs.getString(6).split(",");
      if (numFeatures != featureArray.length) {
        System.err.println("warning: wrong number of features: " + name + " (" + id + "). skipping.");
        continue;
      }
      ImmutableList.Builder<Double> ilb = new ImmutableList.Builder<>();
      for (int i = 0; i < numFeatures; i++) {
        try {
          ilb.add(Double.parseDouble(featureArray[i]));
        } catch (NumberFormatException e) {
          System.err.println("warning: feature not a parsable as double for song " + name + "(" + id + ")");
        }
      }
      toPopulate.add(new Song(numFeatures, name, artist, id, startTime, endTime, ilb.build()));
    }
  }
}
