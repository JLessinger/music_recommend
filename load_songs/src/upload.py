import subprocess
import os
import json
import sys
from config import ECHONEST_API_KEY
import urllib2

def upload_and_get_echonest_id(mp3name):
    cmd = 'curl -F "api_key={0}" -F "filetype=mp3" -F "track=@{1}" "http://developer.echonest.com/api/v4/track/upload"'.format(ECHONEST_API_KEY, mp3name)
    devnull = open(os.devnull, 'w')
    return get_id_from_echonest_json_string(subprocess.check_output(cmd, shell=True, stderr=devnull))


def get_id_from_echonest_json_string(json_str):
    return json.loads(json_str)['response']['track']['id']

def get_profile(id):
    get_req = "http://developer.echonest.com/api/v4/track/profile?api_key={0}&format=json&id={1}&bucket=audio_summary".format(ECHONEST_API_KEY, id)
    json = urllib2.urlopen(get_req).read()
    return json

def get_analysis_from_profile(prof_json, title):
    prof = json.loads(prof_json)
    url = prof['response']['track']['audio_summary']['analysis_url']
    anal_json =  json.loads(urllib2.urlopen(url).read())
    anal_json['meta']['artist'] = 'artist for {0}'.format(title)
    anal_json['meta']['title'] = title
    return json.dumps(anal_json)

def get_analysis(mp3name):
    song_id = upload_and_get_echonest_id(mp3name)
    profile = get_profile(song_id)
    return song_id, get_analysis_from_profile(profile, mp3name[0:-4])

def write_feature_file(mp3name):
    anal = get_analysis(mp3name)    
    f = open("../analysis_files/{0}.analysis".format(anal[0]), 'w')
    f.write(anal[1])
    f.close()


if __name__ == "__main__":
    if len(sys.argv) == 2:
        write_feature_file(sys.argv[1])
        print "done writing"
    else:
        print "usage: python upload.py mp3_file"
