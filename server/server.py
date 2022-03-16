import datetime
import json
import sqlite3
from concurrent.futures import ThreadPoolExecutor

import flask
import hashlib

from flask import render_template
from werkzeug.wrappers import Response
import Users

app = flask.Flask(__name__)

users = {}

executor = ThreadPoolExecutor()
adminuname = "root"
adminpasswd = "root"

newmsg = False
recv = ""


def getdata():
    conn = sqlite3.connect("chat.db")
    cur = conn.cursor()
    cur.execute("SELECT * FROM users")
    data = cur.fetchall()
    conn.close()
    return data


@app.route('/', methods=['POST', 'HEAD', 'GET'])
def chat():
    if flask.request.method == 'POST':
        try:
            msg_received = flask.request.get_json()
            msg_subject = msg_received["subject"]

            if msg_subject == "register":
                register_ret = executor.submit(register, (msg_received))
                return register_ret.result()

            elif msg_subject == "login":
                login_ret = executor.submit(login, (msg_received))
                return login_ret.result()

            elif msg_subject == "logout":
                logout_ret = executor.submit(logout, (msg_received))
                return logout_ret.result()

            elif msg_subject == "getchat":
                getchat_ret = executor.submit(getchat, (msg_received))
                return getchat_ret.result()

            elif msg_subject == "sendmessage":
                sendmessage_ret = executor.submit(gotmessage, (msg_received))
                return sendmessage_ret.result()
            else:
                return "Invalid request"
        except:
            return Response("")

    elif flask.request.method == "HEAD":
        return Response("")

    elif flask.request.method == "GET":
        return render_template("index.html")


def register(msg_received):
    conn = sqlite3.connect(database="chat.db")
    cur = conn.cursor()
    firstname = msg_received["firstname"]
    lastname = msg_received["lastname"]
    username = msg_received["username"]
    password = msg_received["password"]

    cur.execute("SELECT * FROM users WHERE username =?", (username,))
    records = cur.fetchall()
    if len(records) != 0:
        return "Another user used the username. Please chose another username."

    try:
        now = datetime.datetime.now()
        cur.execute(
            "INSERT INTO users (first_name, last_name, username, password,registration_date) VALUES (?, ?, ?, ?, ?)",
            (firstname, lastname, username, str(hashlib.md5(password.encode()).hexdigest()), now))
        conn.commit()
        conn.close()
        return "success"
    except Exception as e:
        print("Error while inserting the new record :", repr(e))
        return "failure"


def login(msg_received):
    conn = sqlite3.connect(database="chat.db")
    cur = conn.cursor()
    username = msg_received["username"]
    password = msg_received["password"]

    cur.execute("SELECT first_name, last_name FROM users WHERE username =? AND password=?",
                (username, hashlib.md5(password.encode()).hexdigest()))
    records = cur.fetchall()
    conn.close()

    if len(records) == 0:
        return "failure"
    else:
        name = records[0][0]
        lname = records[0][1]
        for i in users.values():
            if i.get() == username:
                print("username already logged in")
                print(users)
                return "already"
        else:
            users[username] = Users.Users(username, name, lname)
            print(users)
            authkey = users[username].getkey()
            return f"success,{authkey}"


def logout(msg_received):
    key = msg_received["key"]
    temp = []
    for i in users.values():
        if int(key) == i.getkey():
            temp.append(i.getunamebykey(int(key)))
    try:
        del users[temp[0]]
    except:
        pass
    print(users)
    return "logout"


def getchat(msg_received):
    global newmsg,recv
    with open("messages.json", 'r') as file:
        messages = json.load(file)
        return messages



def gotmessage(msg_received):
    global newmsg,recv
    newmsg = True
    key = msg_received["key"]
    username = ''
    for i in users.values():
        if int(key) == i.getkey():
            username = i.getunamebykey(int(key))
            break


    data = msg_received["message"]

    with open("messages.json", 'r') as file:
        messages = json.load(file)
    with open("messages.json", 'w') as file:
        messages[username] = {"text": data, "user": username}
        json.dump(messages, file)

    recv = [data,username]
    return "successwrite"


@app.route("/insert", methods=["POST"])
def insert():
    try:
        name = flask.request.form["name"]
        lname = flask.request.form["lname"]
        user = flask.request.form["username"]
        password = flask.request.form["password"]

        mydict = {"firstname": name, "lastname": lname, "username": user, "password": password}

        status = register(mydict)
        data = getdata()
        if status == "success":
            return render_template("admin.html", text=data, users=list(users.keys()))
        else:
            return render_template("admin.html", text=data, errortext="user already exists")

    except:
        return render_template("admin.html", errortext="An error has occurred")


@app.route("/admin", methods=["POST"])
def admin():
    try:
        uname = flask.request.form["username"]
        passwrd = flask.request.form["password"]
        if uname == adminuname and passwrd == adminpasswd:
            data = getdata()
            return render_template("admin.html", text=data, users=list(users.keys()))
        else:
            return render_template("index.html", errortext="Wrong username and password")
    except:
        return render_template("index.html", errortext="An error has occurred")


@app.route("/logout_user", methods=["POST"])
def logout_user():
    username = flask.request.form["username"]
    data = getdata()
    temp = []
    for i in users.keys():
        temp.append(i)
        if i == username:
            mydict = {"key": users[i].getkey()}
            status = logout(mydict)
        else:
            status = ""
        if status == "logout":
            return render_template("admin.html", text=data, users=list(users.keys()))
        else:
            return render_template("admin.html", text=data, users=list(users.keys()), errortext="User not found")


if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000, debug=True, threaded=True)
