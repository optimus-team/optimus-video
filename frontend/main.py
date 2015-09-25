import tornado.web
import tornado.ioloop
import tornado.httpserver
from tornado.options import define,options


define("port",default=8080,help="Frontend port",type=int)
options.parse_command_line()


class Application(tornado.web.Application):
	def __init__(self):
		handlers = [
			(r"/",MainHandler),
			(r"/home",HomeHandler),

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
		self.redirect("/")

class HomeHandler(BaseHandler):
	def get(self):
		self.render("home.html")


if __name__ == "__main__":
	http_server = tornado.httpserver.HTTPServer(Application())
	http_server.listen(options.port)
	tornado.ioloop.IOLoop.instance().start()

