import os, tempfile
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
                     (r"/uploads/(.*)", tornado.web.StaticFileHandler, 
                        {"path": os.path.join(os.path.dirname(__file__), "files/uploads")}) ]

        tornado.web.Application.__init__(self, handlers)

class HomeHandler(tornado.web.RequestHandler):

    def get(self):

        self.finish('Upload File: ');

class UploadHandler(tornado.web.RequestHandler):

    def post(self):
        tmp_file = tempfile.NamedTemporaryFile(suffix=".jpg")
        tmp_name = tmp_file.name.split("/")[-1]
        output_file = open("files/uploads/" + tmp_name, 'w')

        transformation_name = self.request.headers['Transformation']
        image = self.request.files['data'][0]
        output_file.write(image['body'])
        image_path = "%s/files/uploads/%s" % (os.getcwd(), tmp_name)
       
        processing_function = transformations_dict[transformation_name]
        processing_function(image_path)
        
        img_URL = "http://10.0.2.2:8000/uploads/%s" % tmp_name

        print image_path
        print img_URL

        self.finish(img_URL)


def main():


    http_server = tornado.httpserver.HTTPServer(Application())
    http_server.listen(options.port)

    tornado.ioloop.IOLoop.instance().start()
    
if __name__ == "__main__":
    main()
