package com.ibm.watson.developer_cloud.android.myapplication;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.MediaStore;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.intent.Intents;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.WindowManager;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.Intents.intending;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

/**
 * Created by blakeball on 9/29/16.
 */

@RunWith(AndroidJUnit4.class)
@LargeTest
public class CameraHelperTest {

  @Rule
  public ActivityTestRule<MainActivity> activityTestRule =
      new ActivityTestRule<MainActivity>(MainActivity.class);

  @Before
  public void unlockScreen() {
    final MainActivity activity = activityTestRule.getActivity();
    Runnable wakeUpDevice = new Runnable() {
      public void run() {
        activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
      }
    };
    activity.runOnUiThread(wakeUpDevice);
  }

  @Before
  public void initializeIntents() {
    Intents.init();
  }

  @After
  public void releaseIntents() {
    Intents.release();
  }

  @Test
  public void testCameraFlow() {
    Bitmap icon = BitmapFactory.decodeResource(
        InstrumentationRegistry.getTargetContext().getResources(),
        R.mipmap.ic_launcher);

    Intent resultData = new Intent();
    resultData.putExtra("data", icon);
    Instrumentation.ActivityResult result = new Instrumentation.ActivityResult(Activity.RESULT_OK, resultData);

    intending(hasAction(MediaStore.ACTION_IMAGE_CAPTURE)).respondWith(result);

    Espresso.onView(withId(R.id.camera_button)).perform(click());

    intended(hasAction(MediaStore.ACTION_IMAGE_CAPTURE));
  }

}
