/**
 * The MySensors Arduino library handles the wireless radio link and protocol
 * between your home built sensors/actuators and HA controller of choice.
 * The sensors forms a self healing radio network with optional repeaters. Each
 * repeater and gateway builds a routing tables in EEPROM which keeps track of the
 * network topology allowing messages to be routed to nodes.
 *
 * Created by Henrik Ekblad <henrik.ekblad@mysensors.org>
 * Copyright (C) 2013-2015 Sensnology AB
 * Full contributor list: https://github.com/mysensors/Arduino/graphs/contributors
 *
 * Documentation: http://www.mysensors.org
 * Support Forum: http://forum.mysensors.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * version 2 as published by the Free Software Foundation.
 *
 *******************************
 *
 * REVISION HISTORY
 * Version 1.0 - Henrik EKblad
 * Contribution by a-lurker and Anticimex,
 * Contribution by Norbert Truchsess <norbert.truchsess@t-online.de>
 * Contribution by Ivo Pullens (ESP8266 support)
 *
 * DESCRIPTION
 * The EthernetGateway sends data received from sensors to the WiFi link.
 * The gateway also accepts input on ethernet interface, which is then sent out to the radio network.
 *
 * VERA CONFIGURATION:
 * Enter "ip-number:port" in the ip-field of the Arduino GW device. This will temporarily override any serial configuration for the Vera plugin.
 * E.g. If you want to use the defualt values in this sketch enter: 192.168.178.66:5003
 *
 * LED purposes:
 * - To use the feature, uncomment any of the MY_DEFAULT_xx_LED_PINs in your sketch, only the LEDs that is defined is used.
 * - RX (green) - blink fast on radio message recieved. In inclusion mode will blink fast only on presentation recieved
 * - TX (yellow) - blink fast on radio message transmitted. In inclusion mode will blink slowly
 * - ERR (red) - fast blink on error during transmission error or recieve crc error
 *
 * See http://www.mysensors.org/build/esp8266_gateway for wiring instructions.
 * nRF24L01+  ESP8266
 * VCC        VCC
 * CE         GPIO4
 * CSN/CS     GPIO15
 * SCK        GPIO14
 * MISO       GPIO12
 * MOSI       GPIO13
 * GND        GND
 *
 * Not all ESP8266 modules have all pins available on their external interface.
 * This code has been tested on an ESP-12 module.
 * The ESP8266 requires a certain pin configuration to download code, and another one to run code:
 * - Connect REST (reset) via 10K pullup resistor to VCC, and via switch to GND ('reset switch')
 * - Connect GPIO15 via 10K pulldown resistor to GND
 * - Connect CH_PD via 10K resistor to VCC
 * - Connect GPIO2 via 10K resistor to VCC
 * - Connect GPIO0 via 10K resistor to VCC, and via switch to GND ('bootload switch')
 *
  * Inclusion mode button:
 * - Connect GPIO5 via switch to GND ('inclusion switch')
 *
 * Hardware SHA204 signing is currently not supported!
 *
 * Make sure to fill in your ssid and WiFi password below for ssid & pass.
 */


// Enable debug prints to serial monitor
#define MY_DEBUG

// Use a bit lower baudrate for serial prints on ESP8266 than default in MyConfig.h
#define MY_BAUD_RATE 9600

// Enables and select radio type (if attached)
#define MY_RADIO_NRF24
//#define MY_RADIO_RFM69

#define MY_GATEWAY_ESP8266

// set SSID and password
#define MY_ESP8266_SSID ""
#define MY_ESP8266_PASSWORD ""

// Enable UDP communication
//#define MY_USE_UDP

// Set the hostname for the WiFi httpClient. This is the hostname
// it will pass to the DHCP server if not static.
#define MY_ESP8266_HOSTNAME "sensor-gateway"

// Enable MY_IP_ADDRESS here if you want a static ip address (no DHCP)
//#define MY_IP_ADDRESS 192,168,1,25

// If using static ip you need to define Gateway and Subnet address as well
//#define MY_IP_GATEWAY_ADDRESS 192,168,1,1
//#define MY_IP_SUBNET_ADDRESS 255,255,255,0
#define MY_MAC_ADDRESS 0x5C, 0xCF, 0x7F, 0xFD, 0x84, 0x9A

// The port to keep open on node server mode
#define MY_PORT 5003

// How many clients should be able to connect to this gateway (default 1)
#define MY_GATEWAY_MAX_CLIENTS 2

// Controller ip address. Enables client mode (default is "server" mode).
// Also enable this if MY_USE_UDP is used and you want sensor data sent somewhere.
//#define MY_CONTROLLER_IP_ADDRESS 192, 168, 1, 3

// Enable inclusion mode
//#define MY_INCLUSION_MODE_FEATURE

// Enable Inclusion mode button on gateway
// #define MY_INCLUSION_BUTTON_FEATURE
// Set inclusion mode duration (in seconds)
//#define MY_INCLUSION_MODE_DURATION 60
// Digital pin used for inclusion mode button
//#define MY_INCLUSION_MODE_BUTTON_PIN  3


// Set blinking period
 #define MY_DEFAULT_LED_BLINK_PERIOD 300

// Flash leds on rx/tx/err
// Led pins used if blinking feature is enabled above
#define MY_DEFAULT_ERR_LED_PIN 16  // Error led pin
#define MY_DEFAULT_RX_LED_PIN  16  // Receive led pin
#define MY_DEFAULT_TX_LED_PIN  16  // the PCB, on board LED

#if defined(MY_USE_UDP)
#include <WiFiUdp.h>
#endif

#include <SPI.h>
#include <ESP8266WiFi.h>
#include <ESP8266WebServer.h>
#include <WiFiClient.h>
#include <MySensors.h>

// webserver
//  - Webserver so the gateway can receive http requests from the hub/controller for SmartThings integration
ESP8266WebServer webServer(80);
WiFiClient httpClient;

// 

void setup()
{
  // show the index page
  webServer.on("/", displayHttpIndex);

  // receive the inbound message from SmartThings
  webServer.on("/node", []() {

    handleNodeMsg();
    
  });

  webServer.begin();
  #ifdef MY_DEBUG
  Serial.println("HTTP server started");
  #endif
  
}

void presentation()
{
	// Present locally attached sensors here
}


void loop()
{
	// Send locally attached sensors data here
  webServer.handleClient();
}

void receive(const MyMessage &msg)
{
  char body[61];
  char convBuf[30 * 2 + 1];
  uint8_t command = msg.getCommand();

  #ifdef MY_DEBUG
  Serial.println("");
  Serial.println("Received message");
  Serial.print("Sender: ");
  Serial.print(msg.sender);
  Serial.print(" | Command: ");
  Serial.print(command);
  Serial.print(" | Sensor: ");
  Serial.println(msg.sensor);
  
  #endif
  

//  if (msg.sensor != 255) {
    
     if (command == 0 || command == 1 || command == 3) {
        #ifdef MY_DEBUG
        Serial.println("Sending to hub");
        #endif

        // Put the command in a stirng so it can be send as plain text in html body
        // future: put this in a JSON format
        sprintf(body, "%d;%d;%d;%d;%d;%s\n",msg.sender, msg.sensor, command, msg.isAck(), msg.type, msg.getString(convBuf));

        #ifdef MY_DEBUG
        Serial.print("Body to ST: ");
        Serial.println(body);
        #endif
        
        sendSTHub(body);
    
        #ifdef MY_DEBUG
        Serial.print("msg.sensor: ");
        Serial.println(msg.sensor);
        Serial.println("Sent to hub");
        Serial.println("");
        #endif
     }
//  }
}

void sendSTHub(String msg) {
  // create http client
  // hard coding for now
  IPAddress hubIp(192, 168, 1, 3);
  int hubPort = 39500;
  String readString;
  boolean currentLineIsBlank = true;
  String tempString;
  
  // Put REST API call to SmartThings here
  // Connect to SmartThings hub
  // Future is to refactor to use OAuth for better security
  if (httpClient.connect(hubIp, hubPort))
  {
    // Send message to hub
    httpClient.println("POST / HTTP/1.1");
    httpClient.print("HOST: ");
    httpClient.print(hubIp);
    httpClient.print(":");
    httpClient.println(hubPort);
    httpClient.println("CONTENT-TYPE: text");
    httpClient.print("CONTENT-LENGTH: ");
    httpClient.println(msg.length());
    httpClient.println();
    httpClient.println(msg);

    // Read the response from the hub
    while (httpClient.connected())
    {
          char c = httpClient.read();
          //read by char HTTP response
          readString += c;
          
          // if you've gotten to the end of the line (received a newline
          // character) and the line is blank, the http request has ended,
          // so you can send a reply
          if (c == '\n') {
            // you're starting a new line
            currentLineIsBlank = true;
          }
          else if (c != '\r') {
            // you've gotten a character on the current line
            currentLineIsBlank = false;
          }
    }
    #ifdef MY_DEBUG
    Serial.print("ST Response: ");
    Serial.println(readString);
    #endif
    httpClient.stop();
  }
  else
  {
    Serial.println("not connected");
  }
  
}

void displayHttpIndex() {
  #ifdef MY_DEBUG
  Serial.println("Index page");
  #endif
  webServer.send(200, "text/html", "<h1>MySenors Gateway</h1>");
}

void handleNodeMsg() {
  #ifdef MY_DEBUG
  Serial.println("handleNodeMsg" );
  #endif
  String msg = "";

  // expected message format nodeId;sensorId;command;ack;type;payload

  switch (webServer.method()) {
    case HTTP_GET:
      httpGetRequest();
      break;

    case HTTP_POST:
      break;

    default:
      Serial.print("Invalid http method: ");
      Serial.println(webServer.method());
    
  }
  
  msg += "<p>";
  msg += "URI: ";
  msg += webServer.uri();
  msg += "<br>";
  msg += "Method: ";
  msg += ( webServer.method() == HTTP_GET ) ? "GET" : "POST";
  msg += "<br>";
  msg += "Arguments: ";
  msg += webServer.args();
  msg += "<br>";

  for ( uint8_t i = 0; i < webServer.args(); i++ ) {
    msg += " " + webServer.argName ( i ) + ": " + webServer.arg ( i ) + "<br>";
  }
  msg += "<br>";
  Serial.println("received http request");
  Serial.print(msg);
  webServer.send(200, "text/html", msg);
}

void httpGetRequest() {
  #ifdef MY_DEBUG
  Serial.println("httpGetRequest()");
  #endif

  String msg;

  if (webServer.args() != 6) {
    // always expecting 6 arguments on request
    Serial.println("Invalid number of GET request args");
    return;
  }

  for ( uint8_t i = 0; i < webServer.args(); i++ ) {
    msg = webServer.argName ( i ) + ": " + webServer.arg ( i );
    Serial.println(msg);
  }
    
  
}

