#!/usr/bin/python

"""Exports the wav files from an Aikuma bold directory to another directory
with human-readable file names."""

import argparse
import os
import json
import re
import shutil

parser = argparse.ArgumentParser(description=
        "Export wav files from an Aikuma directory")
parser.add_argument("aikuma_dir", metavar="SRC", type=str,
        help="The name of the Aikuma directory")
parser.add_argument("export_dir", metavar="DEST", type=str,
        help="The name of the directory to export the wav files to")

args = parser.parse_args()

uuid_to_name = {}

def generate_name(filename, user_map):
    json_str = open(filename).read()
    obj = json.loads(json_str)
    name = obj["languages"][0]["code"] + "-"
    name += user_map[obj["creator_uuid"]] + "-"
    name += obj["date_string"] + "-"
    name = re.sub(" ", "T", name)
    name += obj["recording_name"]
    name = re.sub(" ", "_", name)
    name += ".wav"
    return name

def gen_user_map(users_path):
    user_map = {}
    for userdir in os.listdir(users_path):
        metadata_file = os.path.join(
                args.aikuma_dir, "users", userdir, "metadata.json")
        json_obj = json.loads(open(metadata_file).read())
        user_map[json_obj["uuid"]] = json_obj["name"]
    return user_map

recording_path = os.path.join(args.aikuma_dir, "recordings")
users_path = os.path.join(args.aikuma_dir, "users")
user_map = gen_user_map(users_path)
os.mkdir(args.export_dir)
for filename in os.listdir(recording_path):
    filename = os.path.join(recording_path, filename)
    if filename.endswith(".json"):
        source = re.sub("\.json", ".wav", filename)
        print "source", source
        target = os.path.join(
                args.export_dir, generate_name(filename, user_map))
        print "target", target
        shutil.copyfile(source, target)

