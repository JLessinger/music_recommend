package spotify_hack;

import com.google.common.collect.ImmutableList;
import spotify_hack.kdtree.*;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jonathan on 5/23/15.
 */

public class NearestSongsMain {
    public static final String USG_MSG = "usage: load_recommender.sh path_to_song_db";
    private static final int SONG_DIM = 1;
    private static final int NUM_REC = 5;


    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println(USG_MSG);
        } else{
            /*load songs */
            List<Song> songList = getSongList(args[0]);
            Map<String, Song> songByID = getIDTable(songList);

            KDTree<Song, Double> songs = new KDNode<>(SONG_DIM, songList);

            /* nearest neighbors query loop*/
            try {
                BufferedReader br =
                        new BufferedReader(new InputStreamReader(System.in));

                String input;

                while ((input = br.readLine()) != null) {
                    Song search = songByID.get(input);
                    if(search==null) {
                        System.out.println("song not found");
                    } else {
                        for (Song s : songs.getClosestN(NUM_REC, search)) {

                            if (!s.getID().equals(search.getID())) {
                                //dont recommend the same song
                                System.out.println(s.getName() + " (id=" + s.getID() + ")");

                            }
                        }
                    }
                }

            }catch(IOException io){
                io.printStackTrace();
            }
        }
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

    private static List<Song> getSongList(String path) {
        List<Song> songList = new ArrayList<>();
        int lineNum = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(new File(path)))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] fields = line.split(",");
                ImmutableList.Builder<Double> ilb = new ImmutableList.Builder<>();
                for (int i = 2; i < fields.length; i++) {
                    ilb.add(Double.parseDouble(fields[i]));
                }
                songList.add(new Song(SONG_DIM, fields[0], Integer.parseInt(fields[1]), ilb.build()));
                lineNum++;
            }
        } catch(NumberFormatException n){
            System.err.println("bad song input file: line " + lineNum);
            System.exit(-1);
        } catch (IOException e) {
            System.err.println("problem opening file: "+path);
            System.exit(-1);
        }
        return songList;
    }
}
