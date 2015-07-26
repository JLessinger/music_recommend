import numpy as np
from scipy.signal import medfilt
from scipy.spatial.distance import euclidean
import pyechonest
from pyechonest import song
from pyechonest import track
from pyechonest import config
config.ECHO_NEST_API_KEY="DAQJT7WW3IYXQTPOW"
import csv
import hdf5_getters
import beat_aligned_feats
import load_million_song
from collections import namedtuple
from time import time
import os
import sys

ORDER = 2

def save_feature_vector(feature_vector,artist,title,id,csvpath):
    # Save feature vector to a comma separated values file
    #
    # Parameters:
    # feature_vector: 1 x S-1 np.array -- vector of features
    # csvpath: string -- path where CSV file should be saved
    #
    # Returns: none
    
    csvrow = str(artist) + ': ' + str(title) + ',' + str(id) + ',' + \
        str(','.join(map(str, feature_vector))) + '\n'
    #print csvrow
    f = open(csvpath,'a')
    f.write(csvrow)
    f.close()

def save_feature_database(root_path,csvpath):
    # Create a database where each song is represented by a line in a CSV file
    # as such: artist/title , echonest id , feature_values
    #
    # Parameters
    # root_path: string -- path to MillionSongSubset's data folder
    # csvpath: string -- path where the .csv database will be created
    #
    # Returns: none
    
    filename_re = "^[A-Z]{7}[0-9,A-F]{11}\.(h5|analysis)$" # Example: TRBIJIA128F425F57D.h5
    time_start = time()
    for loop_nr, song_rec in enumerate(load_million_song.iterate_folder_songs_extracted(root_path, filename_re)):
        
        timbre = song_rec.timbre
        sections_start = song_rec.sections_start
        sections_conf = song_rec.sections_conf
        segments_start = song_rec.segments_start
        song_end = song_rec.song_end
        if len(sections_conf) > 0:
            feature_vector = get_feature_vector(timbre,sections_start,sections_conf,segments_start,song_end)
            if feature_vector is None:
                continue
            artist = song_rec.artist
            title = song_rec.title
            id = song_rec.id
            #print id
            #print artist,':',title
            #print poly_feature
            save_feature_vector(feature_vector,artist,title,id,csvpath)

            if ((loop_nr + 1) % 100) == 0:
                print "{0} songs read in {1:.1f} seconds" \
                    .format(loop_nr + 1, time() - time_start)

        end_time = time() - time_start
        print "Total: {0} songs read in {1:.1f} seconds".format(loop_nr + 1, end_time)

## Input: n X 12 (segment by timbre component)
## Output: order x 12
def get_poly_coefficients(timbre_cols, timestamps, order):
    assert timbre_cols.shape[0] == timestamps.shape[0]
    def fit_series(ser_arr):
        return np.polyfit(timestamps, ser_arr, order)
    return np.column_stack(tuple(map(fit_series, timbre_cols.transpose())))

def get_feature_vector(timbre,sections_start,sections_conf,segments_start,song_end):
    best_section = 1 + np.argmax(sections_conf[1:])
    best_section_start = sections_start[best_section]
    if len(sections_start) > best_section + 1:
        best_section_end = sections_start[best_section + 1]
    else:
        best_section_end = song_end
    seg_indices = []
    for i in range(len(segments_start)):
        if best_section_start < segments_start[i] < best_section_end:
            seg_indices.append(i)

    timbre_feature = timbre[seg_indices,:]
    timestamps = segments_start[seg_indices]
    if len(seg_indices) <= ORDER:
        return None
    poly_feature = get_poly_coefficients(timbre_feature,timestamps,ORDER).reshape(-1)
    start_end_vector = [best_section_start,best_section_end]
    feature_vector = np.append(start_end_vector,poly_feature)
    return feature_vector

def generate_feature_file(root_path,csvpath):
    # Main program
    
    #root_path = '/Users/victoriadennis/Documents/databases/MillionSongSubset/data'
    #csvpath = '/Users/victoriadennis/Documents/github/music_recommend/recommender/test_data/songs.csv'
    try:
        os.remove(csvpath)
        save_feature_database(root_path, csvpath)
    except OSError:
        raise Exception("couldn't remove csv")

if __name__ == '__main__':
    root_path = '../resources'
    csv_path = sys.argv[1]
    generate_feature_file(root_path, csv_path)
