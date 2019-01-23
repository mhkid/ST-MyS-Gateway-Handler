# IoT
Home IoT senors on MySensors framework and SmartThings platform

## Summary
This is a home IoT project using arduino, esp8266, and raspberry pi.  This is the device handler code for receiving a message from a MySensors gateway.  The communication from the MySensors gateway to the SmartThings hub is done via an http call to the SmartThings API.  This repo contains the gateway device handler code and device handler(s) for diy sensor devices based on the MySensors library.

  * [MySensors](https://mysensors.org)
  * [SmartThings developer portal](https://developer.smartthings.com/)
  
My repo for the code for the MySensors gateway transport is *https://github.com/mhkid/GatewayTransportSmartThings.git*

### How it works
The MySensor Gateway makes calls the SmartThings API with the message.  The gateway device handler processes the message and orchastrates the appropriate action.  The incoming message identifies the node and sensor and checks to see if it exists.  If it doesn't it creates a child device under the gateway device.  If it does exist it looks at the command and performs an action.

### Notes
  * Read the SmartThings documentation on how to create device handlers.
  * The gateway device handler has to be created manually and then the gateway device can be created manually with that device handler. 
  * The gateway Device Network ID must be the MAC address of the gateway sending the request.  SmartThings uses this to validate the request is coming from a known device on your network.
  * In the SmartThings Developer portal you can point to this repo and just import the code that way.
  * This is very much a work in progress.  Feel free to contribute or give feedback.
  
  
