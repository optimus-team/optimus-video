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
    cmd = "ffmpeg -i stream.sdp -vcodec libx264 -acodec aac -strict -2 -y ~/tmp/out.mp4 &"
    import os
    #  os.spawnl(os.P_DETACH, cmd)
    os.system(cmd)

    return 'ok'


if __name__ == "__main__":
    app.run('0.0.0.0')
