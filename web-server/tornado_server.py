import os, tempfile, shutil
import tornado.httpserver, tornado.ioloop, tornado.options, tornado.web
from tornado.options import define, options #test
from SimpleCV import *


define("port", default=8000, help="run on the given port", type=int)

dir_modified = "files/uploads/modified/"
dir_original = "files/uploads/original/"

def get_edges(image_path):
    img = Image(image_path).edges()
    img.save(image_path)
    return

def invert(image_path):
    img = Image(image_path).invert()
    img.save(image_path)
    return

def tv(image_path):
    tv_original = Image("family_watching_television_1958.jpg", sample=True)
    tv_coordinates = [(353, 379), (433,380),(432, 448), (354,446)]
    tv_mask = Image(tv_original.size()).invert().warp(tv_coordinates)
    tv = tv_original - tv_mask

    bwimage = Image(image_path).grayscale().resize(tv.width, tv.height)
    on_tv = tv + bwimage.warp(tv_coordinates)

    on_tv.save(image_path)
    return

def wanted(image_path, rotation):

    if (rotation/90) % 2 == 0:
        width = 240
        height = 180
        pos = (90, 245)
    else:
        width = 180
        height = 240
        pos = (100, 240)

    face = Image(image_path).edges().binarize().erode().smooth().scale(width,height)
    (r, g, b) = face.splitChannels()
    r = r*1.25
    brown = face.mergeChannels(r, g, b)

    background = Image('files/wanted.jpg')

    mask = brown.invert()
    wanted = background.blit(brown, pos, alphaMask=mask)
    wanted.save(image_path)
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

transformations_dict = {'edges': get_edges, 'wanted' : wanted,
                        'invert': invert, 'tv': tv,
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

        if transformation_name == 'wanted':
            processing_function(modified_path, rotation)
        else:
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
