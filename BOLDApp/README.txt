Installation
------------

At the end of this you will have installed:
- Android SDK 2.3.3 API 10
- Necessary binaries (android, adb)

You need:
- Mac OSX / Linux
- Command line

Steps:
- Download the SDK: http://developer.android.com/sdk/index.html
- Command line: Move to tools to eg. /usr/local -> /usr/local/android-sdk-macosx
- Command line: Run ./tools/android. Or /usr/local/android-sdk-macosx/tools/android
- A window will open.
- Open branch "Android 2.3.3 (API 10)".
- Select "SDK Platform".
- Click "Install package".
- Command line: Run /usr/local/android-sdk-macosx/platform-tools/adb
- Put /usr/local/android-sdk-macosx/platform-tools/ in your PATH.
- Go into the directory this README resides in.
- Run ./install.sh
- If you run into trouble:
  - There is a faulty configuration line in build.xml? Remove that line entirely (Florian tried to avoid this solution, but this seems to be the way to go).
  - The phone refuses the app? Remove all traces of any old BOLD apps ("Uninstall Application").
  - Having trouble to start the app? Remove the "bold" directory on the SD card.
  
Tagging
-------

We use semantic versioning http://semver.org/ – but tag infrequently, or when absolutely needed. Reason: Nothing depends on this. The only reason people need to refer to this is when sending in bug reports. So, we update the version when releasing a new version.