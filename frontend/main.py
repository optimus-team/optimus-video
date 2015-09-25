import tornado.web
import tornado.ioloop
import tornado.httpserver


class Application(tornado.web.Application):
	def __init__(self):
		handlers = [
			(r"/ws",WebSocketHandler),
		]

		settings = dict(
			debug = True,
			template_path = os.path.join(os.path.dirname(__file__),"templates"),
			static_path = os.path.join(os.path.dirname(__file__),"static"),
			login_url = "/auth",
			cookie_secret = "Settings.COOKIE_SECRET",
		)
		tornado.web.Application.__init__(self,handlers,**settings)


class BaseHandler(tornado.web.requestHandler):
	def prepare(self):
		pass


class HomeHandler(BaseHandler):
	self.render("home.html")