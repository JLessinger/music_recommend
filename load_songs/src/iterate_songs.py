import os
import re
from time import time
from collections import namedtuple

from src.hdf5 import hdf5_getters
import json_anal_getters

song_record = namedtuple("song_record", "id  artist  title  timbre sections_start sections_conf segments_start song_end")

def iterate_folder_songs(root_path, filename_re):
    """Iterate over a collection of HDF5 database files, each one containing
    structured data, presumably a song. 
    Yields the full HDF5 record from the file.
    Arguments:
        root_path -- File path to the root of the file collection.
        filename_re -- Regular expression to match HDF5 files
    """
    # Open and collect each file in the database
    for dirpath, dirnames, filenames in os.walk(root_path):
        # Parse each file in the current directory
        for filename in filenames:
            # Sanity check on file name
            if filename_re and not re.search(filename_re, filename):
                print "Warning: Skipping non-matching file name {0}".format(filename)
                continue

            # Open an individual song file
            filepath = os.path.join(dirpath, filename)
            # with h5py.File(filepath, 'r') as song_file:
            #yield filename
            yield filepath

def iterate_folder_songs_extracted(root_path, filename_re):
    """Iterate over a collection of HDF5 database files, each one containing
    structured data, presumably a song. 
    Yields a processed record of song data.
    Arguments:
        root_path -- File path to the root of the file collection.
        filename_re -- Regular expression to match HDF5 files
    """
    for filepath in iterate_folder_songs(root_path, filename_re):
        # Extract the important data from the full song record

        filename = os.path.basename(filepath)
        ext = os.path.splitext(filename)[1]

        if ext == '.h5':
            song = hdf5_getters.open_h5_file_read(filepath)
            id = hdf5_getters.get_track_id(song)
            artist = hdf5_getters.get_artist_name(song)
            title = hdf5_getters.get_title(song)
            timbre = hdf5_getters.get_segments_timbre(song)
            sections_start = hdf5_getters.get_sections_start(song)
            sections_conf = hdf5_getters.get_sections_confidence(song)
            segments_start = hdf5_getters.get_segments_start(song)
            song_end = hdf5_getters.get_duration(song)
            song.close()
        elif ext == '.analysis':
            id = json_anal_getters.get_track_id(filepath)
            artist = json_anal_getters.get_artist_name(filepath)
            title = json_anal_getters.get_title(filepath)
            timbre = json_anal_getters.get_segments_timbre(filepath)
            sections_start = json_anal_getters.get_sections_start(filepath)
            sections_conf = json_anal_getters.get_sections_confidence(filepath)
            segments_start = json_anal_getters.get_segments_start(filepath)
            song_end = json_anal_getters.get_duration(filepath)
        else:
            raise Exception("unrecognized file type: {0}".format(filename))

        # Combine into a song record
        song_rec = song_record(id, artist, title, timbre, sections_start, sections_conf, segments_start, song_end)
        yield song_rec

def print_pitches(pitches):
    """Function to show how to interpret the pitches (chroma) member returned
    from the database
    """
    num_pitch_sets = pitches.shape[0]
    print "num_pitch_sets:", num_pitch_sets,

    num_pitches_per_set = pitches.shape[1]
    print "num_pitches_per_set:", num_pitches_per_set,

    first_set_of_12_pitches = pitches[0]
    print "first_set_of_12_pitches:",first_set_of_12_pitches,
    print "Third pitch:",first_set_of_12_pitches[2],

if __name__ == '__main__':
    """Main program, iterates the whole database
    To run this test, download the Echonest Million Song Subset, available at:
        http://labrosa.ee.columbia.edu/millionsong/pages/getting-dataset#subset
    which links directly to the file:
        http://static.echonest.com/millionsongsubset_full.tar.gz
    Extract the tarball to a directory and set the root_path variable below
    to the data root directory.
    """
    root_path = "MillionSongSubset/data"
    filename_re = "^[A-Z]{7}[0-9,A-F]{11}\.(h5|analysis)$" # Example: TRBIJIA128F425F57D.h5
    time_start = time()
    for loop_nr, song_rec in enumerate(iterate_folder_songs_extracted(root_path, filename_re)):

        if loop_nr == 0:
            # Debugging
            print song_rec,
            print_pitches(song_rec.pitches)
            print

        # Progress report        
        if ( (loop_nr + 1) % 1000) == 0:
            print "{0} songs read in {1:.1f} seconds" \
                .format(loop_nr + 1, time() - time_start)

    end_time = time() - time_start
    print "Total: {0} songs read in {1:.1f} seconds".format(loop_nr + 1, end_time)
    
