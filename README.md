# Tombs of Amascut

Utilities and information for raiding the Tombs of Amascut.

**Note: While Tombs of Amascut is new, Jagex will be quite strict on what is and is not allowed in ToA plugins.**
Feel free to request new features in the Issues tab (after searching for an existing matching issue),
or follow a discussion there on whether a feature should be implemented.

### Current Features

#### Invocation Presets

Right-click the Preset button to create/load/import/export presets.
Invocations that need to be changed will be highlighted green/red.
Shift+right-click to delete presets.

![](docs/invocation-presets.gif)

#### Deposit-pickaxe Swap

While holding a pickaxe, swaps the left-click option to "Deposit-pickaxe"
on the statue in the mirror puzzle room.

![](docs/deposit-pickaxe.gif)

#### Invocation Screenshot
Adds a button to ToA Invocation Interface that when clicked will copy all of your invocations as a picture to your system clipboard.
This image will also include the rewards section if it's enabled via the config options and the button is selected within the in-game interface.

![](docs/screenshot-button.png)

[Click here to see an example of the screenshot that will be created](docs/screenshot-example)

#### Chest Audio
This will play a `.wav` audio file from your local machine whenever the Tombs of Amascut Sarcophagus is opened (also known as the purple chest).

##### Setup
1. Locate your `.runelite` folder
    * On windows this should be located at  `C:\Users\<pcname>\.runelite` or `%USERPROFILE%/.runelite`
    * On linux this should be located at `~/.runelite`
2. Add your sound file to this folder
    * The file should be named `toa-chest.wav` and only `.wav` files are supported.
    * The entirety of the file will be played, it is recommend to limit this file to 30 seconds.
