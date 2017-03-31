CWAC-Cam2: Taking Pictures. Made (Somewhat) Sensible. Again.
============================================================

Taking pictures using a third-party app is fairly straightforward,
using `ACTION_IMAGE_CAPTURE` and `ACTION_VIDEO_CAPTURE`. However, different camera
apps have slightly different behavior, meaning that you are prone to getting
inconsistent results.

Taking pictures using the Android SDK camera classes directly is
eminently possible, but is full of edge and corner cases, not to mention
its own set of per-device idiosyncracies. Plus, there are now two
separate APIs for this, three if you count `MediaRecorder` for
video recording.

`CWAC-Cam2` is an effort to create an in-app `ACTION_IMAGE_CAPTURE`/`ACTION_VIDEO_CAPTURE`
workalike, with a bit more configurability. You still integrate by
opening up a separate activity (`CameraActivity` and `VideoRecorderActivity`, in this case), but
it is all within your own app, rather than relying upon device- or
user-specific third-party camera apps.

Library Objectives
------------------
The #1 objective of this library is maximum compatibility with hardware. As such,
this library will not be suitable for all use cases.

The targeted use case is an app that might otherwise have relied upon
`ACTION_IMAGE_CAPTURE`/`ACTION_VIDEO_CAPTURE`, but needs greater reliability and somewhat greater
control.

If you are trying to write "a camera app" &mdash; an app whose primary job is
to take pictures &mdash; this library may be unsuitable for you.

Installation
------------
To integrate the core AAR, the Gradle recipe is:

```groovy
repositories {
    maven {
        url "https://s3.amazonaws.com/repo.commonsware.com"
    }
}

dependencies {
    compile 'com.commonsware.cwac:cam2:0.7.4'
}
```

The `cam2` artifact depends on some other libraries, available in
JCenter or Maven Central. They should be pulled down automatically
when you integrate in the `cam2` AAR.

You are also welcome to clone this repo and use the `cam2/` Android
library project2 in source form.

Basic Usage
-----------
The only supported API at the moment is through
[`CameraActivity` and its `IntentBuilder`](docs/CameraActivity.md) for
still pictures and
[`VideoRecorderActivity` and its `IntentBuilder`](docs/VideoRecorderActivity.md)
for videos. You will also want to review
[the documentation regarding permissions](docs/Permissions.md), as
while the library will work "out of the box", most likely you will
want to do some permission setup in your app for Android 6.0+ devices.

While there are other `public` classes and methods in the library,
ones that *may* be exposed as part of a public API in the future,
**they are not supported at the present time**.

**NOTE**: Ensure that you do not block hardware acceleration for
the library-provided activities. In other words, do not have
`android:hardwareAccelerated="false"` for your whole
`<application>` in the manifest.

Upgrade Notes
-------------
If you are moving from prior versions to 0.7.0 or higher, note
that this library now uses v3.0.0 of [greenrobot's EventBus](https://github.com/greenrobot/EventBus),
whereas previous versions of the library used a 2.x.x generation
of that library. This should not impact your own use of either
version of greenrobot's EventBus. Also note that the library will not
request runtime permissions on Android 6.0+ by default, as your app should be
doing that. You can call `requestPermissions()` on the `IntentBuilder`
to have the library request the permissions instead. See
[the permissions documentation](docs/Permissions.md) for more.

If you are moving from 0.5.x to 0.6.0 or higher, please note that
the default camera engine is now the one using `android.hardware.Camera`.
The `forceClassic()` method is deprecated, replaced with
`forceEngine()`, which takes a `CameraEngine.ID` enum value
(either `CLASSIC` or `CAMERA2`).

Also, if you are moving from 0.5.x to 0.6.0 or higher, please note
that your photos will be rotated, if the EXIF headers say that they
should be rotated, and if there is sufficient memory to rotate the
photos. Call `skipOrientationNormalization()` to avoid this.

If you are moving from 0.3.x or 0.4.x to 0.5.0 or higher, please
note that `FocusMode` is no longer an inner `enum` of
`AbstractCameraActivity`, but rather is a standalone Java file.
You will need to switch your import statement to
`com.commonsware.cwac.cam2.FocusMode`.

Tested Devices
--------------
The [compatibility status page](docs/CompatibilityStatus.md) outlines
what devices have been tested with this library by the library author.

Dependencies
------------
The `cam2` artifact depends upon `com.github.clans:fab` (for a floating
action button and floating action menu implementation) and
`de.greenrobot:eventbus` (for internal communications within the
library). Both are listed as dependencies in the AAR artifact metadata
and should be added to your project automatically.

ProGuard
--------
It is recommended that you not obfuscate the classes in CWAC libraries:

```
-keep class com.commonsware.cwac.** { *; }
```

If you feel that obfuscating open source code makes sense,
at minimum you will need to employ
[appropriate rules](https://github.com/krschultz/android-proguard-snippets/blob/master/libraries/proguard-eventbus.pro)
to prevent greenrobot's EventBus code, and this library's
use of it, from being obfuscated.

Version
-------
This is version v0.7.4 of this library, which means it is coming
along slowly.

Demo
----
There are two demo projects.

One is `demo/`. This illustrates taking pictures or recording
videos using the front
and rear-facing cameras. More importantly, it serves as a way of
collecting information about a device, particularly if you are
going to [file a bug report](CONTRIBUTING.md).

The `demo-playground/` sample project displays a `PreferenceFragment`
where you can tweak various `IntentBuilder` configurations, then tap
on an action bar item to take a picture using those settings. This is
good for experimenting with the `CameraActivity` and
`VideoRecorderActivity` capabilities.

Additional Documentation
------------------------
[The Busy Coder's Guide to Android Development](https://commonsware.com/Android)
contains three chapters related to this library. One is a tutorial
for implementing Android 6.0 runtime permissions that happens to use
this library. Another is a chapter on various ways to take pictures
that includes coverage of this library. A third chapter explores
the camera APIs in Android and references implementation details from
this library.

License
-------
The code in this project is licensed under the Apache
Software License 2.0, per the terms of the included LICENSE
file.

Questions
---------
If you have questions regarding the use of this code, please post a question
on [Stack Overflow](http://stackoverflow.com/questions/ask) tagged with
`commonsware-cwac` and `android` after [searching to see if there already is an answer](https://stackoverflow.com/search?q=[commonsware-cwac]+camera). Be sure to indicate
what CWAC module you are having issues with, and be sure to include source code 
and stack traces if you are encountering crashes.

If you have encountered what is clearly a bug, or if you have a feature request,
please read [the contribution guidelines](.github/CONTRIBUTING.md), then
post an [issue](https://github.com/commonsguy/cwac-cam2/issues).
**Be certain to include complete steps for reproducing the issue.**

You are also welcome to join
[the CommonsWare Community](https://community.commonsware.com/)
and post questions
and ideas to [the CWAC category](https://community.commonsware.com/c/cwac).

Do not ask for help via social media.

Release Notes
-------------
- v0.7.5: added `showRuleOfThirdsGrid()` to show [a "rule of thirds" grid overlay](https://github.com/commonsguy/cwac-cam2/issues/12) 
- v0.7.4: [added timer option](https://github.com/commonsguy/cwac-cam2/issues/297) for taking pictures 
- v0.7.3: fixed issues surrounding LG devices ([#295](https://github.com/commonsguy/cwac-cam2/issues/295), [#299](https://github.com/commonsguy/cwac-cam2/issues/299)) and 6.0 emulator ([#293](https://github.com/commonsguy/cwac-cam2/issues/293))
- v0.7.2: [added `FileProvider` to `demo-playground`](https://github.com/commonsguy/cwac-cam2/issues/284), fixed compatibility issues ([#271](https://github.com/commonsguy/cwac-cam2/issues/271), [#274](https://github.com/commonsguy/cwac-cam2/issues/274), [#286](https://github.com/commonsguy/cwac-cam2/issues/286))
- v0.7.1: fixed [Camera2 crash](https://github.com/commonsguy/cwac-cam2/issues/278) and [two](https://github.com/commonsguy/cwac-cam2/issues/33) old device [bugs](https://github.com/commonsguy/cwac-cam2/issues/32)
- v0.7.0
    - [Made runtime permission request opt-in](https://github.com/commonsguy/cwac-cam2/issues/233)
    - Upgraded to greenrobot EventBus 3.0.0
    - [Improved handling of rotation during video recording](https://github.com/commonsguy/cwac-cam2/issues/229)
    - [Added zoom support for video recording](https://github.com/commonsguy/cwac-cam2/issues/235)
    - More flexible constraint system for what features are used on what cameras, per [225](https://github.com/commonsguy/cwac-cam2/issues/225), [227](https://github.com/commonsguy/cwac-cam2/issues/227), [247](https://github.com/commonsguy/cwac-cam2/issues/247)
    - `CountDownLatch` [bug fix](https://github.com/commonsguy/cwac-cam2/issues/244)
- v0.6.9: fixed bug [in previous bug fix](https://github.com/commonsguy/cwac-cam2/issues/258), work around [destruction race condition](https://github.com/commonsguy/cwac-cam2/issues/257) 
- v0.6.8: catch [`Camera` exceptions](https://github.com/commonsguy/cwac-cam2/issues/68), fixed [chronometer support](https://github.com/commonsguy/cwac-cam2/issues/255) 
- v0.6.7: workaround for [Nexus 5X Android 7.0 bug](https://github.com/commonsguy/cwac-cam2/issues/184)
- v0.6.6: [improved confirmation behavior for devices that need images rotated](https://github.com/commonsguy/cwac-cam2/issues/241)
- v0.6.5: attempting a workaround for [some buggy devices](https://github.com/commonsguy/cwac-cam2/issues/246)
- v0.6.4: [made some methods `public`](https://github.com/commonsguy/cwac-cam2/issues/236) 
- v0.6.3: added `orientationLockMode()`, integrated pull requests for [library](https://github.com/commonsguy/cwac-cam2/pull/231) and [docs](https://github.com/commonsguy/cwac-cam2/pull/232)
- v0.6.2: fixed [Nexus 6P](https://github.com/commonsguy/cwac-cam2/issues/222) and [Honor 5X](https://github.com/commonsguy/cwac-cam2/issues/215) bugs
- v0.6.1: [Chronometer support](https://github.com/commonsguy/cwac-cam2/issues/220) and [config change bug fix](https://github.com/commonsguy/cwac-cam2/issues/219)
- v0.6.0: [EXIF orientation normalization](https://github.com/commonsguy/cwac-cam2/issues/15), [whitelist for `camera2` usage](https://github.com/commonsguy/cwac-cam2/issues/186), [more focus modes](https://github.com/commonsguy/cwac-cam2/issues/150), and [torch flash mode](https://github.com/commonsguy/cwac-cam2/issues/187)
- v0.5.11: [LG V10 H901](https://github.com/commonsguy/cwac-cam2/issues/141) bug fix
- v0.5.10: [LG G4](https://github.com/commonsguy/cwac-cam2/issues/195), [activity re-creation](https://github.com/commonsguy/cwac-cam2/issues/193) bugs fixed, plus [more graceful failure for a `camera2` issue](https://github.com/commonsguy/cwac-cam2/issues/192)  
- v0.5.9: [handle config changes/process termination better](https://github.com/commonsguy/cwac-cam2/issues/190)
- v0.5.8: better OOM handling, added `ResultReceiver` support for getting details of errors, bug fix in `confirmationQuality`
- v0.5.7: improved [OnePlus X compatibility](https://github.com/commonsguy/cwac-cam2/issues/175), allow [configurable confirmation quality](https://github.com/commonsguy/cwac-cam2/issues/180), [block camera-change FAB in exact-match scenario](https://github.com/commonsguy/cwac-cam2/issues/182) 
- v0.5.6: added quality control to still photos, added video recording to `demo/`
- v0.5.5: fixed [LG G3 video bug](https://github.com/commonsguy/cwac-cam2/issues/168) and [disable camera changes during video recording](https://github.com/commonsguy/cwac-cam2/issues/172)
- v0.5.4: fixed [two](https://github.com/commonsguy/cwac-cam2/issues/155) video [recording](https://github.com/commonsguy/cwac-cam2/issues/159) bugs
- v0.5.3: reverts [inadequate shutdown](https://github.com/commonsguy/cwac-cam2/issues/155) change from v0.5.2 due to bugs
- v0.5.2: fixed [zoom](https://github.com/commonsguy/cwac-cam2/issues/149), [inadequate shutdown](https://github.com/commonsguy/cwac-cam2/issues/155), and [dual engines](https://github.com/commonsguy/cwac-cam2/issues/156) issues
- v0.5.1: fixed [critical bug in video recording](https://github.com/commonsguy/cwac-cam2/issues/154)
- v0.5.0: added zoom support, better display orientation support
- v0.4.4: added Nexus 5X, Galaxy S4 Zoom to alt orientation whitelist
- v0.4.3: too-large camera preview issue fix
- v0.4.2: Camera2 API timing issue fix
- v0.4.1: Nexus 6P bug fix, support for choosing built-in activities or third-party camera apps
- v0.4.0: flash mode support, better preview sizing, bug fixes
- v0.3.4: more bug fixes, added debug preview frame support
- v0.3.3: yet more bug fixes
- v0.3.2: bug fixes
- v0.3.1: fixed bugs related to Nexus 7 (2012), SONY Xperia Z, and two Samsung models
- v0.3.0: added focus modes, exact camera match option, preview mirror option, and demo app improvements
- v0.2.3: reverted part of action bar divider line fix
- v0.2.2: FAB and divider line fixes, minor demo project improvements
- v0.2.1: added Android 6.0 runtime permission support, added missing <uses-feature> element
- v0.2.0: added initial support for video recording, fixed aspect ratio and other bugs
- v0.1.1: added `forceClassic()` and `updateMediaStore()`, fixed numerous issues
- v0.1.0: initial release

Who Made This?
--------------
<a href="http://commonsware.com">![CommonsWare](http://commonsware.com/images/logo.png)</a>

