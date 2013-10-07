Installation
------------

There are three approaches to installing Aikuma. The simplest is to install it
on an android phone via Google Play
(https://play.google.com/store/apps/details?id=org.lp20.aikuma).

The second approach involves building Aikuma. In order to build Aikuma the
Android SDK is required. For information on how to set up the Android SDK,
visit http://developer.android.com/sdk/index.html.

With the Android SDK set up and an Android phone connected to the computer,
run:

$ant clean debug install

There is a third approach. It also requires the Android SDK to be installed,
but no building is required. Download the APK from the Aikuma website
lp20.org/aikuma/files/Aikuma-debug.apk
URL HERE>, and then with an Android phone plugged in run:

$adb install Aikuma-debug.apk
