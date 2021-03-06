/***
 * Copyright (c) 2015-2016 CommonsWare, LLC
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you
 * may
 * not use this file except in compliance with the License. You may
 * obtain
 * a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 * See the License for the specific language governing permissions
 * and
 * limitations under the License.
 */

package com.commonsware.cwac.cam2;

import android.app.ActionBar;
import android.app.Fragment;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import androidx.core.content.ContextCompat;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;

import me.everything.android.ui.overscroll.OverScrollDecoratorHelper;

import static com.commonsware.cwac.cam2.AbstractCameraActivity.EXTRA_FACING;

/**
 * Fragment for displaying a camera preview, with hooks to allow
 * you (or the user) to take a picture.
 */
public class CameraFragment extends Fragment
  implements ReverseChronometer.Listener {
  protected static final String ARG_OUTPUT = "output";
  protected static final String ARG_UPDATE_MEDIA_STORE = "updateMediaStore";
  protected static final String ARG_SKIP_ORIENTATION_NORMALIZATION = "skipOrientationNormalization";
  protected static final String ARG_IS_VIDEO = "isVideoFragment";
  protected static final String ARG_QUALITY = "quality";
  protected static final String ARG_SIZE_LIMIT = "sizeLimit";
  protected static final String ARG_DURATION_LIMIT = "durationLimit";
  protected static final String ARG_ZOOM_STYLE = "zoomStyle";
  protected static final String ARG_FACING_EXACT_MATCH = "facingExactMatch";
  protected static final String ARG_CHRONOTYPE = "chronotype";
  protected static final String ARG_RULE_OF_THIRDS = "ruleOfThirds";
  protected static final String ARG_TIMER_DURATION = "timerDuration";

  private static final int PINCH_ZOOM_DELTA = 20;
  protected CameraController ctlr;
  private FrameLayout mCaptureEffectOverlayLayout;
  private ViewGroup previewStack;
  private ImageView mCameraBtn;
  private AppCompatImageView imgSwitchFacing;
  private FlashMode mCurrentFlashMode;
  private AppCompatImageView imgFlash;
  private RecyclerView mCameraModeSwitcherRV;
  RecyclerView.LayoutManager mLayoutManager;
  private CameraModeSwitcherAdapter mAdapter;
  public AppCompatImageView mBackBtn;
  private View progress;
  private boolean isVideoRecording = false;
  private boolean mirrorPreview = false;
  private ScaleGestureDetector scaleDetector;
  private boolean inSmoothPinchZoom = false;
  private SeekBar zoomSlider;
  private View zoomSliderLayout;
  private Chronometer chronometer;
  private ReverseChronometer reverseChronometer;

  // At default we are gonna take a picture
  private boolean mIsVideoCameraSelected = false;

  /**
   * Event raised when the camera has been opened.
   * Subscribe to this event if you use open()
   * to to find out when the open has succeeded.
   * May include an exception if there was
   * an exception accessing the camera.
   */
  public static class CameraModeChanged {
    public final boolean shouldChangeToPictureMode;
    public final boolean isFacingBack;

    public CameraModeChanged(boolean shouldChangeToPictureMode, boolean isFacingBack) {
      this.shouldChangeToPictureMode = shouldChangeToPictureMode;
      this.isFacingBack = isFacingBack;
    }
  }

  public static CameraFragment newPictureInstance(Uri output,
                                                  boolean updateMediaStore,
                                                  int quality,
                                                  ZoomStyle zoomStyle,
                                                  boolean facingExactMatch,
                                                  boolean skipOrientationNormalization,
                                                  int timerDuration,
                                                  boolean ruleOfThirds) {
    CameraFragment f = new CameraFragment();
    Bundle args = new Bundle();

    args.putParcelable(ARG_OUTPUT, output);
    args.putBoolean(ARG_UPDATE_MEDIA_STORE, updateMediaStore);
    args.putBoolean(ARG_SKIP_ORIENTATION_NORMALIZATION,
            skipOrientationNormalization);
    args.putInt(ARG_QUALITY, quality);
    args.putBoolean(ARG_IS_VIDEO, false);
    args.putSerializable(ARG_ZOOM_STYLE, zoomStyle);
    args.putBoolean(ARG_FACING_EXACT_MATCH, facingExactMatch);
    args.putInt(ARG_TIMER_DURATION, timerDuration);
    args.putBoolean(ARG_RULE_OF_THIRDS, ruleOfThirds);
    f.setArguments(args);

    return (f);
  }

  public static CameraFragment newVideoInstance(Uri output,
                                                boolean updateMediaStore,
                                                int quality,
                                                int sizeLimit,
                                                int durationLimit,
                                                ZoomStyle zoomStyle,
                                                boolean facingExactMatch,
                                                ChronoType chronoType,
                                                boolean ruleOfThirds) {
    CameraFragment f = new CameraFragment();
    Bundle args = new Bundle();

    args.putParcelable(ARG_OUTPUT, output);
    args.putBoolean(ARG_UPDATE_MEDIA_STORE, updateMediaStore);
    args.putBoolean(ARG_IS_VIDEO, true);
    args.putInt(ARG_QUALITY, quality);
    args.putInt(ARG_SIZE_LIMIT, sizeLimit);
    args.putInt(ARG_DURATION_LIMIT, durationLimit);
    args.putSerializable(ARG_ZOOM_STYLE, zoomStyle);
    args.putBoolean(ARG_FACING_EXACT_MATCH, facingExactMatch);
    args.putBoolean(ARG_RULE_OF_THIRDS, ruleOfThirds);

    if (durationLimit > 0 || chronoType != ChronoType.COUNT_DOWN) {
      args.putSerializable(ARG_CHRONOTYPE, chronoType);
    }

    f.setArguments(args);

    return (f);
  }

  /**
   * Standard fragment entry point.
   *
   * @param savedInstanceState State of a previous instance
   */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setRetainInstance(true);
    scaleDetector =
            new ScaleGestureDetector(getActivity().getApplicationContext(),
                    scaleListener);
  }

  /**
   * Standard lifecycle method, passed along to the CameraController.
   */
  @Override
  public void onStart() {
    super.onStart();

    AbstractCameraActivity.BUS.register(this);

    if (ctlr != null) {
      ctlr.start();
    }
  }

  @Override
  public void onHiddenChanged(boolean isHidden) {
    super.onHiddenChanged(isHidden);

    if (!isHidden) {
      ActionBar ab = getActivity().getActionBar();

      if (ab != null) {
        ab.setBackgroundDrawable(getActivity()
                .getResources()
                .getDrawable(
                        R.drawable.cwac_cam2_action_bar_bg_transparent));
        ab.setTitle("");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
          ab.setDisplayHomeAsUpEnabled(false);
        } else {
          ab.setDisplayShowHomeEnabled(false);
          ab.setHomeButtonEnabled(false);
        }
      }

      if (mCameraBtn != null) {
        // Start a "capture" effect
        mCaptureEffectOverlayLayout.setVisibility(View.VISIBLE);
        mCaptureEffectOverlayLayout.postDelayed(new Runnable() {
          @Override
          public void run() {
            mCaptureEffectOverlayLayout.setVisibility(View.GONE);
          }
        }, 60);

        mCameraBtn.setClickable(true);
        imgSwitchFacing.setClickable(canSwitchSources());
      }
    }
  }

  /**
   * Standard lifecycle method, for when the fragment moves into
   * the stopped state. Passed along to the CameraController.
   */
  @Override
  public void onStop() {
    stopChronometers();

    if (ctlr != null) {
      try {
        ctlr.stop();
      } catch (Exception e) {
        ctlr.postError(ErrorConstants.ERROR_STOPPING, e);
        Log.e(getClass().getSimpleName(),
                "Exception stopping controller", e);
      }
    }

    AbstractCameraActivity.BUS.unregister(this);

    super.onStop();
  }

  /**
   * Standard lifecycle method, for when the fragment is utterly,
   * ruthlessly destroyed. Passed along to the CameraController,
   * because why should the fragment have all the fun?
   */
  @Override
  public void onDestroy() {
    if (ctlr != null) {
      ctlr.destroy();
    }

    super.onDestroy();
  }

  private int prevCenterPos;

  /**
   * Standard callback method to create the UI managed by
   * this fragment.
   *
   * @param inflater           Used to inflate layouts
   * @param container          Parent of the fragment's UI (eventually)
   * @param savedInstanceState State of a previous instance
   * @return the UI being managed by this fragment
   */
  @Override
  public View onCreateView(LayoutInflater inflater,
                           ViewGroup container,
                           Bundle savedInstanceState) {
    View v =
            inflater.inflate(R.layout.cwac_cam2_fragment, container, false);

    mCaptureEffectOverlayLayout = v.findViewById(R.id.capture_effect_overlay_layout);
    previewStack =
            v.findViewById(R.id.cwac_cam2_preview_stack);

    progress = v.findViewById(R.id.cwac_cam2_progress);
    mCameraBtn =
            v.findViewById(R.id.cwac_cam2_picture_btn);
    imgSwitchFacing =
            v.findViewById(R.id.cwac_cam2_switch_camera_btn);
    imgFlash =
            v.findViewById(R.id.cwac_cam2_flash_btn);
    chronometer = v.findViewById(R.id.chrono);
    // Works only when setting the text dynamically
    chronometer.setText("00:00:00");
    // Recycler view used to switch camera types (i.e: photo or video)
    mCameraModeSwitcherRV =
            v.findViewById(R.id.camera_mode_switcher_rv);
    mLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
    mCameraModeSwitcherRV.setLayoutManager(mLayoutManager);
      // Horizontal
      OverScrollDecoratorHelper.setUpOverScroll(mCameraModeSwitcherRV, OverScrollDecoratorHelper.ORIENTATION_HORIZONTAL);
    final SnapHelper snapHelper = new LinearSnapHelper();
    snapHelper.attachToRecyclerView(mCameraModeSwitcherRV);

    mAdapter = new CameraModeSwitcherAdapter();
    mAdapter.setCameraModeSwitcherViewClickListener(new CameraModeSwitcherAdapter.CameraModeSwitcherViewClickListener() {
      @Override
      public void onClick(int position) {
//          View view = mCameraModeSwitcherRV.getLayoutManager().findViewByPosition(position);
//          int middle = mCameraModeSwitcherRV.getWidth() / 2;
//        mCameraModeSwitcherRV.getLayoutManager().smoothScrollToPosition(mCameraModeSwitcherRV, null, position);
//        mCameraModeSwitcherRV.scrollBy(middle, 0);
          // Override smoothscrollToPosition to be slower (LinearLayoutManager)
          mCameraModeSwitcherRV.smoothScrollToPosition(position);
//          mCameraModeSwitcherRV.smoothScrollBy(view.getLeft() - middle, 0, new AccelerateDecelerateInterpolator());
//          if (position == 0) {
//              // Changing to PICTURE mode
//              ctlr.getEngine().getBus().post(new CameraModeChanged(true));
//          } else if (position == 1) {
//              // Changing to VIDEO mode
//              ctlr.getEngine().getBus().post(new CameraModeChanged(false));
//          }
      }
    });
    mCameraModeSwitcherRV.post(new Runnable() {
      @Override
      public void run() {
        mCameraModeSwitcherRV.addItemDecoration(new PaddingFirstItemDecoration(mCameraModeSwitcherRV.getWidth() / 2));
      }
    });
    mCameraModeSwitcherRV.setAdapter(mAdapter);
    mCameraModeSwitcherRV.setHasFixedSize(true);
    mCameraModeSwitcherRV.setNestedScrollingEnabled(false);
    mCameraModeSwitcherRV.addOnScrollListener(new RecyclerView.OnScrollListener() {

        // Seems that there's a bug that the onScrolled is called on startup of the recyclerview and the snapView is not the center one
        // so we'll have to make a switch of the positions
        boolean isFirstTimeCalled = true;
      @Override
      public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
          super.onScrolled(recyclerView, dx, dy);

//      if (scrolledPosition == 0) {
//        // Changing to PICTURE mode
//        ctlr.getEngine().getBus().post(new CameraModeChanged(true));
//      } else if (scrolledPosition == 1) {
//        // Changing to VIDEO mode
//        ctlr.getEngine().getBus().post(new CameraModeChanged(false));
//      }

          // TODO: change the button drawable to video type and vice versa

          // NOTE: For some reason it seems as that the onScrolled event is called on the startup of the recyclerview, and the snapView is pretty fucked up
          // at that time, so we explicitly grey the other item
          if (isFirstTimeCalled) {
              // gray the non centered view (pretty shit code)
              View otherView = mCameraModeSwitcherRV.getLayoutManager().findViewByPosition(1);
              TextView txtView = otherView.findViewById(R.id.camera_mode_txt);
              txtView.setTextColor(ContextCompat.getColor(getActivity(), R.color.cwac_cam2_text_light_disabled));
              isFirstTimeCalled = false;
          }
          else {
              View centerView = snapHelper.findSnapView(mLayoutManager);
              int centerPos = mCameraModeSwitcherRV.getChildAdapterPosition(centerView);

              if (prevCenterPos != centerPos) {
                  // dehighlight the previously highlighted view
                  View prevView = mCameraModeSwitcherRV.getLayoutManager().findViewByPosition(prevCenterPos);
                  if (prevView != null) {
                      TextView txtView = prevView.findViewById(R.id.camera_mode_txt);
                      txtView.setTextColor(ContextCompat.getColor(getActivity(), R.color.cwac_cam2_text_light_disabled));
                  }

                  // highlight view in the middle
                  if (centerView != null) {
                      TextView txtView = centerView.findViewById(R.id.camera_mode_txt);
                      txtView.setTextColor(ContextCompat.getColor(getActivity(), R.color.cwac_cam2_text_light_enabled));
                  }

                  prevCenterPos = centerPos;

                if (centerPos == 0) {
                    mCameraBtn.setImageResource(R.drawable.camera_pic_effect);
                    chronometer.setVisibility(View.INVISIBLE);
                    // Reset to gonna take a picture
                    mIsVideoCameraSelected = false;
                } else if (centerPos == 1) {
                    mCameraBtn.setImageResource(R.drawable.camera_vid_effect);
                    mIsVideoCameraSelected = true;
                    chronometer.setVisibility(View.VISIBLE);
                }
              }
          }

      }
    });

    // set current default flash mode to off
    mCurrentFlashMode = FlashMode.OFF;
    mBackBtn =
            v.findViewById(R.id.back_imgview);

    reverseChronometer =
            v.findViewById(R.id.rchrono);


    if (isVideoFragment()) {
      mIsVideoCameraSelected = true;
      mCameraBtn.setImageResource(R.drawable.camera_vid_effect);
      mCameraModeSwitcherRV.setVisibility(View.INVISIBLE);
    }
//    else {
//      fabVideo.setOnClickListener(new View.OnClickListener() {
//        @Override
//        public void onClick(View view) {
//          mCameraBtn.setClickable(false);
//          imgSwitchFacing.setEnabled(false);
//          ctlr.getEngine().getBus().post(new CameraModeChanged(false));
//        }
//      });
//    }

    mCameraBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        performCameraAction();
      }
    });
    imgSwitchFacing.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        progress.setVisibility(View.VISIBLE);
        imgSwitchFacing.setClickable(false);

        try {
          // Flip the facing, necessary because we access EXTRA_FACING to detect camera facing in runtime
          Facing facing = (Facing) getActivity().getIntent().getSerializableExtra(EXTRA_FACING);
          Facing newFacing;
          if (facing.isFront()) {
              newFacing = Facing.BACK;
          } else {
              newFacing = Facing.FRONT;
          }
          getActivity().getIntent().putExtra(EXTRA_FACING, newFacing);
          ctlr.switchCamera();
        } catch (Exception e) {
          ctlr.postError(ErrorConstants.ERROR_SWITCHING_CAMERAS, e);
          Log.e(getClass().getSimpleName(),
                  "Exception switching camera", e);
        }
      }
    });

    imgFlash.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        // TODO: set relevant flash image on each state
        // This is circular switch case
        switch (mCurrentFlashMode) {
          // This is the first default state

          case OFF:
            mCurrentFlashMode = FlashMode.AUTO;
            imgFlash.setImageResource(R.drawable.ic_noun_flash_auto);
            break;
          case AUTO:
            mCurrentFlashMode = FlashMode.ALWAYS;
            imgFlash.setImageResource(R.drawable.ic_noun_flash_on);
            break;
          case ALWAYS:
            mCurrentFlashMode = FlashMode.TORCH;
            imgFlash.setImageResource(R.drawable.ic_noun_torch);
            break;
          case TORCH:
            mCurrentFlashMode = FlashMode.OFF;
            imgFlash.setImageResource(R.drawable.ic_noun_flash_off);
            break;
        }

        try {
          ctlr.stop();
        } catch (Exception e) {
          e.printStackTrace();
        }
        ArrayList<FlashMode> newFlashMode = new ArrayList<>();
        newFlashMode.add(mCurrentFlashMode); // This is the flash mode you want to change to
        getController().getEngine().setPreferredFlashModes(newFlashMode);
        getController().start();
      }
    });
//    changeMenuIconAnimation(
//     (FloatingActionMenu)v.findViewById(R.id.cwac_cam2_settings));

    onHiddenChanged(false); // hack, since this does not get
    // called on initial display

    mCameraBtn.setClickable(false);
    imgSwitchFacing.setClickable(false);

    if (ctlr != null && ctlr.getNumberOfCameras() > 0) {
      prepController();
    }

    if (showRuleOfThirds()) {
      v.findViewById(R.id.rule_of_thirds).setVisibility(View.VISIBLE);
    }

    return (v);
  }

  @Override
  public void onCountdownCompleted() {
    takePicture();
  }

  public void shutdown() {
    if (isVideoRecording) {
      stopVideoRecording(true);
    } else {
      progress.setVisibility(View.VISIBLE);

      if (ctlr != null) {
        try {
          ctlr.stop();
        } catch (Exception e) {
          ctlr.postError(ErrorConstants.ERROR_STOPPING, e);
          Log.e(getClass().getSimpleName(),
                  "Exception stopping controller", e);
        }
      }
    }
  }

  /**
   * @return the CameraController this fragment delegates to
   */
  public CameraController getController() {
    return (ctlr);
  }

  /**
   * Establishes the controller that this fragment delegates to
   *
   * @param ctlr the controller that this fragment delegates to
   */
  public void setController(CameraController ctlr) {
    int currentCamera = -1;

    if (this.ctlr != null) {
      currentCamera = this.ctlr.getCurrentCamera();
    }

    this.ctlr = ctlr;
    ctlr.setQuality(getArguments().getInt(ARG_QUALITY, 1));

    if (currentCamera > -1) {
      ctlr.setCurrentCamera(currentCamera);
    }
  }

  /**
   * Indicates if we should mirror the preview or not. Defaults
   * to false.
   *
   * @param mirror true if we should horizontally mirror the
   *               preview, false otherwise
   */
  public void setMirrorPreview(boolean mirror) {
    this.mirrorPreview = mirror;
  }

  @SuppressWarnings("unused")
  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onEventMainThread(
          CameraController.ControllerReadyEvent event) {
    if (event.isEventForController(ctlr)) {
      prepController();
    }
  }

  @SuppressWarnings("unused")
  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onEventMainThread(CameraEngine.OpenedEvent event) {
    if (event.exception == null) {
      progress.setVisibility(View.GONE);
      imgSwitchFacing.setClickable(canSwitchSources());
      mCameraBtn.setClickable(true);
      zoomSlider = getView().findViewById(R.id.cwac_cam2_zoom);
      zoomSliderLayout = getView().findViewById(R.id.cwac_cam2_zoom_layout);
//      zoomSlider.getProgressDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
//      zoomSlider.getThumb().setColorFilter(Color.YELLOW, PorterDuff.Mode.SRC_IN);

      int timerDuration = getArguments().getInt(ARG_TIMER_DURATION);

      if (timerDuration > 0) {
        reverseChronometer.setVisibility(View.VISIBLE);
        reverseChronometer.setOverallDuration(timerDuration);
        reverseChronometer.setListener(this);
        reverseChronometer.reset();
        reverseChronometer.run();
      }

      if (ctlr.supportsZoom()) {

        // Enable pinch and slider zoom by default
        previewStack.setOnTouchListener(
                new View.OnTouchListener() {
                  @Override
                  public boolean onTouch(View v, MotionEvent event) {
                    return (scaleDetector.onTouchEvent(event));
                  }
                });
        zoomSlider.setVisibility(View.VISIBLE);
        zoomSlider.setOnSeekBarChangeListener(seekListener);
      } else {
        previewStack.setOnTouchListener(null);
        zoomSlider.setVisibility(View.GONE);
      }

      if (isVideoFragment()) {
          // NOTE: If its a video we start recording automatically @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
        performCameraAction();
      }
    } else {
      ctlr.postError(ErrorConstants.ERROR_OPEN_CAMERA,
              event.exception);
      getActivity().finish();
    }
  }

  @SuppressWarnings("unused")
  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onEventMainThread(
          CameraEngine.SmoothZoomCompletedEvent event) {
    inSmoothPinchZoom = false;
    zoomSlider.setEnabled(true);
    zoomSlider.setProgress(ctlr.getZoomLevel());
  }

  protected void performCameraAction() {
    if (mIsVideoCameraSelected) {
        if (isVideoFragment()) {
          toggleVideoRecording();
        } else {
          // This would start recording video automatically
          ctlr.getEngine().getBus().post(new CameraModeChanged(false, isCameraFacingBack()));
        }
    } else {
      // This is just for sanity, because we always at PICTURE mode fragment, unless we're in the middle of recording a video
      if (!isVideoFragment()) {
        takePicture();
      }
    }
  }

  /**
   * Checks whether the camera is facing back or front, There's no easy way to determine it via any cwac-cam2 object
   * (CameraController, CameraEngine, etc..) and no, getCameraNumber is not the integer of the camera-id, but something cam2 based
   */
  public boolean isCameraFacingBack() {
      Facing facing = (Facing) getActivity().getIntent().getSerializableExtra(EXTRA_FACING);
      return !facing.isFront();
  }

  private void takePicture() {
    PictureTransaction.Builder b = new PictureTransaction.Builder();
    Uri output = getArguments().getParcelable(ARG_OUTPUT);

    if (output != null) {
      b.toUri(getActivity(), output,
              getArguments().getBoolean(ARG_UPDATE_MEDIA_STORE, false),
              getArguments().getBoolean(ARG_SKIP_ORIENTATION_NORMALIZATION,
                      false));
    }
    mCameraBtn.setClickable(false);
    imgSwitchFacing.setClickable(false);
    ctlr.takePicture(b.build());
  }

  private void toggleVideoRecording() {
     if (isVideoRecording) {
        stopVideoRecording(false);
     } else {
       recordVideo();
     }
  }

  private void recordVideo() {
    try {
      VideoTransaction.Builder b =
              new VideoTransaction.Builder();
      Uri output = getArguments().getParcelable(ARG_OUTPUT);

      b.to(new File(output.getPath()))
              .quality(getArguments().getInt(ARG_QUALITY, 1))
              .sizeLimit(getArguments().getInt(ARG_SIZE_LIMIT, 0))
              .durationLimit(
                      getArguments().getInt(ARG_DURATION_LIMIT, 0));

      ctlr.recordVideo(b.build());
      mCameraBtn.setImageResource(R.drawable.camera_vid_recording_effect);
      isVideoRecording = true;
//        mCameraBtn.setImageResource(
//          R.drawable.cwac_cam2_ic_stop);
//        mCameraBtn.setColorNormalResId(
//          R.color.cwac_cam2_video_fab);
//        mCameraBtn.setColorPressedResId(
//          R.color.cwac_cam2_video_fab_pressed);
      imgSwitchFacing.setVisibility(View.INVISIBLE);
      imgFlash.setVisibility(View.INVISIBLE);
      mCameraModeSwitcherRV.setVisibility(View.INVISIBLE);
      mBackBtn.setVisibility(View.INVISIBLE);
      configureChronometer();
    } catch (Exception e) {
      Log.e(getClass().getSimpleName(),
              "Exception recording video", e);
      // TODO: um, do something here and return to last state.. to test and go into this exception, do not put output uri
    }
  }

  public void stopVideoRecording() {
    stopVideoRecording(true);
  }

  private void stopVideoRecording(boolean abandon) {
    setVideoFABToNormal();

    try {
      ctlr.stopVideoRecording(abandon);
    } catch (Exception e) {
      ctlr.postError(ErrorConstants.ERROR_STOPPING_VIDEO, e);
      Log.e(getClass().getSimpleName(),
              "Exception stopping recording of video", e);
    } finally {
      isVideoRecording = false;
    }
  }

  private void setVideoFABToNormal() {
    mCameraBtn.setImageResource(
      R.drawable.camera_vid_effect);
    chronometer.stop();
//    mCameraBtn.setColorNormalResId(
//      R.color.cwac_cam2_picture_fab);
//    mCameraBtn.setColorPressedResId(
//      R.color.cwac_cam2_picture_fab_pressed);
//    imgSwitchFacing.setEnabled(canSwitchSources());
  }

  private boolean canSwitchSources() {
    return (!getArguments().getBoolean(ARG_FACING_EXACT_MATCH,
            false));
  }

  protected boolean isVideoFragment() {
    return (getArguments().getBoolean(ARG_IS_VIDEO, false));
  }

  private boolean showRuleOfThirds() {
    return (getArguments().getBoolean(ARG_RULE_OF_THIRDS, false));
  }

  private ChronoType getChronoType() {
    ChronoType chronoType =
            (ChronoType) getArguments().getSerializable(ARG_CHRONOTYPE);

    if (chronoType == null) {
      chronoType = ChronoType.NONE;
    }

    return (chronoType);
  }

  private void configureChronometer() {
    chronometer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener(){
      @Override
      public void onChronometerTick(Chronometer cArg) {
        long time = SystemClock.elapsedRealtime() - cArg.getBase();
        int h   = (int)(time /3600000);
        int m = (int)(time - h*3600000)/60000;
        int s= (int)(time - h*3600000- m*60000)/1000 ;
        String hh = h < 10 ? "0"+h: h+"";
        String mm = m < 10 ? "0"+m: m+"";
        String ss = s < 10 ? "0"+s: s+"";
        cArg.setText(hh+":"+mm+":"+ss);
      }
    });
    chronometer.setBase(SystemClock.elapsedRealtime());

    if (getChronoType() == ChronoType.COUNT_UP) {
      chronometer.setVisibility(View.VISIBLE);
      chronometer.start();
    } else if (getChronoType() == ChronoType.COUNT_DOWN) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        chronometer.setVisibility(View.VISIBLE);
        chronometer.setBase(SystemClock.elapsedRealtime() +
                getArguments().getInt(ARG_DURATION_LIMIT, 0));
        chronometer.setCountDown(true);
        chronometer.start();
      } else {
        reverseChronometer.setVisibility(View.VISIBLE);
        reverseChronometer
                .setOverallDuration(getArguments()
                        .getInt(ARG_DURATION_LIMIT, 0) / 1000);
        reverseChronometer.reset();
        reverseChronometer.run();
      }
    }
  }

  private void stopChronometers() {
    if (chronometer != null) {
      chronometer.stop();
    }

    if (reverseChronometer != null) {
      reverseChronometer.setListener(null);
      reverseChronometer.stop();
    }
  }

  private void prepController() {
    LinkedList<CameraView> cameraViews = new LinkedList<CameraView>();
    CameraView cv = (CameraView) previewStack.getChildAt(0);

    cv.setMirror(mirrorPreview);
    cameraViews.add(cv);

    for (int i = 1; i < ctlr.getNumberOfCameras(); i++) {
      cv = new CameraView(getActivity());
      cv.setVisibility(View.INVISIBLE);
      cv.setMirror(mirrorPreview);
      previewStack.addView(cv);
      cameraViews.add(cv);
    }

    ctlr.setCameraViews(cameraViews);
  }

  private int ZOOM_SLIDER_VISIBILITY_TIME_GONE_MS = 3000;


  private Runnable zoomSliderGoneVisibilityRunnable = new Runnable() {
    @Override
    public void run() {
      zoomSliderLayout.setVisibility(View.GONE);
    }
  };


  private ScaleGestureDetector.OnScaleGestureListener scaleListener =
          new ScaleGestureDetector.SimpleOnScaleGestureListener() {

            @Override
            public boolean onScale(ScaleGestureDetector detector) {
              zoomSliderLayout.setVisibility(View.VISIBLE);

              // Reset the countdown to the gone visibility timer
              zoomSliderLayout.removeCallbacks(zoomSliderGoneVisibilityRunnable);
              zoomSliderLayout.postDelayed(zoomSliderGoneVisibilityRunnable, ZOOM_SLIDER_VISIBILITY_TIME_GONE_MS);

              float scaleFactor = detector.getScaleFactor();
              if (!inSmoothPinchZoom) {
                if (ctlr.changeZoom(scaleFactor)) {
                  inSmoothPinchZoom = true;
                }
                zoomSlider.setProgress(ctlr.getZoomLevel());
              }
              return true;
            }
          };

  private SeekBar.OnSeekBarChangeListener seekListener =
          new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar,
                                          int progress,
                                          boolean fromUser) {
              if (fromUser) {
                zoomSliderLayout.setVisibility(View.VISIBLE);

                // Reset the countdown to the gone visibility timer
                zoomSliderLayout.removeCallbacks(zoomSliderGoneVisibilityRunnable);
                zoomSliderLayout.postDelayed(zoomSliderGoneVisibilityRunnable, ZOOM_SLIDER_VISIBILITY_TIME_GONE_MS);

                if (ctlr.setZoom(progress)) {
                  seekBar.setEnabled(false);
                }
              }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
              // no-op
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
              // no-op
            }
          };
}