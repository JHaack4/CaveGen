# Adding support for new Pikmin 2 romhacks to Cavegen

This is a guide for adding support for new romhacks to Cavegen. This is intended for *Linux/MacOS* users; all the similar guides I found expect you to be using Windows, whereas this will work on any device (including Windows through WSL).

## Tools you need
- A recent-ish version of Dolphin (https://dolphin-emu.org/). We'll use this for extracting the filesystem of the romhack so Cavegen has access to its assets (radar images, custom enemy icons, etc.) and Caveinfo files.
- Wiimms SZS Toolkit (https://szs.wiimm.de/). We'll use this for converting the game's built-in custom file formats, namely SZS - basically a custom zip format, and BTI - an image format, to file types normal computers can recognize.
- Java (JDK + JRE) 1.8 or later, to build and run Cavegen.
- A code editor of your choice. I use VSCode, but Notepad++, Vim, IntelliJ, etc. will work fine. (We won't have to edit any Caveinfo files, so it's not strictly necessary to use something with SHIFT-JIS support.)
- A bash (or compatible) terminal with access to the `find` command.

## Guide
We'll be starting from an *already-built* ISO of the romhack you wish to add support for. Most romhacks come with installation/patching instructions themselves, so follow the instructions for the romhack of your choice. The reason we do this is that we need a complete folder structure for several of the important directories, and romhacks usually only include the files they need to change in the installation folder.

### Extracting the filesystem of the romhack
Open Dolphin and place your romhack ISO in a location it can see. Right click the game in the games list, click Properties, then go to the Filesystem tab. You should see a folder structure of the game's internal files. At the top there should be an entry called "Disc" or similar which represents the entire ISO - right click it and choose "extract entire disc" to a folder of your choice, which I'll assume is called `disc_fs`.

(You may find that Dolphin creates two folders inside your destination folder: `files` and `sys`. All future instructions assume you're inside the `files` folder if it exists; `sys` is irrelevant for our purposes.)

### Copying necessary files
There are 5 folders Cavegen requires to generate layout images for a romhack: `arc`, `caveinfo`, `units`, `enemytex`, and `resulttex`. Create a folder somewhere for these files to be copied into; I'll assume it's called `romhack` for the sake of this guide.

`arc`, `caveinfo`, and `units` can be found in `disc_fs/user/Mukki/mapunits`. Copy them into `romhack/`.

`enemytex` can be found in `disc_fs/user/Yamashita`. The `arc` in this folder is different and should be ignored. Copy `disc_fs/user/Yamashita/enemytex` into `romhack/`. Note that existing romhacks in Cavegen have a `special` folder located inside `enemytex` that isn't present in the ISO. You can just copy this folder from another romhack into our `enemytex` and it should work.

`resulttex` can be found in `disc_fs/user/Matoba`. Copy it into `romhack/`.

### Extracting SZS and BTI files
Now that we have our folder set up, we need to extract the Gamecube file formats into regular computer ones. We'll do this in two steps: SZS first, then BTI second.

In your bash shell, cd into `romhack/`. I'll assume `romhack/` is the working directory for the rest of this section. You also need Wiimms SZS tools for this section. The `find` commands below were run on BSD `find`; if you have GNU `find`, you might need to modify them a little, but overall they should be about the same.

To extract all the SZS files at once, run the following command to extract every SZS archive in your romhack folder:
```bash
find ./ -iname "*.szs" -execdir wszst EXTRACT {} \;
```

Next, we have to convert BTI images to PNG:
```bash
find ./ -iname "*.bti" -execdir wimgt DECODE {} \;
```

This should leave you with a complete set of files required for Cavegen to generate layouts for your romhack.

### Adding code support for your romhack into Cavegen
Open your code editor of choice. We're going to be making some basic edits to the Java code that makes up Cavegen. You don't strictly need to know Java for this, but I assume you have some programming knowledge and you can figure out any bits of syntax you're not familiar with. Ctrl-F "251" should bring you to the right lines in most instances, and you can copy what 251 does.

In your CaveGen folder, open CaveGen.java. Here we need to add a flag for the romhack. Go to the parameter definition section (starting on line 9 currently) and add a bool to the list for your romhack called whatever you wish.

Next go to the `resetParams()` function and add a statement to reset the variable you added above to false.

Then go down to where the `fileSystem` variable is set - currently this is about line 121. You should see some if/else blocks testing the flags for the existing romhacks. Add one for yours, setting the value of `fileSystem` to whatever you named your folder above (`romhack` in this example). After this, you're done in CaveGen.java.

Next, open Parser.java. At the top, you'll see some arrays that define the user-friendly cave names and caveinfo file names for each romhack (and the vanilla game). Copy these arrays (`all` and `special` will do) to below the existing ones and rename your new ones to `allRomhack` and `specialRomhack` (substituting the word "romhack" with the name of the hack, ideally).

Now you need to check what the caveinfo files are named in your romhack. Look at the file names in `romhack/caveinfo/` - you should see a bunch of text files. The main caves in the vanilla game are named `tutorial_1.txt` up through 3, `forest_1.txt` etc., `yakushima_1.txt` etc., and `last_1.txt` etc. for each of the four main areas, in order. Your romhack may keep the same names, or it may leave those files as they are and include its own custom caveinfo files (as Pikmin 251 does). In `allRomhack`, change the first several entries (those that don't begin with "ch") to the caveinfo filenames in your romhack, without ".txt".

Then change the entries in `specialRomhack` to the cave name abbreviations you want to use for each cave, in the *exact same order* as the filenames in `allRomhack`. These are completely up to you, but Cavegen will expect you to use these names exactly when you invoke commands at the end.

There's one last set of code changes we need to make in this file. Scroll down to the `fromSpecial` method and add a new `if` block that changes `all` and `special` to those of your romhack. Use Pikmin 251 as an example. Do the same in the `toSpecial` function beneath.

## Wrap up
Now that you've made all the necessary code changes and done all the extractions, just move your `romhack` folder into CaveGen's `files` folder, beside `gc` and `251`. Then run `./build.sh` or `build.bat` to compile the code, and you should be good to go!
