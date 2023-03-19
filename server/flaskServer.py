import redis
import time
import json
import sys
import os
import random

from flask import Flask, request, jsonify
from flask_cors import CORS
from gevent import pywsgi
from clickhouse_driver import Client

app = Flask(__name__)
app.debug = True
app.threaded = True
CORS(app, supports_credentials=True)
pool = redis.ConnectionPool(host='127.0.0.1', port=6379, db=0, password='', decode_responses=True)
client = Client(host='127.0.0.1', port=19000, database='default', user='', password='', send_receive_timeout=25)


def getRedisConn():
    redisConn = redis.Redis(connection_pool=pool)
    return redisConn


@app.route('/api/deviceData/<clientid>', methods=["POST"])
def deviceData(clientid):
    data = request.get_json()
    if data:
        payload = data['payload']
        timestamp = data['timestamp']
        if clientid and payload and timestamp:
            sql = "INSERT INTO default.device_data " \
                  "VALUES(%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,'%s',%d,%d)" % \
                  (payload['temp'], payload['humi'], payload['lux'], payload['weight'], payload['soilTemp'], payload['soilHumi'], payload['soilEC'], payload['soilPH'], payload['waterHeight'], payload['light'], payload['fan'], payload['waterPump'], clientid, timestamp/1000, timestamp/1000)
            res = client.execute(sql)
            return "ok"
        else:
            return "error"
    else:
        return "error"


@app.route('/api/deviceConnection/<clientid>', methods=["POST"])
def deviceConnection(clientid):
    data = request.get_json()
    if data:
        connection = data['connection']
        timestamp = data['timestamp']
        if clientid and connection and timestamp:
            key = "seu/device/{}".format(clientid)
            val = {
                "connection": connection,
                "timestamp": timestamp
            }
            redisConn = getRedisConn()
            redisConn.hmset(key, val)
            return "ok"
        else:
            return "error"
    else:
        return "error"


@app.route('/api/userGetDeviceData', methods=["GET"])
def userGetDeviceData():
    uid = request.args.get("uid")
    token = request.args.get("token")
    did = request.args.get("did")
    dataType = request.args.get("dataType")
    period = request.args.get("period")
    if uid and token and did and dataType and period:
        key = "seu/token/{}".format(uid)
        redisConn = getRedisConn()
        if redisConn.exists(key):
            redis_token = redisConn.get(key)
            if redis_token == token:
                if period == "1d":
                    timestampStart = int(time.time())-86400
                    timestampEnd = int(time.time())
                    sql = "SELECT %s FROM default.device_data " \
                      "WHERE device_id = '%s' AND coll_time>%d ORDER BY coll_time" % \
                      (dataType, did, timestampStart)
                    res = client.execute(sql)
                    resInfo = {
                        "status": "1",
                        "data": str(res),
                        "time": str(timestampEnd)
                    }
                    return json.dumps(resInfo)
                else:
                    resInfo = {
                        "status": "2"
                    }
                    return json.dumps(resInfo)
        resInfo = {
            "status": "0"
        }
        return json.dumps(resInfo)
    else:
        return "error"


@app.route('/api/userRegister', methods=["GET"])
def userRegister():
    uid = request.args.get("uid")
    pwd = request.args.get("pwd")
    tel = request.args.get("tel")
    if uid and pwd and tel:
        key = "seu/user/{}".format(uid)
        val = {
            "uid": uid,
            "pwd": pwd,
            "tel": tel
        }
        redisConn = getRedisConn()
        if redisConn.exists(key):
            resInfo = {
                "status": "2"
            }
        else:
            redisConn.hmset(key, val)
            resInfo = {
                "status": "1"
            }
        return json.dumps(resInfo)
    else:
        return "error"


@app.route('/api/userLogin', methods=["GET"])
def userLogin():
    uid = request.args.get("uid")
    pwd = request.args.get("pwd")
    if uid and pwd:
        key = "seu/user/{}".format(uid)
        redisConn = getRedisConn()
        if redisConn.exists(key):
            info = redisConn.hgetall(key)
            if info['pwd'] == pwd:
                key = "seu/token/{}".format(uid)
                timeStamp = int(time.time()) # 获取当前时间戳
                token = "{}{}".format(uid, timeStamp)
                redisConn.set(key, token, ex=2592000)
                resInfo = {
                    "status": "1",
                    "uid": uid,
                    "tel": info['tel'],
                    "token": token,
                    "login_time": time.strftime("%Y/%m/%d %H:%M:%S")
                }
                if "did" in info:
                    resInfo["did"] = info['did']
            else:
                resInfo = {
                    "status": "2"
                }
        else:
            resInfo = {
                "status": "0"
            }
        return json.dumps(resInfo)
    else:
        return "error"


@app.route('/api/userCheckToken', methods=["GET"])
def userCheckToken():
    uid = request.args.get("uid")
    token = request.args.get("token")
    if uid and token:
        key = "seu/token/{}".format(uid)
        redisConn = getRedisConn()
        if redisConn.exists(key):
            redis_token = redisConn.get(key)
            if redis_token == token:
                resInfo = {
                    "status": "1"
                }
                return json.dumps(resInfo)
        resInfo = {
            "status": "0"
        }
        return json.dumps(resInfo)
    else:
        return "error"


@app.route('/api/userCheckDevice', methods=["GET"])
def userCheckDevice():
    uid = request.args.get("uid")
    token = request.args.get("token")
    did = request.args.get("did")
    if uid and token and did:
        key = "seu/token/{}".format(uid)
        redisConn = getRedisConn()
        if redisConn.exists(key):
            redis_token = redisConn.get(key)
            if redis_token == token:
                key = "seu/device/{}".format(did)
                if redisConn.exists(key):
                    info = redisConn.hgetall(key)
                    if "uid" in info:
                        if info['uid'] == uid:
                            if "connection" in info:
                                resInfo = { 
                                    "status": "1",
                                    "connection": info['connection']
                                }
                                return json.dumps(resInfo)
                            else:
                                resInfo = { 
                                    "status": "1",
                                    "connection": "unknown"
                                }
                                return json.dumps(resInfo)
        resInfo = {
            "status": "0"
        }
        return json.dumps(resInfo)
    else:
        return "error"


@app.route('/api/userLogout', methods=["GET"])
def userLogout():
    uid = request.args.get("uid")
    token = request.args.get("token")
    if uid and token:
        key = "seu/token/{}".format(uid)
        redisConn = getRedisConn()
        if redisConn.exists(key):
            redis_token = redisConn.get(key)
            if redis_token == token:
                redisConn.delete(key)
                resInfo = {
                    "status": "1"
                }
                return json.dumps(resInfo)
        resInfo = {
            "status": "0"
        }
        return json.dumps(resInfo)
    else:
        return "error"


@app.route('/api/userBindDevice', methods=["GET"])
def userBindDevice():
    uid = request.args.get("uid")
    token = request.args.get("token")
    did = request.args.get("did")
    if uid and token and did:
        key = "seu/token/{}".format(uid)
        redisConn = getRedisConn()
        if redisConn.exists(key):
            redis_token = redisConn.get(key)
            if redis_token == token:
                key = "seu/device/{}".format(did)
                if redisConn.exists(key):
                    info = redisConn.hgetall(key)
                    if "uid" in info:
                        resInfo = {
                            "status": "2"
                        }
                        return json.dumps(resInfo) 
                redisConn.hset(key, "uid", uid) 
                key = "seu/user/{}".format(uid)
                redisConn.hset(key, "did", did)
                resInfo = {
                    "status": "1"
                }
                return json.dumps(resInfo)
        resInfo = {
            "status": "0"
        }
        return json.dumps(resInfo)
    else:
        return "error"


@app.route('/api/userUnbindDevice', methods=["GET"])
def userUnbindDevice():
    uid = request.args.get("uid")
    token = request.args.get("token")
    did = request.args.get("did")
    if uid and token and did:
        key = "seu/token/{}".format(uid)
        redisConn = getRedisConn()
        if redisConn.exists(key):
            redis_token = redisConn.get(key)
            if redis_token == token:
                key = "seu/user/{}".format(uid)
                redisConn.hdel(key, "did")
                key = "seu/device/{}".format(did)
                redisConn.hdel(key, "uid")
                resInfo = {
                    "status": "1"
                }
                return json.dumps(resInfo)
        resInfo = {
            "status": "0"
        }
        return json.dumps(resInfo)
    else:
        return "error"


@app.route('/api/userChangePwd', methods=["GET"])
def userChangePwd():
    uid = request.args.get("uid")
    pwd = request.args.get("pwd")
    newpwd = request.args.get("newpwd")
    if uid and pwd and newpwd:
        key = "seu/user/{}".format(uid)
        redisConn = getRedisConn()
        if redisConn.exists(key):
            info = redisConn.hgetall(key)
            if info['pwd'] == pwd:
                redisConn.hset(key, "pwd", newpwd)       
                key = "seu/token/{}".format(uid)
                if redisConn.exists(key):
                    redisConn.delete(key)      
                resInfo = {
                    "status": "1"
                }
            else:
                resInfo = {
                    "status": "2"
                }
        else:
            resInfo = {
                "status": "0"
            }
        return json.dumps(resInfo)
    else:
        return "error"


if __name__ == '__main__':
    server = pywsgi.WSGIServer(('0.0.0.0', 8082), app)
    server.serve_forever()