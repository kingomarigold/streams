import tornado.ioloop
import tornado.web
import tornado.template
import json
import base64
import image_resize
import os

class ImageHandler(tornado.web.RequestHandler):
    def post(self):
        my_res = dict()
        req_body = json.loads(self.request.body)
        img_data = req_body['data']
        img_decode = base64.b64decode(img_data)
        resp = image_resize.perform_resize(img_decode,os.path.splitext(req_body['fileName'])[1][1:])
        my_res['data'] = base64.b64encode(resp)
        self.set_header("Content-Type","application/json")
        self.write(my_res)

def make_app():
    return tornado.web.Application([
        (r"/image",ImageHandler),
        (r"/images/(.*)",tornado.web.StaticFileHandler,{"path": "./static/images"},),
        (r"/css/(.*)",tornado.web.StaticFileHandler,{"path": "./static/css"},),
        (r"/js/(.*)",tornado.web.StaticFileHandler,{"path": "./static/css"},),
    ],autoreload=True)

if __name__== "__main__":
    print('Running in port 8888')
    app = make_app()
    app.listen(8888)
    tornado.ioloop.IOLoop.current().start()
        
        
                
                          
