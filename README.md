# Watson Developer Cloud Android SDK
The Watson Android SDK provies utilites to help integrate Watson Developer Cloud services into Android apps.

##Microphone Input Stream
Convience function for creating an `InputStream` from device microphone

```java
InputStream myInputStream = new MicrophoneInputStream();
```

An example using a Watson Developer Cloud service would look like

```java
speechService.recognizeUsingWebSocket(new MicrophoneInputStream(),
getRecognizeOptions(), new BaseRecognizeCallback() {

@Override public void onTranscription(SpeechResults speechResults){
  String text = speechResults.getResults().get(0).getAlternatives().get(0).getTranscript();
  System.out.println(text);
}

@Override public void onError(Exception e) {
  // Handle exception
}

@Override public void onDisconnected() {
  
}

});
```

##StreamPlayer
Provides the ability to directly play an InputStream
```java
StreamPlayer player = new StreamPlayer();
player.playStream(yourInputStream);
```

## License

This library is licensed under Apache 2.0. Full license text is
available in [LICENSE](LICENSE).
