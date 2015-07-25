import json
import os
import numpy as np
from os.path import basename

def get_json_obj(anal_name):
    return json.loads(open('analysis/{0}'.format(anal_name), 'r').read())

def get_track_id(anal_name):
    return os.path.splitext(anal_name)[0]

def get_title(anal_name):
    return get_json_obj(anal_name)['meta']['title']

def get_artist_name(anal_name):
    return get_json_obj(anal_name)['meta']['artist']

def get_mapper(anal_name, list_key, key):
    return np.array(map(lambda x: x[key], get_json_obj(anal_name)[list_key]))

def get_sections_start(anal_name):
    return get_mapper(anal_name, 'sections', 'start')

def get_sections_confidence(anal_name):
    return get_mapper(anal_name, 'sections', 'confidence')

def get_segments_start(anal_name):
    return get_mapper(anal_name, 'segments', 'start')

def get_segments_timbre(anal_name):
    return get_mapper(anal_name, 'segments', 'timbre')

def get_duration(anal_name):
    return get_json_obj(anal_name)['meta']['seconds']


