# music_recommend
5/23/15 spotify hackathon project

TODO:

* move away from sqlite - too slow
* sanity checks [see brian mcfee's paper], unit tests

DESCRIPTION

Timbre-based recommendation engine. You type in a song ID, you get a list of songs with similar timbre. 

ALGORITHM

* Song represented as point in Euclidean space. Recommendation = n nearest neighbors
* Point computed from song features:
       * Find "chorus" section (Highest "confidence")
		* Could use other confidence measures
       * Get segments from section
       * Get timbre vector contours from section
       * Get the 12 m-th order best fit polynomials
       Construct vector from 12*m coefficients



IMPLEMENTATION

2 components: load_songs and recommender


  LOAD_SONGS:
      
        Reads the Million Song Dataset (or MillionSongSubset) into records containing ID, artist, album, song and pitches (chroma), and creates a database containing a feature vector for each song (right now, just uses timbre). Can also upload an mp3 and add it to database.
        
        To run this test, you will have to download the Echonest Million Song Subset, available at:
          http://labrosa.ee.columbia.edu/millionsong/pages/getting-dataset#subset
        which links directly to the file:
          http://static.echonest.com/millionsongsubset_full.tar.gz
        Extract the tarball to a directory and set the root_path variable in the code
        to the data root directory.
        
        Has been tested in Windows 7, OS X
        
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
        Create the database by calling compute_features.save_feature_database(root_path, db_path), where root_path is the location of the “data” subfolder within the Million Song Subset and db_path is where you want to create the feature database.  
      
  
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
        
        Song file is a sqlite database file. See feature_databases/sql/ddl.sql for schema.



BUGS



