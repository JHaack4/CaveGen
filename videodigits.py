import argparse
import cv2
import numpy as np
import os
import glob
import sys

path_to_digits = "files/digits/"

parser = argparse.ArgumentParser(description='Find the digits from a video file.')
parser.add_argument('videoFile', help='video file for processing. Must be 720x480')
parser.add_argument('-verbose', help='print debug text', action="store_true")
parser.add_argument('-images', help='show debug images', action="store_true")
args = parser.parse_args()
if args.verbose:
    print(args)

### generate template numbers

templates = []
for i in range(10):
    temp = cv2.imread(path_to_digits + str(i) + "_32.bti.png",cv2.IMREAD_UNCHANGED)
    
    height,width = temp.shape[:2]
    temp = cv2.resize(temp, (int(width*38*1.0/height),38))
    alpha = temp[:,:,[3,3,3]].astype(float)/255
    temp = (temp[:,:,:3] * alpha + 128 * (1 - alpha)).astype('uint8')
    temp = temp[:,:,0]
    
    templates.append(temp)
    
    if args.images:
        cv2.imshow("Digit",temp)
        cv2.waitKey(20)
    if args.verbose:
        print(temp.shape)
        
blank_template = templates[0].copy()
blank_template[:,:] = 128
templates.append(blank_template)
        
       
### find and read the digits of a single frame

most_recent_digits = [-1,-1,-1,-1,-1]
consecutive_digits = [0,0,0,0,0]
all_digits = []

def read_digits_on_frame(image):
    
    for i in range(5):
        img = image[162:200, 267+40*i+1:267+40*(i+1)-1]
        img = img.copy()
        blur = cv2.GaussianBlur(img,(139,139),0)
        diff = cv2.subtract(128 + cv2.subtract(img,blur),cv2.subtract(blur,img))
        diff = cv2.cvtColor(diff, cv2.COLOR_BGR2GRAY)

        score = []
        for j in range(11):
            template = templates[j]
            res = cv2.matchTemplate(diff,template,cv2.TM_SQDIFF_NORMED)
            score.append(( j,res.min(axis=0).min(axis=0) ))

        score = sorted(score, key=lambda x: x[1])
        if args.verbose:
            print(",".join(["%d %.3f" % x for x in score]))
        if args.images:
            cv2.imshow('Digit', diff)
            cv2.waitKey(35)
        pred = score[0][0]
            
        if most_recent_digits[i] == pred:
            consecutive_digits[i] += 1
        else:
            most_recent_digits[i] = pred
            consecutive_digits[i] = 1
        all_digits.append(pred)


### check for frames of the challenge mode result screen

# https://stackoverflow.com/questions/50899692/most-dominant-color-in-rgb-image-opencv-numpy-python
def bincount_app(a):
    a2D = a.reshape(-1,a.shape[-1])
    a2D = np.floor_divide(a2D,64)
    col_range = (4, 4, 4) # generically : a2D.max(0)+1
    a1D = np.ravel_multi_index(a2D.T, col_range)
    return np.unravel_index(np.bincount(a1D).argmax(), col_range)
    
def is_chresult_screen(frame):
    b,g,r = bincount_app(frame)
    if args.verbose:
        print("frame: %d %d %d" % (b,g,r))
    return b==3 and g==2 and r==2

       
### Open the video file
videoFile = args.videoFile
if videoFile == 'find':
    videoDir = ""
    with open("seed_video_path.txt","r") as f:
        for line in f:
            if len(line) > 0:
                videoDir = line.strip()
    list_of_files = glob.glob(videoDir + "*")
    videoFile = max(list_of_files, key=os.path.getctime)
    print("Searching: %s" % videoDir)
    print("Using: %s" % videoFile)
cap = cv2.VideoCapture(videoFile)

if (cap.isOpened() == False): 
    print("Failure - error opening video stream or file")
    sys.exit(0)
    
    
### watch the video, find frames of the challenge mode result screen, and read the digits  
skip = 0
count = 0
while(cap.isOpened()):
    count += 1
    ret, frame = cap.read()
    if not ret:
        break
    if skip > 0:
        skip -= 1
        continue
    if not is_chresult_screen(frame):
        skip = 100
        continue
    
    if args.images:
        cv2.imshow('Frame',frame)
        cv2.waitKey(50)
    read_digits_on_frame(frame)

with open("seed_digits_parsed.txt","w") as f:
    for i in range(len(all_digits)):
        d = all_digits[i]
        f.write(('_' if d > 9 else str(d)) + ("\n" if i % 5 == 4 else ""))
        print('_' if d > 9 else d,end="\n" if i % 5 == 4 else "")

cap.release()
cv2.destroyAllWindows()
