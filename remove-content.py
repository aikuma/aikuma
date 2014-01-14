#!/usr/bin/python

import argparse
import json
import os
import shutil

def delete_recording(aikuma_dir, recording_name):
    recordings_path = os.path.join(aikuma_dir, "recordings")
    for filename in os.listdir(recordings_path):
        filename = os.path.join(recordings_path, filename)
        if filename.endswith(".json"):
            json_data = json.loads(open(filename).read())
            if json_data["name"] == recording_name:
                os.remove(filename)
                os.remove(filename[:-4] + "wav")

def delete_speaker(aikuma_dir, speaker_name, recordings):
    speakers_path = os.path.join(aikuma_dir, "speakers")
    for filename in os.listdir(speakers_path):
        filename = os.path.join(speakers_path, filename)
        if os.path.isdir(filename):
            jsonfile = os.path.join(filename, "metadata.json")
            json_data = json.loads(open(jsonfile).read())
            if json_data["name"] == speaker_name:
                uuid = json_data["uuid"]
                os.remove(os.path.join(
                        aikuma_dir, "images", uuid + ".small.jpg"))
                os.remove(os.path.join(
                        aikuma_dir, "images", uuid + ".jpg"))
                shutil.rmtree(filename)
                if recordings:
                    delete_recordings_of_speaker(aikuma_dir, uuid)

def delete_recordings_of_speaker(aikuma_dir, speaker_uuid):
    recordings_path = os.path.join(aikuma_dir, "recordings")
    for filename in os.listdir(recordings_path):
        filename = os.path.join(recordings_path, filename)
        if filename.endswith(".json"):
            json_data = json.loads(open(filename).read())
            if speaker_uuid in json_data["speakersUUIDs"]:
                os.remove(filename)
                os.remove(filename[:-4] + "wav")

parser = argparse.ArgumentParser(
        description="Delete content from an Aikuma directory")
parser.add_argument("aikuma_dir", metavar="DIR", type=str,
        help="The Aikuma directory")
parser.add_argument("-s", "--speaker", metavar="USER", type=str,
        help="A speaker whose content, images and metadata are to be deleted")
parser.add_argument("-js", "--just_speaker", metavar="USER", type=str,
        help="A user whose image and metadata are to be deleted")
parser.add_argument("-r", "--recording", metavar="REC", type=str,
        help="A recording that is to be deleted")

args = parser.parse_args()

#delete_recording(args.aikuma_dir, args.recording)
#delete_speaker(args.aikuma_dir, args.speaker, True)
#delete_speaker(args.aikuma_dir, args.just_speaker, False)

print args
