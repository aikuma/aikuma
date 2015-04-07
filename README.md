Installation
============

There are two approaches to installing Aikuma:

Google Play
-----------

The simplest is to install it on an android phone via Google Play
(https://play.google.com/store/apps/details?id=org.lp20.aikuma).

Building from Source
--------------------

The second approach involves downloading the Aikuma source from Github and
building it.

Dependencies:
  * Android SDK (https://developer.android.com/sdk/index.html)
  * Android 4.2.2 SDK Platform (download using the `android` command included in the Android SDK)
  * Google Play Service library (http://developer.android.com/google/play-services/setup.html)
  * JDK 6 (for the Android SDK)
  * Apache Ant (for the Android SDK)
  * Gradle (2.3.6 recommended to build aikuma-cloud-storage.jar)

Pre-installation steps:
  * Change the reference to Google Play Service library (http://developer.android.com/google/play-services/setup.html)
    1. Download Google Play Services from Android SDK Manager.
    2. Copy the library-folder($ANDROID_HOME/extras/google/google_play_services/libproject/google-play-services_lib) to the location you want.
    2. Check the ID of Android_4.2.2_API. This can be checked using `android list target`.
    3. type in `android update lib-project --target <target_ID> --path <path_to_the_copied_google_play_services_lib>`
    4. Change `../../workspace/google-play-services_lib` to \<relative_path_to_google_play_services_lib\> in Aikuma/project.properties
 
  * Build and move aikuma-cloud-storage library
    1. Install Gradle
    2. checkout the cloud-storage branch. `git checkout cloud-storage`
    3. Build the cloud-storage library. `cd AikumaCloudStorage`, `gradle publishToMavenLocal`
    4. Copy the cloud-storage library(~/.m2/repository/org/lp20/aikuma-cloud-storage/0.4.0/aikuma-cloud-storage-0.x.0.jar) to the location you want.
    5. checkout the Aikuma mobile app branch.(Latest test version is updated in search-interface branch). `git checkout search-interface`
    6. Replace any `aikuma-cloud-storage*.jar` with the built cloud-storage library to the folder(Aikuma/libs) 


With the dependencies installed and an Android phone connected to your computer, run:

`$ Aikuma/build.sh`

Aikuma should now be installed on your phone!


