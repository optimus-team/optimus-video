import tornado.web
import tornado.ioloop
import tornado.httpserver
from tornado.options import define,options
import os
import time
import subprocess
import glob
from subprocess import PIPE

define("port",default=8080,help="Frontend port",type=int)
options.parse_command_line()

current_port = [5000]

pids  = []
class Application(tornado.web.Application):
	def __init__(self):
		handlers = [
			(r"/", MainHandler),
			(r"/home", HomeHandler),
			(r"/video", VideoHandler),
			(r"/port", PortHandler),
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
		files = glob.glob("static/video/*.mp4")
		data = dict()
		data["files"] = []
		for file in files:
			if "background" not in file:
				data["files"].append(file)
		print data
		self.render("home.html",**data)

class VideoHandler(BaseHandler):
	def post(self):
		sdp_headers = self.get_argument('sdp',None)
		path = "static/video"
		with open('%s/stream.sdp' % path,'w') as f:
			f.write(sdp_headers)

		ts = time.strftime("%Y_%m_%d_%H_%M_%S")
		cmd = "ffmpeg -i {}/stream.sdp -vcodec libx264 -acodec aac -strict -2 -y {}/{}.mp4 && ".format(path,path,ts,path,ts)
		pid = subprocess.Popen(cmd,stdout=PIPE,stderr=PIPE,shell=True).pid
		pids.append(pid)
		# avconv -ss 00:00:02 -i %s/%s.mp4 -vsync 1 -t 0.01 out.jpg


class PortHandler(BaseHandler):
	def get(self):
		data = "%s %s" %(current_port[0],current_port[0]+2)
		current_port[0] += 2
		if(current_port[0] >= 5500):
			current_port[0] = 5000
		self.write(data)

if __name__ == "__main__":
	http_server = tornado.httpserver.HTTPServer(Application())
	http_server.listen(options.port)
	tornado.ioloop.IOLoop.instance().start()

