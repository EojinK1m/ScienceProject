from flask import Flask, request, jsonify
from flask_restful import Resource, Api
from datetime import datetime

app = Flask(__name__)
api = Api(app)

existing_data = {
    "time": "0000-00-00 00:00",
    "sensed_distance": 0,
    "whether_detect" : False
}
sensor_logs = []


def check_json_validate(checking_json):
    correct_keys = existing_data.keys()

    for key in correct_keys:
        if key not in checking_json:
            return False

    return True

# def check_date_correct(date_str):
#     date_form = re.compile('.*-.*-.* .*:.*')
#
#     if date_form.match(date_str) is not None:
#         if len(date_str) is 16:
#             return True
#     return False


def save_existing_data():
    sensor_logs.append(existing_data)


class SensorData(Resource):


    def get(self):
        return jsonify(existing_data)

    def post(self):
        global existing_data

        new_data = request.get_json()
        now = datetime.now()
        new_data['time'] = '20'+now.strftime("%y-%m-%d %H:%M")

        save_existing_data()
        existing_data = new_data
        print(new_data)


api.add_resource(SensorData, '/science-project/api/sensor-data')

if(__name__ == '__main__'):
    app.run(host="10.156.147.135", port=5000, debug=True)
