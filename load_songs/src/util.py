import json
import os


def get_config_key(key):
    config_file = open("../../global_config.json")
    json_obj = json.loads(config_file.read())
    config_file.close()
    return json_obj[key]

def get_file_base(path):
    return os.path.splitext(os.path.basename(path))[0]