
from argparse import Namespace
import cv2
import numpy as np
import os
import glob
import sys
import time
import ast

# def RepresentsInt(s):
#     try: 
#         int(s)
#         return True
#     except ValueError:
#         return False

# def RepresentsFloat(s):
#     try: 
#         float(s)
#         return True
#     except ValueError:
#         return False

# def parseArg(a):
#     if RepresentsInt(a):
#         return int(a)
#     elif RepresentsFloat(a):
#         return float(a)
#     elif a == "True":
#         return True
#     elif a == "False":
#         return False
#     else:
#         return a

# args_dict = {}
# with open("files/continuous_config.txt", "r") as f:
#     for line in f:
#         spl = line.split(":")
#         args_dict[spl[0].strip()] = parseArg(":".join(spl[1:]).strip())
# args = Namespace(**args_dict)
# if args.verbose:
#     print(args)
# y: 157
# x: 257
# s: 39
# w: 40
# h: 40
# camera: find
# templates: files/digits/templates/
# video_path: C:\Users\Jordan\Videos\
# images: True
# verbose: True

with open("continuous_config.txt", "r") as f:
    args = Namespace(**ast.literal_eval(f.read()))
print(args)

try:
    os.mkdir(args.templates)
except FileExistsError:
    pass

videoFile=args.camera
if videoFile == 'find':
    list_of_files = glob.glob(args.video_path + "*")
    videoFile = max(list_of_files, key=os.path.getctime)
    print("Using: %s" % videoFile)
cap = cv2.VideoCapture(videoFile)

if (cap.isOpened() == False): 
    print("Failure - error opening video stream")
    print("make sure virtual cam is setup correctly")
    sys.exit(0)

### generate template numbers

templates = []

if not args.generate_new_templates:
    for i in range(10):
        temp = cv2.imread(args.templates + str(i) + ".png",cv2.IMREAD_UNCHANGED)
        templates.append(temp)
    templates.append(cv2.imread(args.templates + "_.png",cv2.IMREAD_UNCHANGED))
        
### find and read the digits of a single frame

def read_digits_on_frame(image):

    if args.images:
        comp_img = np.zeros((11*args.height,5*args.width,3), np.uint8)
    output_digits = []
    
    for i in range(5):
        img = image[args.y:args.y+args.height, args.x+args.spacing*i:args.x+args.spacing*i+args.width, :]
        img = img.copy()
        #blur = cv2.GaussianBlur(img,(139,139),0) # this is too slow
        blur = np.zeros(img.shape, np.uint8)
        blur[:,:] = frame.mean(axis=0).mean(axis=0)
        diff = cv2.subtract(128 + cv2.subtract(img,blur),cv2.subtract(blur,img))
        diff = cv2.cvtColor(diff, cv2.COLOR_BGR2GRAY)

        score = []
        #score_addn = [-0.02, 0.015, 0, 0.01, 0,  0, 0, 0.015, 0, 0,  0.07]
        score_addn = [0,0.01,0,0.01,0, 0,0,0,0,0, 0.03]
        for j in range(11):
            template = templates[j]
            res = cv2.matchTemplate(diff,template,cv2.TM_SQDIFF_NORMED)
            score.append((j,res.min(axis=0).min(axis=0) + score_addn[j]))

        score = sorted(score, key=lambda x: x[1])
        pred = score[0][0] if score[0][1] < 0.18 else 10

        if args.verbose:
            print(",".join(["%d %.3f" % x for x in score]))
        if args.images:
            for j in range(11):
                temp_height,temp_width = templates[j].shape[:2]
                temp_x = i*args.width+(args.width-temp_width)//2
                temp_y = j*args.height+(args.height-temp_height)//2
                comp_img[j*args.height:j*args.height+args.height, i*args.width:i*args.width+args.width, 0] = diff
                comp_img[j*args.height:j*args.height+args.height, i*args.width:i*args.width+args.width, 1] = (0 if pred==j else diff)
                comp_img[j*args.height:j*args.height+args.height, i*args.width:i*args.width+args.width, 2] = 128
                comp_img[temp_y:temp_y+temp_height, temp_x:temp_x+temp_width, 2] = templates[j]
        
        output_digits.append(pred)

    # sss = "files/digits6/" + str(count) + "-" + "".join([str(i) if i < 10 else '_' for i in output_digits]) + ".png"
    # print(sss)
    # cv2.imwrite(sss, image)

    print("digits " + "".join([str(i) if i < 10 else '_' for i in output_digits]), flush=True)
    if args.images:
        cv2.imshow('Digit', comp_img)
        cv2.waitKey(500)

### check for frames of the challenge mode result screen

# https://stackoverflow.com/questions/50899692/most-dominant-color-in-rgb-image-opencv-numpy-python
def bincount_app(a):
    a2D = a.reshape(-1,a.shape[-1])
    a2D = np.floor_divide(a2D,64)
    col_range = (4, 4, 4) # generically : a2D.max(0)+1
    a1D = np.ravel_multi_index(a2D.T, col_range)
    return np.unravel_index(np.bincount(a1D).argmax(), col_range)
    
def is_chresult_screen(frame):
    height,width = frame.shape[:2]
    x = width//8
    y = height//25
    window = frame[0:y, 3*x:5*x, :]
    b,g,r = window.mean(axis=0).mean(axis=0)
    #print("frame: %d %d %d" % (b,g,r))
    return abs(b-252) + abs(g-11) + abs(r-1) < 15 or abs(b-128) + abs(g-0) + abs(r-0) < 5
    # b,g,r = bincount_app(frame)
    # if args.verbose:
    # return b==3 and g==2 and r==2

def is_fadeout_screen(frame):
    average = frame.mean(axis=0).mean(axis=0)
    return average[0] < 5 and average[1] < 5 and average[2] < 5

def is_levelenter_screen(frame):
    height,width = frame.shape[:2]
    x = width
    y = height//10
    window1 = frame[0:2*y, 0:x, :]
    window2 = frame[2*y:7*y, 0:x, :]
    window3 = frame[7*y:height, 0:x, :]
    average1 = window1.mean(axis=0).mean(axis=0)
    average2 = window2.mean(axis=0).mean(axis=0)
    average3 = window3.mean(axis=0).mean(axis=0)
    #print()
    #print(average1)
    #print(average2)
    #print(average3)
    return average1[0] < 5 and average1[1] < 5 and average1[2] < 5 and \
            average3[0] < 5 and average3[1] < 5 and average3[2] < 5 and \
            average2[0] < 20 and average2[1] < 30 and average2[2] < 30

def generate_new_templates(image):

    copy = image.copy()
    
    for i in range(5):
        img = image[args.y:args.y+args.height, args.x+args.spacing*i:args.x+args.spacing*i+args.width, :]
        img = img.copy()
        #blur = cv2.GaussianBlur(img,(139,139),0)
        blur = np.zeros(img.shape, np.uint8)
        blur[:,:] = frame.mean(axis=0).mean(axis=0)
        diff = cv2.subtract(128 + cv2.subtract(img,blur),cv2.subtract(blur,img))
        diff = cv2.cvtColor(diff, cv2.COLOR_BGR2GRAY)

        cv2.imwrite(args.templates + str(count) + "-" + str(i) + ".png", diff)

        if i % 2 == 0:
            copy[args.y:args.y+args.height, args.x+args.spacing*i:args.x+args.spacing*i+args.width, 2] = 200
        else:
            copy[args.y:args.y+args.height, args.x+args.spacing*i:args.x+args.spacing*i+args.width, 1] = 200
    
    cv2.imshow('Digit', copy)
    cv2.waitKey(args.template_wait)

    with open("files/continuous_config.txt", "r") as f:
        updated = Namespace(**ast.literal_eval(f.read()))
        args.x = updated.x
        args.y = updated.y
        args.spacing = updated.spacing
        args.width = updated.width
        args.height = updated.height
    
    
### watch the video, find frames of the challenge mode result screen, and read the digits  
skip = 0
count = 0
last_frame_was_digit = False
while(cap.isOpened()):
    count += 1
    ret, frame = cap.read()
    
    if not ret:
        break
    if skip > 0:
        skip -= 1
        continue
    
    #height,width = frame.shape[:2]
    #frame = cv2.resize(frame, (853,480), interpolation=cv2.INTER_NEAREST)
    #frame = frame[:,66:786,:] # crop to 720x480
    
    if is_fadeout_screen(frame):
        print("fadeout",flush=True)
    elif is_levelenter_screen(frame):
        print("levelenter", flush=True)
    elif is_chresult_screen(frame):
        if args.generate_new_templates:
            generate_new_templates(frame)
        else:
            read_digits_on_frame(frame)
        last_frame_was_digit = True
    else:
        if last_frame_was_digit:
            print("donedigit",flush=True)
        last_frame_was_digit = False
    
    # print("count " + str(count), flush=True)

    if args.images:
        cv2.imshow('Frame',frame)
        key_press = cv2.waitKey(10)
        if key_press & 0xFF == ord('q'):
            break
    

cap.release()
cv2.destroyAllWindows()
print("exit")

# {"y": 157,
#  "x": 268,
#  "spacing":39,
#  "width":40,
#  "height":40,
#  "camera":1,
#  "templates":"files/digits3/",
#  "video_path":"C:\\Users\\Jordan\\Videos\\",
#  "images":True,
#  "verbose":False,
#  "generate_new_templates":True
# }

# {"y": 157,
#  "x": 250,
#  "spacing":29,
#  "width":30,
#  "height":38,
#  "camera":1,
#  "templates":"files/digits4/",
#  "video_path":"C:\\Users\\Jordan\\Videos\\",
#  "images":False,
#  "verbose":False,
#  "generate_new_templates":False,
#  "template_wait":10
# }
