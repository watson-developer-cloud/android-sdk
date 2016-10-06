package com.ibm.watson.developer_cloud.android.myapplication;

import android.provider.MediaStore;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.intent.Intents;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
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

  @Test public void testCameraIsOpenedOnIntent() {

    Intents.init();

    Espresso.onView(withId(R.id.camera_button)).perform(click());

    intending(hasComponent(String.valueOf(hasAction(MediaStore.ACTION_IMAGE_CAPTURE))));
    Intents.release();

  }

}
