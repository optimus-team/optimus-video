from flask import Flask
app = Flask(__name__)

@app.route("/", methods=["GET", "POST"])
def hello():
    from flask import request
    #  print dir(request)
    #  print request.values
    sdp_headers = request.form.get('sdp')

    with open('./stream.sdp', 'w') as f:
        f.write(sdp_headers)

    import time
    ts = time.strftime("%Y-%m-%d-%H:%M:%S") + '.mp4'
    cmd = "ffmpeg -i stream.sdp -vcodec libx264 -acodec aac -strict -2 -y ~/tmp/video/{}".format(ts)
    import subprocess
    subprocess.Popen(cmd, shell=True) # it's async

    return 'ok'


if __name__ == "__main__":
    app.run('0.0.0.0')
