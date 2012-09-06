rm -rf ./gen
ant debug
mkdir -p bin
adb install -r bin/BOLDApp-debug.apk
