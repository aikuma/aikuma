cd Aikuma
rm bin/Aikuma.apk
pwd
ant release
pwd
jarsigner -verbose -sigalg SHA1withRSA -digestalg SHA1 -keystore ../aikuma.keystore bin/Aikuma-release-unsigned.apk aikuma_key
pwd
zipalign -v 4 bin/Aikuma-release-unsigned.apk bin/Aikuma.apk
