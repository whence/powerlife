from bottle import route, run, static_file, response, HTTPResponse
from PIL import Image
import StringIO
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

def serve_image(filepath):
    image = Image.open(filepath)
    width, height = image.size

    if width > height:
        rotated = image.transpose(Image.ROTATE_90)
        output = StringIO.StringIO()
        rotated.save(output, 'JPEG', quality=85)
        content = output.getvalue()
        image.close()
        rotated.close()
        output.close()
    else:
        output = StringIO.StringIO()
        image.save(output, 'JPEG', quality=85)
        content = output.getvalue()
        image.close()
        output.close()
        
    headers = {'content-type': 'image/jpeg', 'content-length': len(content) }
    return HTTPResponse(content, **headers)

@route('/lms/<filepath:path>')
def serve_lms(filepath):
    return serve_image(os.path.join(lms_root, filepath))

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

run(host='0.0.0.0', port=8080)
