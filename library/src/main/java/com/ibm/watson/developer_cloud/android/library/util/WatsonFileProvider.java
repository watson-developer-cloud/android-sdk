package com.ibm.watson.developer_cloud.android.library.util;

import android.support.v4.content.FileProvider;

/**
 * Trivial subclass of FileProvider to avoid provider authority collisions.
 * See https://commonsware.com/blog/2017/06/27/fileprovider-libraries.html for more info.
 */
public class WatsonFileProvider extends FileProvider { }
