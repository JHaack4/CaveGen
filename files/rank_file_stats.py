import numpy as np

# run command:
# run seed pod -noimages -dontstorejudge -num 100000 -judge pod rankfile

with open("rank_file.txt") as f:
    for line in f:
        line = line.strip()
        x = line.split(";")
        nm = x[0]
        x = [float(x) for x in x[1:]]
        print(f"{nm:20s} {x[0]:10.2f} {x[500]:10.2f} {x[999]:10.2f} {np.std(x):10.2f}")

