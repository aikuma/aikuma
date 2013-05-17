#!/usr/bin/python

"""Exports the wav files from an Aikuma bold directory to another directory
with human-readable file names.
"""

from __future__ import print_function
import argparse
import json
import os
import re
import shutil

SAMPLE_RATE = 16000

def gen_user_map(users_path):
    """Given the path to a directory of users, creates a dictionary that maps
    from user UUIDs to usernames.
    """
    user_map = {}
    for userdir in os.listdir(users_path):
        metadata_file = os.path.join(
                args.aikuma_dir, "users", userdir, "metadata.json")
        json_obj = json.loads(open(metadata_file).read())
        user_map[json_obj["uuid"]] = json_obj["name"]
    return user_map

def generate_name(filename, user_map):
    """Takes a filename and a dictionary mapping from user UUIDs to usernames
    and returns a human-readable name for the file in the form
    <iso639-3 code>-<username>-<iso8601 date>-<recording name>.wav
    """
    json_str = open(filename).read()
    obj = json.loads(json_str)
    name = obj["languages"][0]["code"] + "-"
    name += user_map[obj["creator_uuid"]] + "-"
    name = re.sub(" ", "_", name)
    name += obj["date_string"] + "-"
    name = re.sub(" ", "T", name)
    name += obj["recording_name"]
    name = re.sub(" ", "_", name)
    return name

def sample_to_sec(sample):
    sample = float(sample)
    return sample / SAMPLE_RATE

def generate_textgrid(filename,tgfilename):
    print(filename, tgfilename)
    obj = json.loads(open(filename).read())
    if "original_uuid" in obj:
        tgfile = open(tgfilename, "w")
        mapfilename = os.path.splitext(filename)[0] + ".map"
        mappingdata = open(mapfilename).readlines()
        intervals = []
        for line in mappingdata:
            interval = line.split(":")
            s1 = interval[0].split(",")[0]
            s2 = interval[0].split(",")[1]
            t1 = interval[1].split(",")[0]
            t2 = interval[1].split(",")[1]
            intervals.append(((sample_to_sec(s1), sample_to_sec(s2)),
                    (sample_to_sec(t1),sample_to_sec(t2))))
        print("File type = \"ooTextFile\"", file=tgfile)
        print("Object class = \"TextGrid\"", file=tgfile)
        print("", file=tgfile)
        print("xmin = 0", file=tgfile)
        print("xmax = {0}".format(intervals[-1][0][1]), file=tgfile)
        print("tiers? <exists>", file=tgfile)
        print("size = 1", file=tgfile)
        print("item = []:", file=tgfile)
        print("\titem = [1]:", file=tgfile)
        print("\t\tclass = \"IntervalTier\"", file=tgfile)
        print("\t\tname = \"target\"", file=tgfile)
        print("\t\txmin = 0", file=tgfile)
        print("\t\txmax = {0}".format(intervals[-1][0][1]), file=tgfile)
        print("\t\tintervals: size = " + str(len(intervals)), file=tgfile)
        for i in range(0,len(intervals)):
            print("\t\tintervals [" + str(i+1) + "]:", file=tgfile)
            print("\t\t\txmin = {0}".format(intervals[i][0][0]), file=tgfile)
            print("\t\t\txmax = {0}".format(intervals[i][0][1]), file=tgfile)
            print("\t\t\ttext = \"{0}, {1}\"".format(intervals[i][1][0],
                    intervals[i][1][1]), file=tgfile)


parser = argparse.ArgumentParser(description=
        "Export wav files from an Aikuma directory")
parser.add_argument("aikuma_dir", metavar="SRC", type=str,
        help="The name of the Aikuma directory")
parser.add_argument("export_dir", metavar="DEST", type=str,
        help="The name of the directory to export the wav files to")

args = parser.parse_args()

# Iterates through the files in the source directory, generates their
# appropriate names and copies them to the destination directory.
recording_path = os.path.join(args.aikuma_dir, "recordings")
users_path = os.path.join(args.aikuma_dir, "users")
user_map = gen_user_map(users_path)
os.mkdir(args.export_dir)
for filename in os.listdir(recording_path):
    filename = os.path.join(recording_path, filename)
    if filename.endswith(".json"):
        source = re.sub("\.json", ".wav", filename)
        target = os.path.join(
                args.export_dir, generate_name(filename, user_map)+".wav")
        shutil.copyfile(source, target)
        #strippedfilename = os.path.splitext(os.path.basename(filename))[0]
        tgfilename = os.path.join(args.export_dir,
                generate_name(filename, user_map)+".TextGrid")
        generate_textgrid(filename, tgfilename)
