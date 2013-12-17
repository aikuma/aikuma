Aikuma is comprised of several subpackages of the package org.lp20.aikuma:

org.lp20.aikuma.ui contains code related to the user interface:
	- All the activities and fragments, with the exception of MainActivity,
	  which is in org.lp20.aikuma.
	- Subclasses of ArrayAdapter for the list activities
	- InterleavedSeekBar, a subclass of SeekBar to deal with the rendering of
	  the seek bar for interleaved playing
	- MenuBehaviour, to deal with menu behaviour across activities
	- NoiseLevel, code specific to PhoneRespeakActivity to deal with detecting
	  background noise levels and setting appropriate microphone sensitivity.
	- A subclass org.lp20.aikuma.ui.sensors, which presently contains a
	  ProximityDetector to allow Activities to have behaviour associated with
	  users placing the phone to the ear.

org.lp20.aikuma.model contains code related to models of data stored by Aikuma:
	- Speaker, to represent the speakers who have contributed to a recording.
	- Segments, to represent the mappings between segments of an original and
	  an associated respeaking or commentary.
	- Language, to represent ISO 639-3 languages, and custom languages.
	- Recording, to represent the metadata associated with a recording.
	- ServerCredentials, to store FTP server credentials for syncing.

org.lp20.aikuma.util contains various utilities to help get things done:
	- Client, an FTP client to facilitate syncing.
	- FileIO, to facilitate reading and writing Aikuma data from and to file.
	- ImageUtils, which contains methods to process and format user images
	  appropriately.
	- StandardDateFormat - which defines an ISO 8601 SimpleDateFormat for use
	  in the JSON representations of data.
	- SyncUtil, which uses the FTP client to periodically sync and offers a
	  method for immediate syncing.

org.lp20.aikuma.audio contains code related to recording and playing audio:
	- Audio provides methods to change which phone speaker the audio is played
	  through
	- SimplePlayer, a wrapper class for android.media.MediaPlayer.
	- MarkedPlayer, a subclass of SimplePlayer that allows for markers to be
	  set at points in the audio that trigger callbacks when reached.
	- Player, an interface for things that play audio.
	- InterleavedPlayer, a class that offers interleaved play of an original
	  recording and a respeaking, by playing alternate segments of each.
	- Beeper, which allows for notification beeps to be played when recording
	  starts and stops.
	- the org.lp20.aikuma.record subpackage is described below

org.lp20.aikuma.record is a subpackage of org.lp20.aikuma.audio (above) that
deals specifically with the recording aspect of audio processing. The key
components are:

	- Microphone, which is used to get input from the physical
	  microphone and yields buffers of that input data in a callback that is
	  supplied to the microphone.
	- PCMWriter, to write PCM/WAV files.
	- PhoneRespeaker, which provides the functionality required for audio
	  driven respeaking: Playing an original audio and recording the users
	  voice when they speak, interrupting playing.
	- ThumbRespeaker, which provides the functionality required for
	  thumb-driven respeaking: Plays the original when the user presses the
	  play button, records the respeaking when the user presses the respeak
	  button.
	- Two further subpackages, analyzers and recognizers, facilitate
	  determining when speech is occurring.
