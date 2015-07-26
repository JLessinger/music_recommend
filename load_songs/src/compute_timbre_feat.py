import os
import numpy as np
import sqlite3
import iterate_songs

from time import time
import time as t
import sys
from util import get_config_key

ORDER = get_config_key("POLY_ORDER")
INSERT_STMT = 'INSERT OR REPLACE INTO SONGS (title, artist, id, startTime, endTime, features) VALUES  (?, ?, ?, ?, ?, ?);'

USG_MSG = "usage: python compute_timbre_feat.py analysis_root_path output_db_path"


def features_as_str(feature_vector):
    return ','.join(map(str, feature_vector))


def get_song_tuples(root_path):
    tuples = []
    # Parameters
    # root_path: string -- path to MillionSongSubset's data folder
    #
    # Returns: lists of tuples where each tuple is a song (database row)
    
    filename_re = "^[A-Z]{7}[0-9,A-F]{11}\.(h5|analysis)$" # Example: TRBIJIA128F425F57D.h5

    time_start = time()
    for loop_nr, song_rec in enumerate(iterate_songs.iterate_folder_songs_extracted(root_path, filename_re)):
        
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
            start, end = get_start_end(sections_start, sections_conf, song_end)
            tuples.append((title, artist, str(id), start, end, features_as_str(feature_vector)))

            if ((loop_nr + 1) % 100) == 0:
                print "{0} songs read in {1:.1f} seconds" \
                    .format(loop_nr + 1, time() - time_start)

    end_time = time() - time_start
    print "Total: {0} songs read in {1:.1f} seconds".format(loop_nr + 1, end_time)
    return tuples


def get_ddl():
    return open('../../feature_databases/sql/ddl.sql', 'r').read()


def create_db(dbpath):
    conn = sqlite3.connect(dbpath)
    c = conn.cursor()
    c.execute(get_ddl())
    conn.commit()
    conn.close()


def save_feature_sql_database(rootpath, dbpath):
    if not os.path.isfile(dbpath):
        create_db(dbpath)

    tuples = get_song_tuples(rootpath)
    con = sqlite3.connect(dbpath)
    cur = con.cursor()
    cur.executemany(INSERT_STMT, tuples)
    con.commit()
    # wait for this transaction to complete before inserting
    t.sleep(1)

## Input: n X 12 (segment by timbre component)
## Output: (1+order) x 12
def get_poly_coefficients(timbre_cols, timestamps, order):
    assert timbre_cols.shape[0] == timestamps.shape[0]
    def fit_series(ser_arr):
        return np.polyfit(timestamps, ser_arr, order)
    return np.column_stack(tuple(map(fit_series, timbre_cols.transpose())))

def get_start_end(sections_start, sections_conf, song_end):
    best_section = 1 + np.argmax(sections_conf[1:])
    best_section_start = sections_start[best_section]
    if len(sections_start) > best_section + 1:
        best_section_end = sections_start[best_section + 1]
    else:
        best_section_end = song_end
    return (best_section_start, best_section_end)

def get_feature_vector(timbre,sections_start,sections_conf,segments_start,song_end):
    best_section_start, best_section_end = get_start_end(sections_start, sections_conf, song_end)
    seg_indices = []
    for i in range(len(segments_start)):
        if best_section_start < segments_start[i] < best_section_end:
            seg_indices.append(i)

    timbre_feature = timbre[seg_indices,:]
    timestamps = segments_start[seg_indices]
    if len(seg_indices) <= ORDER:
        return None # i think silently fail is ok
    poly_feature = get_poly_coefficients(timbre_feature,timestamps,ORDER).reshape(-1)
    return poly_feature


if __name__ == '__main__':
    if len(sys.argv) != 3:
        print USG_MSG
    else:
        root_path = sys.argv[1]
        db_path = sys.argv[2]
        save_feature_sql_database(root_path, db_path)
