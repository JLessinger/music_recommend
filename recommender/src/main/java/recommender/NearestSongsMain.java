package recommender;

import com.google.common.collect.ImmutableList;
import kdtree.*;

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
    private static final int NUM_REC = 5;

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
                /*load songs */
                List<Song> songList = new ArrayList<>();
                int songDim = populateSongList(args[0], songList);
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
                        /* we have to get 1 more because it will always find the song itself */
                for (Song s : songTree.getClosestN(NUM_REC+1, search)) {

                    if (!s.getID().equals(search.getID())) {
                                /*dont recommend the same song*/
                        System.out.println(s.getName() + " (id=" + s.getID() + ")");

                    }
                }
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

    /**
     *
     * @param path
     * @return dimension of each song (if they are all the same)
     */
    private static int populateSongList(String path, List<Song> toPopulate) throws IOException{
        if (toPopulate == null) {
            throw new NullPointerException("cannot populate null song list");
        }
        int lineNum = 1;
        int numFields = -1;
        try (BufferedReader br = new BufferedReader(new FileReader(new File(path)))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] fields = line.split(",");
                if (numFields == -1) {
                    /* if this is the first line, set the number of fields */
                    numFields = fields.length;
                } else {
                    /* otherwise, check that this line has the right number of fields and exit if not */
                    if (numFields != fields.length) {
                        throw new IOException("wrong number of fields in data file " + path + ": line " + lineNum);
                    }
                }
                /* csv so far is well-formed */
                ImmutableList.Builder<Double> ilb = new ImmutableList.Builder<>();
                for (int i = 2; i < numFields; i++) {
                    ilb.add(Double.parseDouble(fields[i]));
                }
                toPopulate.add(new Song(numFields - 2, fields[0], fields[1], ilb.build()));
                lineNum++;
            }
        } catch (NumberFormatException n) {
            quit(-1, "number format problem in " + path + ": line " + lineNum);
        } catch (IOException e) {
            quit(-1, e.getMessage());
        }
        if (numFields < 0) {
            /* numFields was never set, so csv has 0 lines */
            throw new IOException("empty csv: " + path);
        }
        return numFields - 2;
    }
}
