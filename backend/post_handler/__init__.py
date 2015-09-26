from flask import Flask
app = Flask(__name__)

@app.route("/", methods=["GET", "POST"])
def hello():
    from flask import request
    #  print dir(request)
    print request.values
    print request.form.get('sdp')
    return 'ok'


if __name__ == "__main__":
    app.run('0.0.0.0')
