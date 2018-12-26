# NickChess

This is a simple implementation of a chess algorithm using alpha-beta pruning technique for Android Devices.

![NickChess screenshot](http://nikitapestin.com/icons/NickChessScreen.png)

## Requirements

* [Android SDK](https://developer.android.com/studio/)
* Android Lollipop 5.0 (API Level 21)

## Usage

* Import the Android project into Android SDK.
* Build the project.

## Restrictions

You can play only white for now.

## Hints

You can change efficiency of algorithm by changing MAX_DEPTH value in file:
```
app/src/main/java/com/nikitapestin/nickchess/GameSearch.java
```
This constant controls number of moves the algorithm looks forward. Higher values increase difficulty, but give a huge performance hit.

## License

Released under the [MIT License](http://www.opensource.org/licenses/mit-license.php).