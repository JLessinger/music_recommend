import numpy as np
from scipy.signal import medfilt
from scipy.spatial.distance import euclidean
from pyechonest import config
config.ECHO_NEST_API_KEY="DAQJT7WW3IYXQTPOW"
import iterate_songs
from time import time

def get_chord_templates(template_type):
    # Create chord templates for major and minor chords
    #
    # Parameters:
    # template_type: string
    # --type of template
    # --options: 'majmin',
    #
    # Returns:
    # chord_templates: Tx12 np.array
    # --first 12 rows are major templates
    # --next 12 are minor, last is no chord
    
    if template_type == 'majmin':
        scale_degree_templates = np.zeros([25,3])
        chord_templates = np.zeros([25,12])
    # major chords
    for i in range(12):
        scale_degree_templates[i] = np.mod(np.add([0,4,7],i),12)
        chord_templates[i][scale_degree_templates[i].astype('int')] = (1./3.)
    # minor chords
    for i in range(12,24):
        scale_degree_templates[i] = np.mod(np.add([0,3,7],i),12)
        chord_templates[i][scale_degree_templates[i].astype('int')] = (1./3.)
    # no chord
    chord_templates[-1,:] = 1./12.

    return chord_templates

def get_divergence(chroma,chord_templates):
    # Compute divergence between chroma and each chord template
    #
    # Parameters:
    # chroma: 1x12 np.array -- pitch class intensity for each pitch
    # chord_templates: Tx12 np.array -- templates for each type of chord
    #
    # Returns:
    # divergence: 1xT np.array -- divergence of chroma from each template
    
    divergence = np.zeros([1,chord_templates.shape[0]])
    for i in range(chord_templates.shape[0]):
        divergence[0,i] = euclidean(chroma,chord_templates[i])
    
    return divergence

def get_chord_name(index):
    # Print name of chord from its index in the template
    #
    # Parameters:
    # index: int -- index of chord from 1 to T
    #
    # Returns:
    # chord_name: string -- name of chord
    chords={0:'C',1:'C#',2:'D',3:'D#',4:'E',5:'F',
        6:'F#',7:'G',8:'G#',9:'A',10:'A#',11:'B'};
    if index < 12:
        chord_name = chords[index] + 'M'
    elif index < 24:
        chord_name = chords[index-12] + 'm'
    else:
        chord_name = 'no chord'
    
    return chord_name

def get_chord_array(btchromas,template_type):
    # Get the chord in each beat of a track
    # (beats determined by echonest)
    #
    # Parameters:
    # btchromas: 12 x S np.array -- beat-sync chroma in each segment
    #
    # Returns:
    # chord_array: 1 x S np.array -- chord type in each segment
    
    chord_templates = get_chord_templates(template_type)
    divergence_matrix = np.zeros([chord_templates.shape[0],btchromas.shape[1]])
    chord_array = np.zeros(btchromas.shape[1])
    # build divergence series
    for i in range(btchromas.shape[1]):
        divergence_matrix[:,i] = get_divergence(btchromas[:,i],chord_templates)
    #div_filt = medfilt(divergence, kernel_size=15)
    
    div_matrix_filt = medfilt(divergence_matrix,[1,9])
    chord_array = np.argmin(div_matrix_filt,axis=0)

    return chord_array

#def get_tonic_array(track):
#    # Get the tonic key in every segment of a track
#    #
#    # Parameters:
#    # track: echonest track object
#    #
#    # Returns:
#    # key_array: 1 x S np.array -- tonic in each segment
#
#    tonic_pos = 0
#    for i in range(0,len(t.sections)):
#        section_end = t.sections[i]['start'] + t.sections[i]['duration']
#        while(t.beats[tonic_pos]['start'] < section_end and key_pos < len(t.segments)):
#            tonic_array[tonic_pos] = t.sections[i]['key']
#            tonic_pos += 1
#
#    return tonic_array

def create_feature_vector(btchromas, template_type):
    # Create feature vector based on chord content
    #
    # Parameters:
    # h5path: path to h5 file containing track info
    #
    #
    # Returns:
    # feature_vector: 1 x S-1 np.array
    # -- first value is the percentage of the track that's root major chords,
    # -- second value is second degree major chords
    # ...
    # -- thirteenth value is root minor chords
    
    #h5 = hdf5_getters.open_h5_file_read(h5path)
    #key = hdf5_getters.get_key(h5)
    #h5.close()
    
    chord_array = get_chord_array(btchromas,template_type)
    major_chord_array = chord_array[np.nonzero(chord_array < 12)]
    minor_chord_array = chord_array[np.nonzero((chord_array > 12) & (chord_array < 24))]
    
    major_hist = np.histogram(major_chord_array,bins = 12,range = (-0.5,11.5))[0]
    minor_hist = np.histogram(minor_chord_array,bins = 12,range = (11.5,23.5))[0]
    total_hist = np.concatenate([major_hist,minor_hist])
    
    key = np.argmax(total_hist)
    
    #make features relative to tonic
    rel_major_hist = np.zeros(12)
    rel_minor_hist = np.zeros(12)
    for i in range(12):
        rel_major_hist[i] = major_hist[np.mod(i+key,12)]
        rel_minor_hist[i] = minor_hist[np.mod(i+key,12)]
    total_rel_hist = np.concatenate([rel_major_hist,rel_minor_hist])
    
    feature_vector = total_rel_hist/float(sum(total_rel_hist))
    
    return feature_vector,key

def print_estimated_info(feature_vector,num_top,key):
    # Print the estimated key and names of the num_top most frequent chords in a
    # feature vector, and their associated topic probabilities
    #
    # Parameters:
    # feature_vector: 1 x S-1 np.array
    # -- first value is the percentage of the track that's root major chords,
    # -- second value is second degree major chords
    # ...
    # -- thirteenth value is root minor chords
    #
    # Returns: none
    
    print 'key: ' + get_chord_name(key)
    top_chords_num = feature_vector.argsort()[-num_top:][::-1]
    for i in range(len(top_chords_num)):
        if top_chords_num[i] < 12:
            abs_chord = np.mod(top_chords_num[i] + key, 12)
        else:
            abs_chord = np.mod(top_chords_num[i] - 12 + key, 12) + 12
        chord_name = get_chord_name(abs_chord)
        print chord_name + ' ' + str(feature_vector[top_chords_num[i]])

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
    print csvrow
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
    
    filename_re = "^[A-Z]{7}[0-9,A-F]{11}\.h5$" # Example: TRBIJIA128F425F57D.h5
    time_start = time()
    for loop_nr, song_rec in enumerate(iterate_songs.iterate_folder_songs_extracted(root_path, filename_re)):
        
        btchromas = song_rec.btchromas
        feature_vector,key = create_feature_vector(btchromas,'majmin')
        artist = song_rec.artist
        title = song_rec.title
        id = song_rec.id
        print id
        print artist,':',title
        print feature_vector
        print_estimated_info(feature_vector,8,key)
        save_feature_vector(feature_vector,artist,title,id,csvpath)
        
        if ( (loop_nr + 1) % 1000) == 0:
            print "{0} songs read in {1:.1f} seconds" \
                .format(loop_nr + 1, time() - time_start)

    end_time = time() - time_start
    print "Total: {0} songs read in {1:.1f} seconds".format(loop_nr + 1, end_time)

#if __name__ == '__main__':
    # Main program
    
    #root_path = '/Users/victoriadennis/Documents/databases/MillionSongSubset/data'
    #csvpath = '/Users/victoriadennis/Documents/databases/MillionSongSubset/features/songs.csv'
    #filename_re = 'TRBIJIA128F425F57D.h5'
    #save_feature_database(root_path,csvpath)