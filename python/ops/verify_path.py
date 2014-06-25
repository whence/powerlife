import os

paths = os.environ['PATH'].split(os.pathsep)
for path in paths:
    if len(path.strip()) == 0:
        print('empty path detected')
    elif not os.path.exists(path):
        print('{} does not exist'.format(path))
