# music_recommend
5/23/15 spotify hackathon project


DESCRIPTION

2 components: load_million_song and recommender


  LOAD_MILLION_SONG:
      
        Reads the Million Song Dataset (or MillionSongSubset) into records containing ID, artist, album, song and pitches (chroma), and creates a database containing a feature of the topic probabilities of each type of chord for each song
        
        To run this test, you will have to download the Echonest Million Song Subset, available at:
          http://labrosa.ee.columbia.edu/millionsong/pages/getting-dataset#subset
        which links directly to the file:
          http://static.echonest.com/millionsongsubset_full.tar.gz
        Extract the tarball to a directory and set the root_path variable in the code
        to the data root directory.
        
        Has been tested in Windows 7.
        
        loadmillionsong.py:
        Dependencies:
        Python 2.7
        h5py library
        
        How to use:
        From your Python application, import the load_million_song.py module. Iterate over the song data by calling 
        iterate_folder_songs_extracted(root_path, filename_re),
        where root_path is the path to the data folder in the Million Song Dataset (or subset), and filename_re is 
        a regular expression to identify a valid H5 datset, e.g., "^[A-Z]{7}[0-9,A-F]{11}\.h5$"
        An example of a song data file name is TRBIJIA128F425F57D.h5
        
        compute_features.py:
        Dependencies:
        Python 2.7
        numpy
        scipy
        pyechonest
        pytables
        
        How to use:
        From your Python application, import the compute_features.py module. 
        Create the .csv database by calling compute_features.save_feature_database(root_path,csvpath), where root_path is the location of the “data” subfolder within the Million Song Subset and csvpath is where you want to create the feature database.  
      
  
  RECOMMENDER:
        Stores songs with name, id and floating point coordinates in a KD-tree.
        Efficient nearest-neighbor search in n-dimensional Euclidean space
        
        Runs in Unix-like environment. 
        
        Dependencies:
        	java 7+
        	maven 3+
        
        
        build:
        
        $ mvn clean package
        
        
        run:
        
        $ load_recommender.sh path_to_song_file
        
        This will load the songs into memory and then enter an infinite loop. User should enter song ID and press enter. 
        Program will output a few of the most similar songs.
        
        Song file must be in CSV format (no header row) with the following fields in this order:
        
        song name | song id | coordinate 1 | coordinate 2 | ...
        with any fixed number of coordinates.
  
  

HOW TO USE


BUGS
