# Tombs of Amascut

Utilities and information for raiding the Tombs of Amascut.

**Note: While Tombs of Amascut is new, Jagex will be quite strict on what is and is not allowed in ToA plugins.**
Feel free to request new features in the Issues tab (after searching for an existing matching issue),
or follow a discussion there on whether a feature should be implemented.

### Current Features

<!--
#### Invocation Presets

Right-click the Preset button to create/load/import/export presets.
Invocations that need to be changed will be highlighted green/red.
Shift+right-click to delete presets.

![](docs/invocation-presets.gif)
-->

#### Deposit-pickaxe Swap

While holding a pickaxe, swaps the left-click option to "Deposit-pickaxe"
on the statue in the mirror puzzle room.

![](docs/deposit-pickaxe.gif)

#### Invocation Screenshot
Adds a button to ToA Invocation Interface that when clicked will copy all of your invocations as a picture to your system clipboard.
This image will also include the rewards section if it's enabled via the config options and the button is selected within the in-game interface.

![](docs/screenshot-button.png)

[Click here to see an example of the screenshot that will be created](docs/screenshot-example.png)

#### Scabaras Puzzle Helpers

<details>
<summary>Overlay Puzzle Solvers</summary>

#### Addition Puzzle

Highlights a path through the tiles that will add up to the target number.

![](docs/scabaras/addition.gif)

#### Lights

Shows the tiles that need to be flipped to solve the puzzle.

![](docs/scabaras/lights.gif)

#### Obelisks

Highlights the correct obelisk pattern as it is discovered by the player.

![](docs/scabaras/obelisks.gif)

#### Sequence

Shows the tile sequence after the pattern is shown.

![](docs/scabaras/sequence.gif)

#### Matching

Highlights each tile image a unique colour after it has been flipped once.

![](docs/scabaras/matching.gif)
</details>

<details>
<summary>Side Panel Addition Helper</summary>

When entering the Path of Scabaras, opens a side panel
with solutions for the numbered tiles puzzle.

![img.png](docs/scabaras-tile-puzzle-helper.png)
</details>

#### Apmeken Wave Helper

When entering the Path of Apmeken, opens a side panel
with a list of the baboon wave spawns.

![](docs/apmeken-wave-helper.png)

#### Chest Audio
This will play a `.wav` audio file from your local machine whenever the Tombs of Amascut Sarcophagus is opened(the purple chest).

##### Setup
1. Open your `.runelite/tombs-of-amascut` folder
    * On Windows, `C:\Users\<pcname>\.runelite\tombs-of-amascut` or `%USERPROFILE%\.runelite\tombs-of-amascut`
    * On macOS and Linux, `~/.runelite/tombs-of-amascut`
2. Add your sound file
    * The file should be named `toa-chest.wav` and only `.wav` files are supported.
    * The entirety of the file will be played, it is recommended to limit this file to 30 seconds.
