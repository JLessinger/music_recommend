import json
import numpy as np
import util

def get_json_obj(anal_path):
    return json.loads(open('{0}'.format(anal_path), 'r').read())

def get_track_id(anal_name):
    return util.get_file_base(anal_name)

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


