import os
import datetime
from flask import Flask, request, jsonify

app = Flask(__name__)
app.config['JSON_AS_ASCII'] = False

UPLOAD_PATH = os.path.join(os.path.dirname(__file__), 'uploads')

@app.route("/upload", methods=["POST"])
def upload():
    if request.method == "POST":
        file = request.files.get('file')
        if all([file]):
            filename = file.filename
            file.save(os.path.join(UPLOAD_PATH, filename))
            msg = "upload success!"
        else:
            msg = "upload failed :("

        return msg


if __name__ == "__main__":
    app.run(host="0.0.0.0")