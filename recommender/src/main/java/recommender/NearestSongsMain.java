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
    public static final String USG_MSG = "usage: load_recommender.sh path_to_song_db num_features";
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
        if (args.length != 2) {
            quit(-1);
        } else{
            try {
                int songDim = Integer.parseInt(args[1]);
                            /*load songs */
                List<Song> songList = getSongList(args[0], songDim);
                Map<String, Song> songsByID = getIDTable(songList);

                KDTree<Song, Double> songTree = new KDNode<>(songDim, songList);

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

    private static List<Song> getSongList(String path, int songDim) {
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
                songList.add(new Song(songDim, fields[0], fields[1], ilb.build()));
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
