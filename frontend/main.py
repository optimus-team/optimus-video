import tornado.web
import tornado.ioloop
import tornado.httpserver
from tornado.options import define,options
import os
import time
import subprocess

define("port",default=8080,help="Frontend port",type=int)
options.parse_command_line()


class Application(tornado.web.Application):
	def __init__(self):
		handlers = [
			(r"/",MainHandler),
			(r"/home",HomeHandler),
			(r"/video",VideoHandler)

		]

		settings = dict(
			debug = True,
			template_path = os.path.join(os.path.dirname(__file__),"templates"),
			static_path = os.path.join(os.path.dirname(__file__),"static"),
			login_url = "/auth",
			cookie_secret = "Settings.COOKIE_SECRET",
		)
		tornado.web.Application.__init__(self,handlers,**settings)


class BaseHandler(tornado.web.RequestHandler):
	def prepare(self):
		pass

class MainHandler(BaseHandler):
	def get(self):
		self.redirect("/home")

class HomeHandler(BaseHandler):
	def get(self):
		self.render("home.html")

class VideoHandler(BaseHandler):
	def post(self):
		sdp_headers = self.get_argument('sdp',None)
		with open('/root/optimus-video/data','w') as f:
			f.write(sdp_headers)

		ts = time.strftime("%Y-%m-%d-%H:%M:%S") + ".mp4"
		cmd = "ffmpeg -i stream.sdp -vcodec libx264 -acodec aac -strict -2 -y ~/tmp/video/{}".format(ts)
		supbrocess.Popen(cmd,shell=True)


if __name__ == "__main__":
	http_server = tornado.httpserver.HTTPServer(Application())
	http_server.listen(options.port)
	tornado.ioloop.IOLoop.instance().start()

