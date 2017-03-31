/***
 Copyright (c) 2015 CommonsWare, LLC

 Licensed under the Apache License, Version 2.0 (the "License"); you may
 not use this file except in compliance with the License. You may obtain
 a copy of the License at http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package com.commonsware.cwac.cam2.playground;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ResultReceiver;
import android.preference.PreferenceFragment;
import android.support.v4.content.FileProvider;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;
import com.commonsware.cwac.cam2.AbstractCameraActivity;
import com.commonsware.cwac.cam2.ChronoType;
import com.commonsware.cwac.cam2.Facing;
import com.commonsware.cwac.cam2.FocusMode;
import com.commonsware.cwac.cam2.OrientationLockMode;
import com.commonsware.cwac.cam2.VideoRecorderActivity;
import com.commonsware.cwac.cam2.ZoomStyle;
import java.io.File;

public class VideoFragment extends PreferenceFragment {
  private static final String AUTHORITY=
    BuildConfig.APPLICATION_ID+".provider";

  interface Contract {
    void takeVideo(Intent i);
    void setOutput(Uri uri);
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    addPreferencesFromResource(R.xml.prefs_video);
    setHasOptionsMenu(true);
  }

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);

    if (!(activity instanceof Contract)) {
      throw new IllegalStateException("Hosting activity does not implement Contract interface!");
    }
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    inflater.inflate(R.menu.actions, menu);
    menu.findItem(R.id.picture_activity).setVisible(true);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId()==R.id.take_picture) {
      takePicture();

      return(true);
    }
    else if (item.getItemId()==R.id.picture_activity) {
      startActivity(new Intent(getActivity(), PictureActivity.class)
        .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
    }

    return super.onOptionsItemSelected(item);
  }

  private void takePicture() {
    SharedPreferences prefs=getPreferenceManager().getSharedPreferences();
    VideoRecorderActivity.IntentBuilder b=new VideoRecorderActivity.IntentBuilder(getActivity());
    File f=new File(getActivity().getExternalFilesDir(null), "test.mp4");

    b.to(f);
    ((Contract)getActivity())
      .setOutput(FileProvider.getUriForFile(getActivity(), AUTHORITY, f));

    if (prefs.getBoolean("highQuality", false)) {
      b.quality(AbstractCameraActivity.Quality.HIGH);
    }
    else {
      b.quality(AbstractCameraActivity.Quality.LOW);
    }

    if (prefs.getBoolean("ffc", false)) {
      b.facing(Facing.FRONT);
    }
    else {
      b.facing(Facing.BACK);
    }

    if (prefs.getBoolean("exact_match", false)) {
      b.facingExactMatch();
    }

    if (prefs.getBoolean("debug", false)) {
      b.debug();
    }

    if (prefs.getBoolean("updateMediaStore", false)) {
      b.updateMediaStore();
    }

/*
    if (prefs.getBoolean("forceClassic", false)) {
      b.forceClassic();
    }
*/

    String durationLimit=prefs.getString("durationLimit", null);

    if (durationLimit!=null) {
      b.durationLimit(Integer.parseInt(durationLimit));
    }

    String sizeLimit=prefs.getString("sizeLimit", null);

    if (sizeLimit!=null) {
      b.sizeLimit(Integer.parseInt(sizeLimit));
    }

    if (prefs.getBoolean("mirrorPreview", false)) {
      b.mirrorPreview();
    }

    int rawFocusMode=Integer.valueOf(
      prefs.getString("focusMode", "-1"));

    switch (rawFocusMode) {
      case 0:
        b.focusMode(FocusMode.CONTINUOUS);
        break;
      case 1:
        b.focusMode(FocusMode.OFF);
        break;
      case 2:
        b.focusMode(FocusMode.EDOF);
        break;
      case 3:
        b.focusMode(FocusMode.MACRO);
        break;
    }

    int rawOrientationLock=
      Integer.valueOf(prefs.getString("olockMode", "0"));

    switch (rawOrientationLock) {
      case 1:
        b.orientationLockMode(OrientationLockMode.PORTRAIT);
        break;
      case 2:
        b.orientationLockMode(OrientationLockMode.LANDSCAPE);
        break;
    }

    int rawChronoType=Integer.valueOf(prefs.getString("chronoType", "-1"));

    switch (rawChronoType) {
      case 0:
        b.chronoType(ChronoType.NONE);
        break;
      case 1:
        b.chronoType(ChronoType.COUNT_DOWN);
        break;
      case 2:
        b.chronoType(ChronoType.COUNT_UP);
        break;
    }

    int rawZoomStyle=
      Integer.valueOf(prefs.getString("zoomStyle", "0"));

    switch (rawZoomStyle) {
      case 1:
        b.zoomStyle(ZoomStyle.PINCH);
        break;

      case 2:
        b.zoomStyle(ZoomStyle.SEEKBAR);
        break;
    }

    b.onError(new ErrorResultReceiver());

    if (prefs.getBoolean("requestPermissions", true)) {
      b.requestPermissions();
    }

    if (prefs.getBoolean("showRuleOfThirds", false)) {
      b.showRuleOfThirdsGrid();
    }

    Intent result;

    if (prefs.getBoolean("useChooser", false)) {
      result=b.buildChooser("Choose a picture-taking thingy");
    }
    else {
      result=b.build();
    }

    ((Contract)getActivity()).takeVideo(result);
  }

  @SuppressLint("ParcelCreator")
  private class ErrorResultReceiver extends ResultReceiver {
    public ErrorResultReceiver() {
      super(new Handler(Looper.getMainLooper()));
    }

    @Override
    protected void onReceiveResult(int resultCode,
                                   Bundle resultData) {
      super.onReceiveResult(resultCode, resultData);

      if (getActivity()!=null) {
        Toast
          .makeText(getActivity(), "We had an error",
            Toast.LENGTH_LONG)
          .show();
      }
    }
  }
}
