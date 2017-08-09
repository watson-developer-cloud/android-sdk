/*
 * Copyright 2017 IBM Corp. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package com.ibm.watson.developer_cloud.android.library.camera;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.content.CursorLoader;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;

import static android.app.Activity.RESULT_OK;

/**
 * The Class GalleryHelper.
 */
public final class GalleryHelper {

  /**
   * The Constant PICK_IMAGE_REQUEST.
   */
  public static final int PICK_IMAGE_REQUEST = 1001;
  private final String TAG = GalleryHelper.class.getName();
  private Activity activity;

  /**
   * Provides convenience access to device gallery.
   *
   * @param activity the activity
   */
  public GalleryHelper(Activity activity) {
    this.activity = activity;
  }

  /**
   * Starts an activity to select a photo from the device's memory.
   */
  public void dispatchGalleryIntent() {
    Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
    if (galleryIntent.resolveActivity(activity.getPackageManager()) != null) {
      activity.startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST);
    }
  }

  /**
   * This method returns the file of an image selected in the photo gallery. It should be called within the
   * onActivityResult method of an Activity.
   *
   * @param resultCode Result code of a previous activity
   * @param data       Data returned from a previous activity
   * @return An image's file if successful, null otherwise
   */
  public File getFile(int resultCode, Intent data) {
    if (resultCode == RESULT_OK) {
      Uri targetUri = data.getData();
      return new File(getRealPathFromURI(targetUri));
    }
    Log.e(TAG, "Result Code was not OK");
    return null;
  }

  /**
   * This method returns a bitmap of an image selected in the photo gallery. It should be called within the
   * onActivityResult method of an Activity.
   *
   * @param resultCode Result code of a previous activity
   * @param data       Data returned from a previous activity
   * @return A bitmap image if successfully completed, null otherwise
   */
  public Bitmap getBitmap(int resultCode, Intent data) {
    if (resultCode == RESULT_OK) {
      Uri targetUri = data.getData();
      try {
        return BitmapFactory.decodeStream(activity.getContentResolver().openInputStream(targetUri));
      } catch (FileNotFoundException e) {
        Log.e(TAG, "File Not Found", e);
        return null;
      }
    }
    Log.e(TAG, "Result Code was not OK");
    return null;
  }

  /**
   * This method returns a path, given a content URI.
   *
   * @param contentUri URI of some image
   * @return A string path
   */
  public String getRealPathFromURI(Uri contentUri) {
    String[] proj = {MediaStore.Images.Media.DATA};
    CursorLoader cLoader = new CursorLoader(activity.getApplicationContext(), contentUri, proj, null, null, null);
    Cursor cursor = cLoader.loadInBackground();
    int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
    cursor.moveToFirst();
    return cursor.getString(column_index);
  }
}
