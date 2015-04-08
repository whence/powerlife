from bottle import route, run, static_file
import os

lms_root = os.path.expanduser('~/lms')

@route('/')
def index():
    return static_file('index.html', root='app')

@route('/app/<filepath:path>')
def serve_app(filepath):
    return static_file(filepath, root='app')

@route('/static/<filepath:path>')
def serve_static(filepath):
    return static_file(filepath, root='bower_components')

@route('/lms/<filepath:path>')
def serve_lms(filepath):
    return static_file(filepath, root=lms_root)

def get_albums(root):
    return [{'name': album, 'path': os.path.join(subdir, album)}
            for subdir in os.listdir(root) if os.path.isdir(os.path.join(root, subdir))
            for album in os.listdir(os.path.join(root, subdir)) if os.path.isdir(os.path.join(root, subdir, album))]

def get_photo_urls(root, path, gen_url):
    return [gen_url(filename) for filename in os.listdir(os.path.join(root, path)) if filename.endswith('.jpg') or filename.endswith('.jpeg')]

albums = get_albums(lms_root)

@route('/api/albums')
def api_albums():
    return {'albums': albums}

@route('/api/albums/<filepath:path>')
def api_album(filepath):
    return {'photos': get_photo_urls(lms_root, filepath, lambda filename: '/lms/' + filepath + '/' + filename)}

run(host='localhost', port=8080)
