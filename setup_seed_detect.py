from argparse import Namespace
import cv2
import numpy as np
import os
import glob
import sys
import time
import ast
import random
import time

args = None
def generate_args():
    global args
    with open("config.txt", "r") as f:
        config_args_string = f.read()
        config_args_string = config_args_string[0:config_args_string.index("#####")]
        config_args_string = "\n".join([x[0:x.index("#")] if "#" in x else x for x in config_args_string.split("\n")])
        config_args_string = config_args_string.replace("\\","\\\\")
        args = Namespace(**ast.literal_eval(config_args_string))
    args.images = True
generate_args()

templates = []
def digit_templates():
    global templates
    templates = []

    for i in range(10):
        temp = cv2.imread(args.templates + str(i) + ".png",cv2.IMREAD_UNCHANGED)
        templates.append(temp)
    templates.append(cv2.imread(args.templates + "_.png",cv2.IMREAD_UNCHANGED))


letters = {}
letters_height = {}
letters_yoff = {}
letters_width = {}
letters_xoff = {}
other_letters_w = {}
def letter_templates():
    global letters, letters_height, letters_yoff, letters_width,letters_xoff,other_letters_w
    letters = {}
    letters_height = {}
    letters_yoff = {}
    letters_width = {}
    letters_xoff = {}

    for l in 'abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ':
        img = cv2.imread('files/templates/letters/' + (l+l if l.isupper() else l) + ".png")
        img = cv2.resize(img, (int(img.shape[1]*args.letters_xscale), int(img.shape[0]*args.letters_yscale) ), interpolation=cv2.INTER_NEAREST)
        letters[l] = np.uint8(img)
        row_mean = letters[l].max(axis=2).max(axis=1)
        min_idx = 1000
        max_idx = 0
        for i in range(len(row_mean)):
            if row_mean[i] > 6:
                min_idx = min(min_idx,i)
                max_idx = max(max_idx,i)
        letters_height[l] = max_idx-min_idx+1
        letters_yoff[l] = min_idx
        col_mean = letters[l].max(axis=2).max(axis=0)
        min_idx = 1000
        max_idx = 0
        for i in range(len(col_mean)):
            if col_mean[i] > 6:
                min_idx = min(min_idx,i)
                max_idx = max(max_idx,i)
        letters_width[l] = max_idx-min_idx+1
        letters_xoff[l] = min_idx
    other_letters_w = {" ": args.space_mult, "'": args.apostrophe_mult}


def get_screen_type(frame):
    if frame.max(axis=0).max(axis=0).max(axis=0) < args.fadeout_frame_intensity:
        return "fadeout"
    height,width = frame.shape[:2]

    # look for challenge mode result screen
    x = width//8
    y = height//100
    window_top = frame[y:4*y, 3*x:5*x, :]
    b,g,r = window_top.mean(axis=0).mean(axis=0)
    if abs(b-args.chresult_color_b) < 20 and abs(g-args.chresult_color_g) + abs(r-args.chresult_color_r) < 20:
        bM,gM,rM = window_top.max(axis=0).max(axis=0)
        bm,gm,rm = window_top.min(axis=0).min(axis=0)
        if bM-bm < 35 and gM-gm+rM-rm < 25:
            return "chresult"
    
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
    average5 = window5.max(axis=0).max(axis=0)

    # check for rough brightness levels
    f = args.fadeout_frame_intensity
    if max(average5) >= f:
        return None
    a = 5
    b = 45
    if not (average1[0] < 2*f+a and average1[1] < 2*f+a and average1[2] < 2*f+a and \
        average2[0] < 2*f+b and average2[1] < 2*f+b and \
        average3[0] < 2*f+a and average3[1] < 2*f+a and average3[2] < 2*f+a and \
        average4[0] < 2*f+b and average4[1] < 2*f+b and average4[2] < 2*f+b):
        return None

    # look for the word sublevel on the screen
    window4 = frame[10*y:13*y, 0:x, :]
    col_max = window4.max(axis=2).max(axis=0)
    black_space = []
    white_space = []
    count = 0
    white = True
    for i in range(len(col_max)):
        if col_max[i] > args.letter_intensity_thresh:
            if not white:
                black_space.append(count)
                count = 0
            white = True
            count += 1
        else:
            if white and count > 0:
                white_space.append(count)
                count = 0
            white = False
            count += 1
    black_space.append(count)

    if len(white_space) >= 8 and len(white_space) <= 12 \
        and max(white_space) > width * 25/960 and max(white_space) < width * 90/960 \
        and black_space[0] > width/6 and black_space[-1] > width/6 \
        and black_space[0] < width/3 and black_space[-1] < width/3 \
        and sum(white_space) > width * 280/960 \
        and (black_space[-2] > white_space[1]/2 or black_space[-3] > white_space[1]/2):
        # word sublevel is found

        # check for red bg.
        window2 = frame[4*y:8*y, width//4:3*width//4, :]
        average2 = window2.mean(axis=0).mean(axis=0)
        if average2[2]-average2[0] > args.chenter_redness:
            return "chenter"
        else:
            return "storyenter"

    return None

recommend_chresult_color = False
bs = []
gs = []
rs = []
def pull_numbers_from_image(frame, numbers):
    height,width = frame.shape[:2]

    numbers = "     "[0:5-len(numbers)] + numbers # pad to 5

    x = width//8
    y = height//100
    window_top = frame[y:4*y, 3*x:5*x, :]
    b,g,r = window_top.mean(axis=0).mean(axis=0)
    global bs,gs,rs
    bs.append(b)
    gs.append(g)
    rs.append(r)

    frame[y:4*y,3*x,:] = 255
    frame[y:4*y,5*x,:] = 255
    frame[y,3*x:5*x,:] = 255
    frame[4*y,3*x:5*x,:] = 255

    # crop out letters
    output_digits = ""
    for i in range(5):
        img = frame[args.digits_y:args.digits_y+args.digits_height, args.digits_x+args.digits_spacing*i:args.digits_x+args.digits_spacing*i+args.digits_width, :]
        img = img.copy()
        #blur = cv2.GaussianBlur(img,(139,139),0) # this is too slow
        blur = np.zeros(img.shape, np.uint8)
        blur[:,:] = frame.mean(axis=0).mean(axis=0)
        diff = cv2.subtract(128 + cv2.subtract(img,blur),cv2.subtract(blur,img))
        diff = cv2.cvtColor(diff, cv2.COLOR_BGR2GRAY)
        
        cv2.imwrite("output/!im/out_z" + numbers.replace(" ","") + "_" + str(i+1) + "_" + numbers[i].replace(" ","_") + ".png", diff)
        frame[args.digits_y, args.digits_x+args.digits_spacing*i:args.digits_x+args.digits_spacing*i+args.digits_width, :] = 255
        frame[args.digits_y+args.digits_height, args.digits_x+args.digits_spacing*i:args.digits_x+args.digits_spacing*i+args.digits_width, :] = 255
        frame[args.digits_y:args.digits_y+args.digits_height, args.digits_x+args.digits_spacing*i+args.digits_width, :] = 255
        frame[args.digits_y:args.digits_y+args.digits_height, args.digits_x+args.digits_spacing*i, :] = 255

        score = []
        #score_addn = [-0.02, 0.015, 0, 0.01, 0,  0, 0, 0.015, 0, 0,  0.07]
        score_addn = [0,0.01,0,0.01,0, 0,0,0,0,0, 0.03]
        for j in range(11):
            template = templates[j]
            res = cv2.matchTemplate(diff,template,cv2.TM_SQDIFF_NORMED)
            score.append((j,res.min(axis=0).min(axis=0) + score_addn[j]))

        score = sorted(score, key=lambda x: x[1])
        pred = score[0][0] if score[0][1] < 0.18 else 10
        output_digits += " " if pred == 10 else str(pred)

    if output_digits != numbers:
        print("Warning: digit reader failed on " + str(numbers) + " (read " + output_digits + ")")

    cv2.imwrite("output/!im/out_" + numbers.replace(" ","") + ".png", frame)

def handle_chenter_image(frame):
    height,width = frame.shape[:2]

    x = width
    y = height//20
    window4 = frame[10*y:13*y, 0:x, :]
    col_max = window4.max(axis=2).max(axis=0)
    black_space = []
    white_space = []
    count = 0
    white = True
    for i in range(len(col_max)):
        if col_max[i] > args.letter_intensity_thresh:
            if not white:
                black_space.append(count)
                count = 0
            white = True
            count += 1
        else:
            if white and count > 0:
                white_space.append(count)
                count = 0
            white = False
            count += 1
    black_space.append(count)

    #if len(white_space) >= 8 and len(white_space) <= 12 and max(white_space) > width * 25/960 and max(white_space) < width * 90/960 and black_space[0] > width/6 and black_space[-1] > width/6 and (black_space[-2] > white_space[1]/2 or black_space[-3] > white_space[1]/2):
    #    print("Sublevel")

    #print(white_space)
    #print(black_space)

    window2 = frame[4*y:8*y, width//4:3*width//4, :]
    average2 = window2.mean(axis=0).mean(axis=0)
    #print(average2)

    print("Recommend set chenter_redness=" + str(int((average2[2]-average2[0])*0.42)))

    frame[4*height//20,:,:] = 255
    frame[8*height//20,:,:] = 255
    frame[10*height//20,:,:] = 255
    frame[13*height//20,:,:] = 255
    cv2.imwrite("output/!im/out_challenge_mode_enter.png", frame)


def draw_letters_on_image(img_in, cave_name):
    # find the expected with of the image
    sum_img = img_in.copy()
    height,width = img_in.shape[:2]
    temp_w = 0
    temp_h = 0
    for l in cave_name:
        if l in letters:
            img = letters[l]
            temp_h = max(temp_h, img.shape[0])
            temp_w += img.shape[1]
        else:
            temp_w += letters['l'].shape[1] * other_letters_w[l]

    # compute the letters postions, scrunching if necesary
    xs0 = []
    xs1 = []
    x_off = int(width/2 - temp_w/2 + int(args.letters_xoffset*width))
    y_off = int(height*args.letters_yoffset)
    x_scrunch = 1
    x_scrunch_limit = args.x_scrunch_limit
    sum_img[:,:,0]=0
    sum_img[:,:,2]=0
    if temp_w > x_scrunch_limit * width:
        x_scrunch = x_scrunch_limit * width/temp_w
        x_off = int(width * (1-x_scrunch_limit)//2)+int(args.letters_xoffset*width)
        #print("x scr " + str(x_scrunch))
        sum_img[:,int(width*(1-x_scrunch_limit)//2)+int(args.letters_xoffset*width),0] = 255
        sum_img[:,int(width-width*(1-x_scrunch_limit)//2)+int(args.letters_xoffset*width),0] = 255
    for l in cave_name:
        xs0.append(int(x_off))
        if l in letters:
            if args.images:
                img = letters[l].copy()
                img[:,:,1] = 0
                img[:,:,0] = 0
                img = cv2.resize(img, (int(img.shape[1]*x_scrunch), img.shape[0] ), interpolation=cv2.INTER_NEAREST)
                sum_img[y_off:y_off+img.shape[0], int(x_off):int(x_off)+img.shape[1], :] = sum_img[y_off:y_off+img.shape[0], int(x_off):int(x_off)+img.shape[1], :] + img
            x_off += letters[l].shape[1]*x_scrunch
        else:
            x_off += letters['l'].shape[1] * other_letters_w[l] * x_scrunch
        xs1.append(int(x_off))

    # determine vertical cutoffs to use
    xs0_use = []
    xs1_use = []
    spacing_needed = int(width * 0.008)
    #print("sp " + str(spacing_needed))
    for i,l in enumerate(cave_name):
        if l not in letters:
            xs0_use.append(xs0[i])
            xs1_use.append(xs1[i])
            continue
        x0 = max(xs0[i]+1, xs1[i-1]-int((letters[cave_name[i-1]].shape[1]-letters_xoff[cave_name[i-1]]-letters_width[cave_name[i-1]])*x_scrunch)+spacing_needed if i>0 and cave_name[i-1] in letters else 0)
        x1 = min(xs1[i]-1, xs0[i+1]+int(letters_xoff[cave_name[i+1]]*x_scrunch)-spacing_needed if i+1<len(xs0) and cave_name[i+1] in letters else width)
        if i+1<len(xs0) and cave_name[i+1]=="'":
            x1 -= spacing_needed
        if x0 + 4 >= x1:
            diff = (x0 + 4 - x1) // 2
            x0 -= diff
            x1 += diff
        xs0_use.append(x0)
        xs1_use.append(x1)
        if args.images:
            sum_img[0:height//2,x0,0] = 255
            sum_img[0:height//2,x1,0] = 255
    sum_img[4*height//20,:,:] = 255
    sum_img[8*height//20,:,:] = 255
    sum_img[10*height//20,:,:] = 255
    sum_img[13*height//20,:,:] = 255
    return sum_img

def adjust_gamma(image, gamma=1.0):
	# build a lookup table mapping the pixel values [0, 255] to
	# their adjusted gamma values
	invGamma = 1.0 / gamma
	table = np.array([((i / 255.0) ** invGamma) * 255
		for i in np.arange(0, 256)]).astype("uint8")
	# apply gamma correction using the lookup table
	return cv2.LUT(image, table)

try:
    os.mkdir(args.templates)
except FileExistsError:
    pass
try:
    os.mkdir("output/")
except FileExistsError:
    pass
try:
    os.mkdir("output/!im/")
except FileExistsError:
    pass

# function to process all of the existing files...

def process_align_frames():
    print()
    generate_args()
    letter_templates()
    digit_templates()
    global recommend_chresult_color, bs, gs, rs
    bs = []
    gs = []
    rs = []
    recommend_chresult_color = False

    file_names = glob.glob("output/!im/*.png")

    for file_name in file_names:
        if "raw_" in file_name or "out_" in file_name or "debug_" in file_name: continue
        #print(file_name)

        # resize and crop
        frame = cv2.imread(file_name)
        height,width = frame.shape[:2]
        if (height != args.resize_y or width != args.resize_x) and args.resize:
            frame = cv2.resize(frame, (args.resize_x,args.resize_y), interpolation=cv2.INTER_NEAREST)
        height,width = frame.shape[:2]
        if args.crop:
            frame = frame[args.crop_y1:args.crop_y2,args.crop_x1:args.crop_x2,:]
        if args.gamma != 1:
            frame = adjust_gamma(frame, args.gamma)

        # get stats about the image
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
        

        comp_name = file_name.replace("output/!im","").replace(".png","").replace("/","").replace("\\","")
        #print(comp_name)
        frame_type = str(get_screen_type(frame))
        if comp_name.lower() == "fadeout":
            # detect fadeout darkness and recommend parameter changes
            darkness = frame.max(axis=0).max(axis=0).max(axis=0)
            print("Recommend set fadeout_frame_intensity=" + str(int(darkness+5.99)))
            print("Recommend set letter_intensity_thresh=" + str(int(darkness+10.99)))
            cv2.imwrite("output/!im/out_"+comp_name+".png", frame)
            if "fadeout" != frame_type:
                print("Warning, fadeout not detected as type fadeout, was " + frame_type)
            pass
        elif comp_name == "challenge_mode_enter":
            # recommend more param changes
            handle_chenter_image(frame)
            if "chenter" != frame_type:
                print("Warning, challenge_mode_enter not detected as type chenter, was " + frame_type)
            pass
        elif comp_name.isdigit():
            # pull out some digits and recommend color parameters
            pull_numbers_from_image(frame, comp_name)
            recommend_chresult_color = True
            if "chresult" != frame_type:
                print("Warning, " + comp_name + " not detected as type chresult, was " + frame_type)
            pass
        else:
            cv2.imwrite("output/!im/out_"+comp_name+".png", draw_letters_on_image(frame, comp_name.replace("_"," ")))
            if "storyenter" != frame_type:
                print("Warning, " + comp_name + " not detected as type storyenter, was " + frame_type)
            pass

    if recommend_chresult_color:
        print("Recommend set chresult_color_b=" + str(int(np.mean(bs))))
        print("Recommend set chresult_color_g=" + str(int(np.mean(gs))))
        print("Recommend set chresult_color_r=" + str(int(np.mean(rs))))





# use the current video
videoFile=args.camera
if len(sys.argv) >= 2:
    videoFile = sys.argv[1]
else:
    if videoFile == 'find':
        list_of_files = glob.glob(args.video_path + "*")
        videoFile = max(list_of_files, key=os.path.getctime)
    elif isinstance(args.camera, int):
        print("make sure camera is a video, not virtual cam")
        sys.exit(0)
    elif "/" not in videoFile and "\\" not in videoFile:
        videoFile = args.video_path + args.camera
    
cap = cv2.VideoCapture(videoFile)

if (cap.isOpened() == False): 
    print("Failure - error opening video stream " + str(videoFile))
    print("make sure video path exists")
    sys.exit(0)

frame_count = 0

process_align_frames()

print("""
Seed detection setup
-Press q to quit
-Press s to save the current frame to output/!im
-Press d to advance one frame
-Press f to advance one second
-Press g to advance one minute
-Press p to process all of the current frames in output/!im
-In output/!im, you should name the alignment images e.g. "fadeout" "Emergence_Cave" "12345" "challenge_mode_enter"
""")

while(cap.isOpened()):
    ret, frame = cap.read()
    frame_count += 1

    if not ret:
        break

    cv2.imshow('Frame',frame)
    done = False
    while True:
        key_press = cv2.waitKey(0)
        if key_press & 0xFF == ord('q'):
            done = True
            break
        if key_press & 0xFF == ord('g'):
            cap.set(cv2.CAP_PROP_POS_FRAMES, cap.get(cv2.CAP_PROP_POS_FRAMES)+30*60)
            break
        if key_press & 0xFF == ord('f'):
            cap.set(cv2.CAP_PROP_POS_FRAMES, cap.get(cv2.CAP_PROP_POS_FRAMES)+30)
            break
        if key_press & 0xFF == ord('d'):
            break
        if key_press & 0xFF == ord('s'):
            print("saving frame to " + "output/!im/raw_"+str(frame_count)+".png")
            cv2.imwrite("output/!im/raw_"+str(frame_count)+".png", frame)
            break
        if key_press & 0xFF == ord('p'):
            process_align_frames()
    if done:
        break

cap.release()
cv2.destroyAllWindows()
print("exit")