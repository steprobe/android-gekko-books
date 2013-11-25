Building the project
=====================

- The project depends on one third party library project that is part of the Android SDK. This is required for the action bar on older releases.
- To build on the command line (these have only been tried on a Mac, but should work ok on other machines)

```
cd $SDK_FOLDER/extras/android/support/v7/appcompat
android update project --target android-19 --path .

(if this fails for some reason make sure that API version 19 is downloaded and installed in the SDK manager)

cd $PROJECT_DIR
android update project --target android-19 --path . --name GekkoBooks
ant clean
ant debug

or

ant debug install

The second one will install if to any device connected
```

- To open in eclipse:
    The project can simply be imported in the normal way to eclipse, but make sure that the project at $SDK_FOLDER/extras/android/support/v7/appcompat is also imported.

