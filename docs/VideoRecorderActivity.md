# Using VideoRecorderActivity

`VideoRecorderActivity` works much like [`CameraActivity`](CameraActivity.md)
gives you the same "API" as you get with the Android SDK's
`ACTION_VIDEO_CAPTURE`, making it fairly easy for you to get existing
`ACTION_VIDEO_CAPTURE` working with your own local video-recording activity.

## Getting the Intent

The simplest way to craft the right `Intent` to use is to create
a `VideoRecorderActivity.IntentBuilder`, call whatever configuration methods
that you want on that builder, and have it `build()` you an `Intent`.
That `Intent` can be used with `startActivityForResult()`, just as you
might have used it with an `ACTION_VIDEO_CAPTURE` `Intent`.

Under the covers, `VideoRecorderActivity.IntentBuilder` is simply packaging a
series of extras on the `Intent`, so you can always put those extras
on yourself if you so choose. The following table lists the available
configuration methods on `VideoRecorderActivity.IntentBuilder`, the corresponding
extra names (defined as constants on `VideoRecorderActivity`), their default values,
and what their behavior is:

| `IntentBuilder` Method   | Extra Key                 | Data Type                                 | Purpose |
|:------------------------:|:-------------------------:|:-----------------------------------------:|---------|
| `chronoType()`           | `EXTRA_CHRONOTYPE`        | `ChronoType` | Whether there should be a timer shown on the video recording preview screen, and, if so, whether it counts down the remaining time on a time-limited recording or whether it counts up for how long the current recording is. |
| `debug()`                | `EXTRA_DEBUG_ENABLED`     | `boolean`                                 | Indicate if extra debugging information should be dumped to LogCat (default is `false`) |
| `durationLimit()`        | `MediaStore.EXTRA_DURATION_LIMIT ` | `int`                            | Indicate the maximum length of the video in milliseconds |
| `facing()`               | `EXTRA_FACING`            | `AbstractCameraActivity.Facing`           | Indicate the preferred camera to start with (`BACK` or `FRONT`, default is `BACK`) |
| `focusMode()`            | `EXTRA_FOCUS_MODE`        | `AbstractCameraActivity.FocusMode`        | Indicate the desired focus mode for the camera (default is continuous if available, else device default) |
| `forceEngine()`          | `EXTRA_FORCE_ENGINE`             | `CameraEngine.ID`                         | Indicate a camera engine to use (`CLASSIC` or `CAMERA2`), default is determined by algorithm |
| `mirrorPreview()`        | `EXTRA_MIRROR_PREVIEW`    | `boolean`                                 | Indicate if preview should be horizontally flipped (default is `false`)|
| `onError()`              | `EXTRA_UNHANDLED_ERROR_RECEIVER` | `ResultReceiver`                   | Provide a IPC callback to be notified about errors inside Cam2 |
| `orientationLockMode()`  | `EXTRA_ORIENTATION_LOCK_MODE`| `OrientationLockMode`                  | Locks the device orientation (`PORTRAIT` or `LANDSCAPE`) or allows for config changes (`DEFAULT`) |
| `quality()`              | `MediaStore.EXTRA_VIDEO_QUALITY` | `VideoRecorderActivity.Quality`    | Indicate the quality, either `Quality.LOW` or `Quality.HIGH` (default=high) |
| `requestPermissions()`   | `EXTRA_FAIL_IF_NO_PERMISSION`| none (extra takes a `boolean`)         | Indicates if the library should request runtime permissions if your app did not do so already |
| `showRuleOfThirdsGrid()` | `EXTRA_SHOW_RULE_OF_THIRDS_GRID` | `boolean`                                 | Whether a "rule of thirds" grid should overlay the preview (default: `false`) |
| `sizeLimit()`            | `MediaStore.EXTRA_SIZE_LIMIT` | `int`                                 | Indicate the maximum size of the video in bytes |
| `to()`                   | `MediaStore.EXTRA_OUTPUT` | `File`                                    | Destination for picture to be written |
| `updateMediaStore()`     | `EXTRA_UPDATE_MEDIA_STORE`| `boolean`                                 | Indicate if `MediaStore` should be notified about newly-captured photo (default is `false`)|

Note that `to()` is **required**.

Note that if you are going to use `quality()`, `sizeLimit()`, or
`durationLimit()`, you need to call
those first on the `IntentBuilder` before any of the others.
This limitation will be lifted (hopefully) [in the future](https://github.com/commonsguy/cwac-cam2/issues/69).

Also note that `mirrorPreview()` mirrors the preview based on the
orientation when the activity instance was created. Since the
library will recreate the activity on a configuration change,
things look "normal" when the device is not being actively rotated.
However, when the device is part-way through the rotation, before
the configuration change kicks in, the mirroring effect starts
becoming more of a vertical flip rather than a horizontal one.
In short: the image will look upside-down briefly.

If you call `onError()` and provide a `ResultReceiver`, it will
be called with `onReceiveResult()` if there is some error inside
of the library. The `resultCode` will be one of the `ERROR_*`
values defined on `ErrorConstants`. The `Bundle` will contain
a stack trace in a `String` value keyed by `ErrorConstants.RESULT_STACK_TRACE`.

## Example Use of `IntentBuilder`

```java
  Intent i=new VideoRecorderActivity.IntentBuilder(MainActivity.this)
      .quality(Quality.LOW)
      .facing(VideoRecorderActivity.Facing.FRONT)
      .to(new File(testRoot, "test.mp4"))
      .debug()
      .updateMediaStore()
      .build();

  startActivityForResult(i, MAKIN_MOVIES);
```

## `buildChooser()`

In addition to `build()`, `IntentBuilder` supports `buildChooser()`.
This will return an `Intent` that will bring up an activity chooser,
where the user can choose between this library's video-recording activity
or existing `ACTION_VIDEO_CAPTURE` implementations. This way, the
user gets the choice of what should be used to record the video.

`buildChooser()` takes a `CharSequence` parameter, for a title
to go over the chooser dialog. `null` means do not use a title.

## Output

The video will be written to the `File` that you supply to `to()`,
and the `Uri` of the `Intent` delivered to `onActivityResult()` will point
to that file.

And, of course, the `resultCode` passed to `onActivityResult()` will indicate if the user took a picture or abandoned the operation.

## Configuring the Manifest Entry

Getting all of the above working requires nothing in your manifest.
However, more often than not, you will want to change aspects of the
activity, such as its theme.

To do that, add your own `<activity>` element to the manifest, pointing
to the `VideoRecorderActivity` class, and add in whatever attributes or child
elements that you need.

For example, the following manifest entry sets the theme:

```xml
<activity
      android:name="com.commonsware.cwac.cam2.VideoRecorderActivity"
      android:theme="@style/AppTheme"/>
```

Note that `VideoRecorderActivity` does not support being exported. Do not add
an `<intent-filter>` to this activity or otherwise mark it as being
exported.

`VideoRecorderActivity` supports running in a separate process, via
the `android:process` attribute. This ensures that the heap space
consumed in all the camera processing will not affect your main
process' heap space. It does mean that you will consume more system
RAM while the user is taking a picture, and it does incrementally
slow down the launching of the `VideoRecorderActivity`. You can see this
use of `android:process` demonstrated in the `demo-playground/`
sample project.

## Permissions

This library uses certain permissions. You may wish to review
[the documentation on permissions](Permissions.md) to learn
more about what they are and how to manage them.
