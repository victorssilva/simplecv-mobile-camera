import os, tempfile, shutil
import tornado.httpserver, tornado.ioloop, tornado.options, tornado.web
from tornado.options import define, options #test
from SimpleCV import *


define("port", default=8000, help="run on the given port", type=int)

dir_modified = "files/uploads/modified/"
dir_original = "files/uploads/original/"

def get_edges(image_path):
    img = Image(image_path)
    img = img.edges()
    img.save(image_path)
    return

def divide(image_path):
    img = Image(image_path)
    img = img / 10
    img.save(image_path)
    return

def invert(image_path):
    img = Image(image_path)
    img = img.invert()
    img.save(image_path)
    return

def dilate(image_path):
    img = Image(image_path)
    img = img.dilate(5)
    img.save(image_path)
    return

def eight_bit(image_path):
    img = Image(image_path)
    bigger = img.width if img.width > img.height else img.height
    
    if bigger < 400:
        pixel_size = 5
    elif bigger < 800:
        pixel_size = 8
    elif bigger < 1200:
        pixel_size = 14
    else:
        pixel_size = 17

    img = img.pixelize(pixel_size, levels=8)
    img.save(image_path)
    return

transformations_dict = {'edges': get_edges, 'divide': divide,
                        'invert': invert, 'dilate': dilate,
                        '8bit': eight_bit }

def fix_rotation(image_path, rotation):
    img = Image(image_path)
    img = img.rotate(-rotation, fixed=False)
    img.save(image_path)
    return

class Application(tornado.web.Application):
    def __init__(self):
        handlers = [ (r"/", HomeHandler), (r"/upload", UploadHandler),
                     (r"/uploads/modified/(.*)", tornado.web.StaticFileHandler,
                        {"path": os.path.join(os.path.dirname(__file__), dir_modified)}),
                     (r"/process", ProcessHandler) ]

        print 'SimpleCV Mobile App - Listening...\n'

        if not os.path.exists(dir_original):
            os.makedirs(dir_original)
        if not os.path.exists(dir_modified):
            os.makedirs(dir_modified)

        tornado.web.Application.__init__(self, handlers)

class HomeHandler(tornado.web.RequestHandler):

    def get(self):

        self.finish('Upload File: ');

class UploadHandler(tornado.web.RequestHandler):

    def post(self):
        tmp_file = tempfile.NamedTemporaryFile(suffix=".jpg")
        tmp_name = tmp_file.name.split("/")[-1]
        output_file = open(dir_original + tmp_name, 'w')

        image = self.request.files['data'][0]
        output_file.write(image['body'])

        image_path = os.getcwd() + dir_original + tmp_name


        img_URL = "http://10.0.2.2:8000/uploads/original/%s" % tmp_name
        #img_URL = "http://mobiletest.simplecv.org:8000/uploads/original/%s" % tmp_name

        print image_path

        self.finish(img_URL)

class ProcessHandler(tornado.web.RequestHandler):

    def post(self):
        given_path = self.request.arguments['picture'][0]
        transformation_name = self.request.arguments['transformation'][0]
        rotation = int(self.request.arguments['rotation'][0])

        file_name = given_path.split('/')[-1]
        original_path = dir_original + file_name
        modified_path = dir_modified + file_name

        shutil.copyfile(original_path, modified_path);

        fix_rotation(modified_path, rotation)
        processing_function = transformations_dict[transformation_name]
        processing_function(modified_path)

        img_URL = "http://10.0.2.2:8000/uploads/modified/%s" % file_name
        #img_URL = "http://mobiletest.simplecv.org:8000/uploads/modified/%s" % file_name

        print img_URL

        self.finish(img_URL)

def main():

    http_server = tornado.httpserver.HTTPServer(Application())
    http_server.listen(options.port)

    tornado.ioloop.IOLoop.instance().start()

if __name__ == "__main__":
    main()
