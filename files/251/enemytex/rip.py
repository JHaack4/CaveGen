import os

root = os.getcwd()

for path, subdirs, files in os.walk(root):
    for name in files:
        fullpath = os.path.join(path, name)

        if '.bti' in fullpath:
            print(fullpath)
            os.system('wimgt decode "%s"' % fullpath) 