#import tornado.ioloop
#import tornado.web
#
#class MainHandler(tornado.web.RequestHandler):
#    def get(self):
#        self.write("Hello, world")
#
#application = tornado.web.Application([
#    (r"/", MainHandler),
#])
#
#if __name__ == "__main__":
#    application.listen(8000)
#    tornado.ioloop.IOLoop.instance().start()


import tornado.httpserver, tornado.ioloop, tornado.options, tornado.web, os.path
from tornado.options import define, options

define("port", default=8000, help="run on the given port", type=int)

class Application(tornado.web.Application):
    def __init__(self):
        handlers = [ (r"/", HomeHandler), (r"/upload", UploadHandler) ]
        tornado.web.Application.__init__(self, handlers)

class HomeHandler(tornado.web.RequestHandler):

    def get(self):

        self.finish('Upload File: ');

class UploadHandler(tornado.web.RequestHandler):

    def post(self):
    
        file1 = self.request.files['data'][0]

# now you can do what you want with the data, we will just save the file to an uploads folder

        output_file = open("uploads/" + file1['filename'], 'w')

        output_file.write(file1['body'])

        self.finish('Your file has been uploaded')

def main():

    http_server = tornado.httpserver.HTTPServer(Application())
    http_server.listen(options.port)

    tornado.ioloop.IOLoop.instance().start()
    
if __name__ == "__main__":
    main()
