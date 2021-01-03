
from argparse import Namespace
import cv2
import numpy as np
import os
import glob
import sys
import time
import ast
import random

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
    y = height//20
    window1 = frame[0:4*y, 0:x, :]
    window2 = frame[4*y:8*y, 0:x, :]
    window3 = frame[8*y:10*y, 0:x, :]
    window4 = frame[10*y:13*y, x//4:3*x//4, :]
    window5 = frame[13*y:height, 0:x, :]
    average1 = window1.mean(axis=0).mean(axis=0)
    average2 = window2.mean(axis=0).mean(axis=0)
    average3 = window3.mean(axis=0).mean(axis=0)
    average4 = window4.mean(axis=0).mean(axis=0)
    average5 = window5.mean(axis=0).mean(axis=0)
    # print()
    # print(average1)
    # print(average2)
    # print(average3)
    # print(average4)
    # print(average5)
    # frame[4*y,:,:] = 255
    # frame[8*y,:,:] = 255
    # frame[10*y,:,:] = 255
    # frame[13*y,:,:] = 255

    f = 7
    if  average1[0] < f and average1[1] < f and average1[2] < f and \
        average2[0] < f and average2[1] < f and average2[2] < f and \
        average3[0] < f and average3[1] < f and average3[2] < f and \
        average4[0] < f and average4[1] < f and average4[2] < f and \
        average5[0] < f and average5[1] < f and average5[2] < f and abs(average4[2]-average5[2])<2:
        return 'fadeout'
    if  average1[0] < f and average1[1] < f and average1[2] < f and \
        average2[0] < 50 and average2[1] < 50 and average2[2] > 15 and average2[2] < 70 and abs(average2[0] - average2[2]) > 10 and \
        average3[0] < f and average3[1] < f and average3[2] < f and \
        average4[0] > f and average4[1] > f and average4[2] > f and \
        average4[0] < 40 and average4[1] < 40 and average4[2] < 40 and \
        average5[0] < f and average5[1] < f+1 and average5[2] < f+5:
        return 'chenter'
    if  average1[0] < 25 and average1[1] < 25 and average1[2] < 25 and \
        average2[0] < 40 and average2[1] < 40 and average2[2] < 40 and abs(average2[0] - average2[2]) < 6 and abs(average2[0] - average2[1]) < 6 and \
        average3[0] < 25 and average3[1] < 25 and average3[2] < 25 and \
        average4[0] > f and average4[1] > f and average4[2] > f and \
        average4[0] < 40 and average4[1] < 40 and average4[2] < 40 and \
        average5[0] < f and average5[1] < f+1 and average5[2] < f+5:
        return 'storyenter'

    
    return None

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
    
falling_img = None
union_img = None
frames_since_first_story = 10000
frames_since_last_story = 10000
story_frames = []
story_frames_processed = False

legal_names = ["Emergence Cave","Subterranean Complex","Frontier Cavern",
                "Hole of Beasts","White Flower Garden","Bulblax Kingdom","Snagret Hole",
                "Citadel of Spiders","Glutton's Kitchen","Shower Room","Submerged Castle",
                "Cavern of Chaos","Hole of Heroes","Dream Den"]
legal_names_mistakes = ["Bulb!lax Kingdom","Sub!merged Castle","Sub!terranean Complex","H!ole of Beasts","Snagret H!ole","H!ole of Heroes","H!ole of H!eroes","Hole of H!eroes","Wh!ite Flower Garden","Cavern of Ch!aos","Wh!!ite Flower Garden"]

def process_story_frames():
    #print("processing story frames")
    height,width = story_frames[0].shape[:2]

    #print(height)
    #print(width)

    # compute the average image after the letters have stopped moving
    num_avg = 25
    sum_image = story_frames[0].copy()
    sum_image[:,:,:] = 0
    for frame in story_frames[-num_avg:]:
        sum_image = sum_image + frame / num_avg
    _,avg_thresh_img = cv2.threshold(sum_image,75,255,cv2.THRESH_BINARY)

    max_row_for_falling = 10*height//100
    min_row_for_columns = 26*height//100
    max_row_for_columns = 35*height//100

    # compute the spaces from the column image
    column_img = union_img[0:max_row_for_falling,:,2]
    column_mean = [x*max_row_for_falling for x in column_img.mean(axis=0)]
    print(",".join([f"{int(x)}" for x in column_mean]))
    
    whitespace = []
    last_black = True
    for i in range(len(column_mean)):
        f = 11*255/5 # need at least 12 pixels in column white...
        if column_mean[i] > f and last_black:
            whitespace.append(i)
        elif column_mean[i] <= f and not last_black:
            whitespace.append(i)
        last_black = column_mean[i] <= f
    #print(whitespace)

    spaces_between = []
    cur_space_count = 0
    word_shape = ""
    for i in range(0,len(whitespace),2):
        black_dist = whitespace[i] - whitespace[i-1] if i > 0 else 0
        if black_dist/width > 20/1280:
            cur_space_count += 1
            word_shape += ' '
        word_shape += "x"
        spaces_between.append(cur_space_count)

    # try to error correct the name by joining pieces (solves for separated H)
    #print(word_shape)
    cave_name = ""
    for s in legal_names:
        if len(s) == len(word_shape):
            match=True
            for i in range(len(s)):
                if (word_shape[i]==' ') != (s[i]==' '):
                    match=False
            if match:
                cave_name = s
                break
    if cave_name == "": # try to error correct a mistake...
        #print("trying to error correct")
        for s in legal_names_mistakes:
            #print(s)
            if len(s) == len(word_shape):
                #print("len match")
                match=True
                for i in range(len(s)):
                    if (word_shape[i]==' ') != (s[i]==' '):
                        match=False
                if match:
                    #print("match")
                    print("correction match" + s)
                    cave_name = s.replace("!","")
                    for i in range(len(s)-1,-1,-1):
                        if s[i] == "!":
                            #print("bad i " + str(i))
                            whitespace.pop(2*i-1-spaces_between[i]*2)
                            whitespace.pop(2*i-1-spaces_between[i]*2)
                            spaces_between.pop(i)
                    break
        #print(whitespace)
        #print(spaces_between)

    story_frame_count = 0
    info_string = []
    
    # for each frame with a falling letter, compute the location of the falling letter
    for frame in story_frames[0:50]:
        story_frame_count += 1

        #print("storyenter " + str(story_frame_count), flush=True)
        img = frame.copy()
        _,img = cv2.threshold(img,15,255,cv2.THRESH_BINARY)
        for i in range(0,len(whitespace),2):
            section_img = img[0:max_row_for_falling,whitespace[i]:whitespace[i+1],2]
            row_means = section_img.mean(axis=1)
            #print(row_means)
            last_nonzero = -1
            for j,r in enumerate(row_means):
                if r > 5:
                    last_nonzero = j
            if last_nonzero != -1 and last_nonzero < max_row_for_falling-1:
                #print(str(i//2) + " " + str(last_nonzero))
                info_string.append(str(i//2+spaces_between[i//2])+","+str(last_nonzero)+",")
                union_img[last_nonzero,whitespace[i]:whitespace[i+1],:]=0
                union_img[last_nonzero,whitespace[i]:whitespace[i+1],2]=255
                falling_img[last_nonzero,whitespace[i]:whitespace[i+1],:]=0
                falling_img[last_nonzero,whitespace[i]:whitespace[i+1],2]=255
                #img[last_nonzero,whitespace[i]:whitespace[i+1],:]=0
                #img[last_nonzero,whitespace[i]:whitespace[i+1],2]=255
        if args.images:
            pass
            #cv2.imwrite("im/" + str(count) + "s" + str(story_frame_count) + ".png", img)
        info_string.append(";")

    # compute the height/bottom of each char for offsetting
    offset_info = ""
    char_heights = []
    for i in range(0,len(whitespace),2):
        section_img = avg_thresh_img[min_row_for_columns:max_row_for_columns,whitespace[i]:whitespace[i+1],2]
        row_means = section_img.mean(axis=1)
        last_nonzero = -1
        first_nonzero = -1
        for j,r in enumerate(row_means):
            if r > 5:
                last_nonzero = j
                if first_nonzero == -1:
                    first_nonzero = j
        last_nonzero += min_row_for_columns
        first_nonzero += min_row_for_columns
        if i > 0 and spaces_between[i//2] - spaces_between[i//2-1] > 0:
            offset_info += ",-1"
        if last_nonzero != -1 and last_nonzero < max_row_for_columns-1:
            offset_info += "," + str(last_nonzero)
            avg_thresh_img[last_nonzero,whitespace[i]:whitespace[i+1],:]=0
            avg_thresh_img[last_nonzero,whitespace[i]:whitespace[i+1],1:2]=255
            avg_thresh_img[first_nonzero,whitespace[i]:whitespace[i+1],:]=0
            avg_thresh_img[first_nonzero,whitespace[i]:whitespace[i+1],1:2]=255
        else:
            offset_info += ",-2"
        char_heights.append(last_nonzero-first_nonzero)

    #print(char_heights)
    if cave_name == "Hole of Beasts" and char_heights[-3] * 1.2 >= char_heights[-2]:
        cave_name = "Hole of Heroes"

    print("lettersinfo," + cave_name.replace(" ","_") + "," + str(height) + "," + str(len(whitespace)//2+cur_space_count) + offset_info + ";" + "".join(info_string), flush=True)    
    
    if args.images:
        avg_thresh_img[min_row_for_columns,:,:] = 255
        avg_thresh_img[max_row_for_columns,:,:] = 255
        union_img[max_row_for_falling,:,:] = 0 
        union_img[max_row_for_falling,:,0] = 155
        falling_img[max_row_for_falling,:,:] = 0 
        falling_img[max_row_for_falling,:,0] = 155
        for x in whitespace:
            avg_thresh_img[0:max_row_for_columns,x,:] = 0
            avg_thresh_img[0:max_row_for_columns,x,2] = 255
            union_img[0:max_row_for_columns,x,:] = 0
            union_img[0:max_row_for_columns,x,2] = 255
            falling_img[0:max_row_for_columns,x,:] = 0
            falling_img[0:max_row_for_columns,x,2] = 255

        cv2.imwrite("im/" + str(count) + "!avg" + ".png", sum_image)
        cv2.imwrite("im/" + str(count) + "!avg_th" + ".png", avg_thresh_img)
        cv2.imwrite("im/" + str(count) + "!union" + ".png", union_img)
        cv2.imwrite("im/" + str(count) + "!union_p" + ".png", falling_img)


def random_colorize(img):
    ret = img.copy()
    ret[:,:,0] = img[:,:,2]*(random.randint(100,255)/255)
    ret[:,:,1] = img[:,:,2]*(random.randint(100,255)/255)
    ret[:,:,2] = img[:,:,2]*(random.randint(100,255)/255)
    return ret

    
### watch the video, find frames of the challenge mode result screen, and read the digits  
skip = 0
count = 0
last_frame_was_digit = False

while(cap.isOpened()):
    count += 1
    ret, frame = cap.read()
    frames_since_first_story += 1
    frames_since_last_story += 1
    
    if not ret:
        break
    if skip > 0:
        skip -= 1
        continue

    if count < 30*60*8: continue
    
    height,width = frame.shape[:2]
    if count == 1:
        print(f"height {height} width {width}")
    #frame = cv2.resize(frame, (853,480), interpolation=cv2.INTER_NEAREST)
    #frame = frame[:,66:786,:] # crop to 720x480
    frame = frame[:,360:,:]
    
    frame_type = is_levelenter_screen(frame)

    if frame_type == 'fadeout':
        print("fadeout",flush=True)
    elif frame_type == 'chenter':
        print("levelenter", flush=True)
    elif frame_type == 'storyenter':
        print("storyenter " + str(count), flush=True)
        img = frame.copy()
        story_frames.append(img)
        _,img = cv2.threshold(img,17,255,cv2.THRESH_BINARY)
        # Creating kernel 
        kernel = np.ones((3, 3), np.uint8) 
        img = cv2.erode(img, kernel) 
        if frames_since_last_story > 10:
            falling_img = img.copy()
            union_img = img.copy()
            falling_img[:,:,:] = 0
            union_img[:,:,:] = 0
            frames_since_first_story = 0
            story_frames = []
            story_frames.append(img)
            story_frames_processed = False
        if frames_since_first_story <= 50:
            falling_img = falling_img + random_colorize(img)
            union_img = union_img + img/5
        frames_since_last_story = 0

        if frames_since_first_story >= 80 and len(story_frames) >= 80:
            if not story_frames_processed:
                story_frames_processed = True
                process_story_frames()

        
    elif is_chresult_screen(frame):
        if args.generate_new_templates:
            generate_new_templates(frame)
        else:
            read_digits_on_frame(frame)
        last_frame_was_digit = True
    else:
        if last_frame_was_digit:
            print("donedigit",flush=True)
        else:
            skip = 5
        last_frame_was_digit = False

    
    if args.images:
        cv2.imshow('Frame',frame)
        key_press = cv2.waitKey(1)
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

# if  average1[0] < 3 and average1[1] < 3 and average1[2] < 3 and \
#         average2[0] < 3 and average2[1] < 3 and average2[2] < 3 and \
#         average3[0] < 3 and average3[1] < 3 and average3[2] < 3 and \
#         average4[0] < 1.5 and average4[1] < 1.5 and average4[2] < 1.5 and \
#         average5[0] < 3 and average5[1] < 3 and average5[2] < 3:
#         return 'fadeout'
#     if  average1[0] < 3 and average1[1] < 3 and average1[2] < 3 and \
#         average2[0] < 20 and average2[1] < 20 and average2[2] > 15 and average2[2] < 45 and \
#         average3[0] < 3 and average3[1] < 3 and average3[2] < 3 and \
#         average4[0] > 5 and average4[1] > 5 and average4[2] > 5 and \
#         average4[0] < 30 and average4[1] < 30 and average4[2] < 30 and \
#         average5[0] < 3 and average5[1] < 3 and average5[2] < 3:
#         return 'chenter'
#     if  average1[0] < 20 and average1[1] < 20 and average1[2] < 20 and \
#         average2[0] < 20 and average2[1] < 20 and average2[2] < 20 and abs(average2[0] - average2[2]) < 10 and \
#         average3[0] < 20 and average3[1] < 20 and average3[2] < 20 and \
#         average4[0] > 1.5 and average4[1] > 1.5 and average4[2] > 1.5 and \
#         average4[0] < 30 and average4[1] < 30 and average4[2] < 30 and \
#         average5[0] < 3 and average5[1] < 3 and average5[2] < 3:
#         return 'storyenter'