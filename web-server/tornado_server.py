import os, tempfile, shutil
import tornado.httpserver, tornado.ioloop, tornado.options, tornado.web
from tornado.options import define, options #test
from SimpleCV import *


define("port", default=8000, help="run on the given port", type=int)

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

transformations_dict = {'edges': get_edges, 'divide': divide,
                        'invert': invert, 'dilate': dilate }


class Application(tornado.web.Application):
    def __init__(self):
        handlers = [ (r"/", HomeHandler), (r"/upload", UploadHandler),
                     (r"/uploads/modified/(.*)", tornado.web.StaticFileHandler,
                        {"path": os.path.join(os.path.dirname(__file__), "files/uploads/modified")}),
                     (r"/process", ProcessHandler) ]

        tornado.web.Application.__init__(self, handlers)

class HomeHandler(tornado.web.RequestHandler):

    def get(self):

        self.finish('Upload File: ');

class UploadHandler(tornado.web.RequestHandler):

    def post(self):
        tmp_file = tempfile.NamedTemporaryFile(suffix=".jpg")
        tmp_name = tmp_file.name.split("/")[-1]
        output_file = open("files/uploads/original/" + tmp_name, 'w') #TODO: close?

        image = self.request.files['data'][0]
        output_file.write(image['body'])
        image_path = "%s/files/uploads/original/%s" % (os.getcwd(), tmp_name)


        img_URL = "http://10.0.2.2:8000/uploads/original/%s" % tmp_name

        print image_path
        print img_URL

        self.finish(img_URL)

class ProcessHandler(tornado.web.RequestHandler):

    def post(self):
        given_path = self.request.arguments['picture'][0]
        transformation_name = self.request.arguments['transformation'][0]

        file_name = given_path.split('/')[-1]
        original_path = "files/uploads/original/" + file_name
        modified_path = "files/uploads/modified/" + file_name

        shutil.copyfile(original_path, modified_path);

        processing_function = transformations_dict[transformation_name]
        processing_function(modified_path)

        img_URL = "http://10.0.2.2:8000/uploads/modified/%s" % file_name

        print modified_path
        print img_URL

        self.finish(img_URL)

def main():

    http_server = tornado.httpserver.HTTPServer(Application())
    http_server.listen(options.port)

    tornado.ioloop.IOLoop.instance().start()

if __name__ == "__main__":
    main()
