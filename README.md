# IBM Watson Developer Cloud Android SDK [![Build Status](https://travis-ci.org/watson-developer-cloud/android-sdk.svg?branch=master)](https://travis-ci.org/watson-developer-cloud/android-sdk)

Android client library to assist with using the [Watson][wdc] services, a collection of REST
APIs and SDKs that use cognitive computing to solve complex problems.

## Table of Contents

  * [Installation](#installation)
    * [Gradle](#gradle)
  * [Usage](#usage)
  * [Service Credentials](#service-credentials)
    * [Getting the Credentials](#getting-the-credentials)
    * [Adding the Credentials](#adding-the-credentials)
  * [Questions](#questions)
  * [Examples](#examples)
  * [Testing](#testing)
  * [License](#license)
  * [Contributing](#contributing)

## Installation

##### Gradle

```gradle
'com.ibm.watson.developer_cloud:android-sdk:0.5.2'
```

##### AAR

Download the aar [here][aar].

-----
The minimum supported Android API level is 19. Now, you are ready to see some [examples](https://github.com/watson-developer-cloud/android-sdk/tree/master/example).

## Usage

The examples below assume that you already have service credentials. If not, you will have to create a service in [IBM Cloud][bluemix].

## Service Credentials

#### Getting the Credentials
1. Sign up for an [IBM Cloud account](https://console.bluemix.net/registration/).
1. Create an instance of the Watson service you want to use and get your credentials:
    - Go to the [IBM Cloud catalog](https://console.bluemix.net/catalog/?category=ai) page and select the service you want.
    - Log in to your IBM Cloud account.
    - Click **Create**.
    - Click **Show** to view the service credentials.
    - Copy the `apikey` value, or copy the `username` and `password` values if your service instance doesn't provide an `apikey`.
    - Copy the `url` value.

#### Adding the Credentials

Once you've followed the instructions above to get credentials, they should be added to the `example/res/values/credentials.xml` file shown below.

```xml
<resources>
  <!-- Paste Language Translator information here -->
  <string name="visual_recognition_iam_apikey"></string>
  <string name="visual_recognition_url"></string>

  <!-- Paste Speech to Text information here -->
  <string name="speech_text_iam_apikey"></string>
  <string name="speech_text_url"></string>

  <!-- Paste Text to Speech information here -->
  <string name="text_speech_iam_apikey"></string>
  <string name="text_speech_url"></string>
</resources>
```

## Questions

If you are having difficulties using the APIs or have a question about the IBM
Watson Services, please ask a question on
[dW Answers](https://developer.ibm.com/answers/questions/ask/?topics=watson)
or [Stack Overflow](http://stackoverflow.com/questions/ask?tags=ibm-watson).

You can also check out the [wiki][wiki] for some additional information.

## Examples

This SDK is built for use with the [Watson Java SDK][java-sdk].

The examples below are specific for Android as they use the Microphone and Speaker; for actual services refer to the [Java SDK][java-sdk]. You can use the provided example app as a model for your own Android app using Watson services.

### MicrophoneHelper

Provides simple microphone access within an activity.

```java
MicrophoneHelper microphoneHelper = new MicrophoneHelper(this);
```

The MicrophoneHelper object allows you to create new MicrophoneInputStream objects and close them. The MicrophoneInputStream class is a convenience class for creating an `InputStream` from device microphone. You can record raw PCM data or data encoded using the ogg codec.

```java
// record PCM data without encoding
MicrophoneInputStream myInputStream = microphoneHelper.getInputStream(false);

// record PCM data and encode it with the ogg codec
MicrophoneInputStream myOggStream = microphoneHelper.getInputStream(true);
```

An example using a Watson Developer Cloud service would look like

```java
speechService.recognizeUsingWebSocket(new MicrophoneInputStream(),
getRecognizeOptions(), new BaseRecognizeCallback() {
  @Override
  public void onTranscription(SpeechResults speechResults){
    String text = speechResults.getResults().get(0).getAlternatives().get(0).getTranscript();
    System.out.println(text);
  }

  @Override
  public void onError(Exception e) {
  }

  @Override public void onDisconnected() {
  }

});
```

Be sure to take a look at the example app to get a working example of putting these all together.

### StreamPlayer

Provides the ability to directly play an `InputStream`. **Note:** The `InputStream` must come from a PCM audio source. Examples include WAV files or Audio/L16.

```java
StreamPlayer player = new StreamPlayer();
player.playStream(yourInputStream);
```

Since this SDK is intended to be used with the Watson APIs, a typical use case for the `StreamPlayer` class is for playing the output of a Watson Text to Speech call. In that case, you can specify the type of audio file you'd like to receive from the service to ensure it will be output properly by your Android device.

```java
SynthesizeOptions synthesizeOptions = new SynthesizeOptions.Builder()
  .text("I love making Android apps")
  .accept(SynthesizeOptions.Accept.AUDIO_WAV) // specifying that we want a WAV file
  .build();
InputStream streamResult = textService.synthesize(synthesizeOptions).execute();

StreamPlayer player = new StreamPlayer();
player.playStream(streamResult); // should work like a charm
```

Another content type that works from the Text to Speech APIs is the Audio/L16 type. For this you need to specify the sample rate, and you can do so with the alternate version of the `playStream()` method. The default sample rate on the single-argument version is 22050.

```java
SynthesizeOptions synthesizeOptions = new SynthesizeOptions.Builder()
  .text("I love making Android apps")
  .accept("audio/l16;rate=8000") // specifying our content type and sample rate
  .build();
InputStream streamResult = textService.synthesize(synthesizeOptions).execute();

StreamPlayer player = new StreamPlayer();
player.playStream(streamResult, 8000); // passing in the sample rate
```

### CameraHelper

Provides simple camera access within an activity.

```java
CameraHelper cameraHelper = new CameraHelper(this);
cameraHelper.dispatchTakePictureIntent();

@Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (requestCode == CameraHelper.REQUEST_IMAGE_CAPTURE) {
      System.out.println(cameraHelper.getFile(resultCode));
    }
  }
```

### GalleryHelper

Like the CameraHelper, but allows for selection of images already on the device.

To open the gallery:

```java
GalleryHelper galleryHelper = new GalleryHelper(this);
galleryHelper.dispatchGalleryIntent();

@Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (requestCode == GalleryHelper.PICK_IMAGE_REQUEST) {
      System.out.println(galleryHelper.getFile(resultCode, data));
    }
  }
```

## Testing

Testing in this SDK is accomplished with [Espresso](https://google.github.io/android-testing-support-library/docs/espresso/).

To run the tests, in Android Studio:

Within the example package, right-click the androidTest/java folder and click Run 'All Tests'.

## Build + Test

Use [Gradle][] (version 1.x) to build and test the project you can use

Gradle:

  ```sh
  $ cd android-sdk
  $ gradle test # run tests
  ```

## Open Source @ IBM

Find more open source projects on the [IBM Github Page](http://ibm.github.io/)

## License

This library is licensed under Apache 2.0. Full license text is
available in [LICENSE](LICENSE).

## Contributing

See [CONTRIBUTING.md](.github/CONTRIBUTING.md).

[wdc]: https://www.ibm.com/watson/developer/
[java-sdk]: https://github.com/watson-developer-cloud/java-sdk
[bluemix]: https://console.bluemix.net
[Gradle]: http://www.gradle.org/
[OkHttp]: http://square.github.io/okhttp/
[gson]: https://github.com/google/gson
[releases]: https://github.com/watson-developer-cloud/android-sdk/releases
[wiki]: https://github.com/watson-developer-cloud/android-sdk/wiki

[aar]: https://github.com/watson-developer-cloud/android-sdk/releases/download/v0.5.2/library-release.aar
