Feel free to use/modify this tool as you see fit
If you have questions, ping the Pikmin Speedrunning or Hocotate Hacker Discord


DOWNLOAD:
Requires Java version 8 or later.
https://github.com/JHaack4/CaveGen
Click "Clone or Download" and download ZIP. Unzip it.
Run the tool by double clicking CaveViewer.jar, or run from the command line as below.


COMPILE:
javac *.java --release 8 && jar cmf manifest.mf CaveGen.jar *.class
RUN:
java CaveGen [args]
RUN JAR FILE:
java -jar CaveGen.jar [args]


ARGUMENTS:
output (required) - folder that the images get sent to. 
	seed - a folder such as "12345678" is generated for each seed
	cave - a folder such as "BK-4" is generated for this sublevel
	both - both folders are made
	none - no images are generated (tool runs much faster for collecting stats)
cave (required) - cave that gets generated. 
	.txt file: e.g. caveinfo.txt
	group: "cmal" "story" "both" generates sublevels for the entire game
	abbreviation: e.g. BK, SCx, CH1, CH2, etc.
sublevelNum (required unless using a group) - sublevel to generate, or 0 for the entire cave
seed - seed to use (corresponds to Meeo's setseed code)
num - number of instances of each sublevel to create (be careful with this!)
consecutiveSeeds - seeds are checked in order of succesive rand() calls (useful for TAS)
challengeMode - spawns holes and items on normal mode (this is auto-set correctly for you)
storyMode - spawns holes and items on hard mode
noImages - no images generated
noPrints - no info printed to console
noStats - no stats report is generated to the !reports folder
region [us|jpn|pal] - changes treasure icons (default us)
251 - uses pikmin 251 caves
caveInfoReport - generates pictures containing all info about a sublevel
drawSpawnPoints - draws locations where things can spawn
drawSpawnOrder - draws order objects spawned in
drawAngles - draws angles that things are facing
drawDoorIds - draws door ids
drawTreasureGauge - draws rings to help you determine treasure gauge reads
drawHoleProbs - estimates contiditonal probabilities for the hole location in challenge mode
drawWayPoints - draws the waypoint graph
drawWPVertexDists - draws the distance from each waypoint to the pod
drawWPEdgeDists - draws the distance along each edge in the waypoint graph
drawAllWayPoints - does all three of the preceeding calls
drawScores - draws the game's internal scoring function for treasures and holes
drawDoorLinks - draws the game's internal scoring for distances
drawEnemyScores - draws the game's internal scoring for enemies
drawUnitHoleScores - draws the unitscores and doorscores for the hole phase
drawUnitItemScores - draws the unitscores and doorscores for the item phase
drawAllScores - does all five of the preceeding calls
drawNoWaterBox - doesn't draw the blue waterboxes
drawNoFallType - doesn't draw the falltype indicators
drawNoGateLife - doesn't draw the gate's life
drawNoObjects - doesn't draw any objects
findGoodLayouts 0.01 - runs a heuristic to only output images for the best 1% of layouts
requiredMapUnits unitType,rot,idFrom,doorFrom,doorTo;... - only outputs maps such that
	for the ith entry in this semicolon separated list, the ith map unit generated
	has this unit type and rotation, and is attached at the door with id doorTo 
	to the map unit with id idFrom at the door with id doorFrom
judge attk|pod|at|key|cmat|score [<0.5%] [>12345] - runs various heuristics to score/rank 
	levels, with optional filtering

EXAMPLES:
java -jar CaveGen.jar seed story -seed 0x12345678 -drawSpawnPoints
  This generates images of all levels in story mode with that seed.
java -jar CaveGen.jar cave BK 4 -num 100 -seed 0 -consecutiveSeeds
  This generates images for 100 instances of BK4, checking seeds following 0.
java -jar CaveGen.jar none CH12 0 -num 10000
  This generates stats for 10000 instances of concrete maze, no images.
java -jar CaveGen.jar caveinfo.txt 0
  This generates the whole caveinfo.txt cave


SPAWNPOINT KEY:
0 Pink        = easy enemy (size = radius, num rings = max spawn count)
1 Red         = hard enemy
2 Orange      = treasure
4 Green       = hole spot
5 Grey        = door
6 Light green = plant
7 Teal        = starting spot
8 Purple      = special enemy
9 Blue        = alcove


FALLING KEY:
0 nothing = not falling
1 purple  = fall if anything near
2 blue    = only fall if pikmin near
3 red     = only fall if leader near
4 orange  = only fall if pikmin carrying near
5 green   = only fall if a purple earthquake is nearby


TREASURE GAUGE KEY:
0.5 ticks = cyan
1 tick    = yellow
2 ticks   = light blue
3 ticks   = red
4 ticks   = purple
5 ticks   = orange
6 ticks   = green
7 ticks   = brown
8 ticks   = blue


UNIT TYPE KEY:
0 alcove
1 room
2 corridor


MORE INFO:
https://pikmintkb.com/wiki/Cave_generation_parameters


AUTHOR:
JHawk, 11/4/2019
special thanks to ice cube, Meeo, and Espyo for their help

If you want to learn about the spawning algorithm, you can read the code in CaveGen.java,
or watch this video: https://www.twitch.tv/videos/499998582
To test your understanding, try the quiz below:


QUIZ:
GENERAL QUESTIONS (for story mode)
1. What's the primary difference between the spawning algorithm in story mode and challenge mode?
2. Why is there a skewed distribution for the room that the pod spawns in?
3. Why is there a skewed distribution for the rooms that are used, despite the game's attempt to balance the rooms used?
4. What factors control how corridors spawn relatvie to rooms and alcoves?
5. What factors control how corridors branch out?
6. Can a corridor exist behind an alcove?
7. Where does the hole/geyser spawn?
8. What determines which and where enemies (possibly containing treasures) spawn?
9. Where do treasures (not in enemies) spawn? How does ordering matter? Which enemies matter?
10. What determines where enemies in alcoves spawn? How does ordering matter?
11. How do gates prioritize their spawning? If you see a gate between two rooms, what does that tell you about the alcoves?
12. Which direction does the camera face at the start of a sublevel? 
13. How does the view during the comedown relate to view from pressing L at the start of the sublevel?
14. From how far away does the treasure gauge pick up a treasure?

SUBLEVEL KNOWLEDGE
15. In HoB 1, the probability that the treasure spawns in a room instead of an alcove is 9/16. Why?
16. Where do the mitites spawn in HoB 4?
17. In HoB 4, the card spawns in the spiral room if and only if there is a fire geyser or bulborb in the spiral room. Why?
18. Why does the hole sometimes appear not in an alcove in WFG 2? Is the probability of this occurance 1/8?
19. On SH 2, how can you tell if you have a mum or a flower, based solely on the location?
20. On SH 2, why does the strawberry never spawn in the water? Is the same true for the sushi and why?
21. How can you tell which bulborb holds the treasure in BK 1, without using the gauge?
22. What factors determine how many purple flowers spawn on BK 4?
23. If you've found the tape in SCx 1, what does that tell you about the location of the Nouveau Table? How about vice versa? What other sublevels does this logic extend to?
24. What is the probability of having to do rimwalk on SCx 5?
25. Why does the bolt in particular sometimes spawn close on SCx 6?
26. When does the hole spawn in a corridor in SCx 8?
27. Can a treasure spawn in a corridor in FC 3? How can you tell if you are in a corridor?
28. Why do the treasures sometimes spawn close on FC 4?
29. Why does the exit sometimes spawn close on FC 6?
30. On CoS3, if you see a gate with nothing visually behind it, what does that tell you? Why?
31. On GK4, the odds of high invigorator are 27.6%, while the odds of high chocolate are 28.8%. Why are these different?
32. Why does the treasure sometimes not spawn on SCx7? SC4? GK3? CoS4? How are all of these cases different?


REAL TIME CONTINOUS CHALLENGE MODE MANIPULATION:
1) Install: Git, Python (with numpy & opencv), and OBS (with VirtualCam).
2) Use Git to download CaveGen from GitHub (this way, you can easily pull updates)
3) Test that VirtualCam works from OBS (can use Google hangouts or some other way to read your camera)
4) Configure OBS in some reasonable way, and record a video of challenge mode result screen digits
5) Modify continous_config.py so that images is True, "camera"="find", and video_path to the directory of your recorded video
6) Run python continuous.py, and make sure it's reading the video you recorded
7) Create a new directory and set templates to point there, and set generate_new_templates to True
8) Tweak the parameters x,y,height,width,spacing until you crop out well shaped templates (important to make these as perfect as possible)
9) Select a set of templates, and name them 0,1,...,9 and _.png. (similar to files/digits_wii)
10) Set images and generate_new_templates back to False, and see if continuous can read the digits
11) Once you're satisfied that continous.py can read digits, set camera back to 1 (or whatever port VirtualCam outputs to)
12) Configure manip_config.txt to your desired parameters
13) Run the score attack manip with: seed manip attk
14) If you want to run the CMAL manip, you need a special file called a timestable. You can create one yourself with "seed timestable key|cmat|700k", 
	but this will take ~30hr to run. See if someone on the discord has already uploaded it separately (it's ~4GB)


SEED DETECTION FROM LETTERS:
1) Install: Git, Java, Python 3.6+ (with numpy & opencv), and OBS Studio (latest patch, otherwise with VirtualCam).
2) Use Git to download CaveGen from GitHub (this way, you can easily pull updates)
3) Test that VirtualCam works from OBS (can use Google hangouts or some other way to read your camera)
4) Configure OBS in some reasonable way, and record a video of letters falling. Try to capture an example of both HoB and WFG.
5) Modify continouous_config.py so that camera is find, and the video path points to your video directory. Set playback and images to True.
6) Adjust the crop parameters until you've cropped out just the gameplay from the video.
7) Adjust the fadeout_frame_intensity parameter until it detects fadeouts correctly (can use paint to find the darkness of a fadeout in your video, set this param to slightly larger than that)
8) Adjust the letters_* parameters until the debug images in the im/ folder match for HoB.
9) Adjust the x_scrunch_limit parameter until WFG matches up.
10) Adjust letter_intensity_thresh until the debug images correctly pick out letters.
11) Run the command "seed manip pod", and test if it now correctly reads the seeds. From the GUI, use "[" and "]" to change the expected sublevel.
12) If you want to run from a live OBS feed, use virtual cam, and set the camera in the config to 1, 2, ... until you find the right camera.
