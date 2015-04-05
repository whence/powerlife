from bottle import route, run, static_file

@route('/')
def index():
    return static_file('index.html', root='app')

@route('/static/<filepath:path>')
def serve_static(filepath):
    return static_file(filepath, root='bower_components')

@route('/app/<filepath:path>')
def serve_app(filepath):
    return static_file(filepath, root='app')

run(host='localhost', port=8080)
