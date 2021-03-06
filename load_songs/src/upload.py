import subprocess
import os
import json
import sys
import urllib2
import util
from util import get_config_key

ECHONEST_API_KEY = get_config_key("ECHONEST_API_KEY")

def upload_and_get_echonest_id(mp3name):
    ECHONEST_API_KEY = get_config_key("ECHONEST_API_KEY")
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

def get_analysis(mp3path):
    song_id = upload_and_get_echonest_id(mp3path)
    profile = get_profile(song_id)
    mp3name = util.get_file_base(mp3path)
    return song_id, get_analysis_from_profile(profile, mp3name)

def write_analysis_file(mp3path):
    anal = get_analysis(mp3path)
    f = open("../analysis_files/{0}.analysis".format(anal[0]), 'w')
    f.write(anal[1])
    f.close()


if __name__ == "__main__":
    if len(sys.argv) == 2:
        write_analysis_file(sys.argv[1])
        print "done writing"
    else:
        print "usage: python upload.py mp3_file"
