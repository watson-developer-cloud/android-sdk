package com.ibm.watson.developer_cloud.android.myapplication;

import android.provider.MediaStore;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.intent.Intents;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.WindowManager;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.intent.Intents.intending;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

/**
 * Created by blakeball on 9/29/16.
 */

@RunWith(AndroidJUnit4.class)
public class CameraHelperTest {

  @Rule
  public ActivityTestRule<MainActivity> activityTestRule =
      new ActivityTestRule<>(MainActivity.class);

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

  @Test public void testCameraIsOpenedOnIntent() {

    Intents.init();

    Espresso.onView(withId(R.id.camera_button)).perform(click());
    Espresso.closeSoftKeyboard();
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    intending(hasComponent(String.valueOf(hasAction(MediaStore.ACTION_IMAGE_CAPTURE))));
    Intents.release();

  }

}
