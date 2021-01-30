import os

root = os.getcwd()

for path, subdirs, files in os.walk(root):
    for name in files:
        fullpath = os.path.join(path, name)

        if '.szs' in fullpath:
            print(fullpath)
            os.system('"WSZST Decompress %s"' % fullpath) 