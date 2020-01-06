#include <Servo.h> //서보 라이브러리를 불러옵니다.
#include <ESP8266HTTPClient.h>
#include <ESP8266WiFi.h>
#include <ArduinoJson.h>
 
Servo myservo;  // 서보를 제어할 서보 오브젝트를 만듭니다.

               
#define echo  5
#define trig  16
#define piezo  14
#define servoPin  4

#define ssid "Fish"
#define pw "77777777"

#define server_adress "http://10.156.147.135:5000/science-project/api/sensor-data"

void start_wifi(){
    WiFi.begin(ssid, pw);

    while (WiFi.status() != WL_CONNECTED) {  //Wait for the WiFI connection
 
        delay(500);
        Serial.println("Waiting for connection");
    }
    Serial.println(WiFi.localIP());
}

void setup() 
{ 
    Serial.begin(115200);
    myservo.attach(servoPin);  // 핀 9의 서보를 서보 오브젝트에 연결합니다.
    pinMode(trig, OUTPUT);
    pinMode(echo, INPUT);

    start_wifi();
} 


int get_distance(){
    float distance;
    unsigned long duration;
    
    digitalWrite(trig, LOW);
    digitalWrite(echo, LOW);
    delayMicroseconds(2);
    digitalWrite(trig, HIGH);
    delayMicroseconds(10);
    digitalWrite(trig, LOW);

    duration = pulseIn(echo,HIGH);
    distance = ((float)(340 * duration) / 10000) / 2;

    return (int)distance;
} 

void turn_servo(){
    static int pos = 0;
    static int pos_step;

    if(pos >= 180)pos_step = -1;
    else if(pos <= 0)pos_step = 1;
    
    pos += pos_step;
    myservo.write(pos);   
}

bool is_detect(int sensed_distance){
    return sensed_distance <= 20;
}

char* make_data2json(int sensed_distance, bool whether_detect){
    StaticJsonDocument<300> JSONdoc;
    static char JSONmessageBuffer[300]; //다른 함수에서 char포인터 받아와서 쓰면 error
    JSONdoc["sensed_distance"] = sensed_distance;
    JSONdoc["whether_detect"] = whether_detect;    

    serializeJsonPretty(JSONdoc, JSONmessageBuffer);
    return JSONmessageBuffer;
}

int send2server(int sensed_distance, bool whether_detect){
    if(WiFi.status() != WL_CONNECTED)return -1; //WiFi연결 확인

    
    HTTPClient http;
    char* JSONmessageBufferPtr;    
    int httpCode;
    String payload;

    JSONmessageBufferPtr = make_data2json(sensed_distance, whether_detect);
    Serial.println(JSONmessageBufferPtr);

    
    http.begin(server_adress);
    http.addHeader("Content-Type", "application/json");

    httpCode = http.POST(JSONmessageBufferPtr);
    payload = http.getString();                        

    if(httpCode < 0){
        Serial.println("unknown http error" + String(http.errorToString(httpCode)));   
    }
    else{
        Serial.println(httpCode);   //Print HTTP return code
        Serial.println(payload);    //Print request response payload
    } 
    
    http.end();  //Close connection    
    return httpCode;
}

void loop(){
    int sensed_distance;
    bool whether_detect;
    
    turn_servo();
    sensed_distance = get_distance();
    whether_detect = is_detect(sensed_distance);
   
    if(send2server(sensed_distance, whether_detect) < 0){
        Serial.println("Unknow http error occured");
    }
  
    delay(50);                           
}
