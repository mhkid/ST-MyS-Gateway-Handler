/**
 *  mySensorsGateway.groovy
 *
 *  Copyright 2017 Eric Frame
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *  Change History:
 *
 *    Date        Who            What
 *    ----        ---            ----
 *    2017-07-30  Eric Frame     Original Creation
 *    2018-01-17  Eric Frame	 Gateway refactor and cleanup
 */
 
metadata {
	definition (name: "MySensors Gateway", namespace: "ecframe", author: "Eric Frame") {
        capability "Configuration"
        capability "Refresh"
	}

    simulator {
    }

    // Preferences
	preferences {
		input("gatewayIp", "text", title: "Gateway IP Address", description: "Enter Gateway IP Address", required: true, displayDuringSetup: true)
		input("gatewayMAC", "text", title: "Gateway MAC Address", description: "Enter Gateway MAC Address", required: true, displayDuringSetup: true)
		input("gatewayPort", "text", title: "Gateway Port", description: "Enter Gateway Port (80)", required: true, displayDuringSetup: true)
		input("gatewayURL", "string", title:"URL Path", description: "Rest of the URL, include forward slash.", displayDuringSetup: true)
		input(name: "httpPostGet", type: "enum", title: "POST or GET", options: ["POST","GET"], required: true, displayDuringSetup: true)

	// Tile Definitions
	tiles (scale: 2){
		standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 3, height: 2) {
			state "default", label:'Refresh', action: "refresh.refresh", icon: "st.secondary.refresh-icon", backgroundColor:"#00A0DC"
		}
        
		standardTile("configure", "device.configure", inactiveLabel: false, decoration: "flat", width: 3, height: 2) {
			state "configure", label:'Configure', action: "configuration.configure", icon:"st.secondary.tools"
		}

		childDeviceTiles("all")
	}

	}
}


// parse events into attributes
def parse(String description) {
    def msg = parseLanMessage(description);
    def header = msg.header
    def body = msg.body
    def sensorDeviceId
    def evt = [:]
    def children = device.childDevices
    
	log.debug " "
	log.debug "header: $header"
    log.debug "Gateway message: $body"

    if (body) {
        def param = body.split(";")
        def node = param[0]
        def sensor = param[1]
        def command = param[2].toInteger()
        def ack = param[3]
        def type = param[4].toInteger()
        def payload = param[5]
		def childFound = false
        def childCreated = false
        //def childSensorDevice = null
        //def eventMap = null
        
        log.debug "node:${node} | sensor:${sensor} | command:${command} | ack:${ack} | type:${type} | payload:${payload}"
        
        /*
		// don't process internal commands at this point.  Later implementation
        if (command != 3) {
			sensorDeviceId = device.deviceNetworkId + "-" + node + "-" + sensor
        
        	log.debug "sensorDeviceId: ${sensorDeviceId}"
        
        	childFound = findChild(sensorDeviceId) 
        	try {
        		if (!childFound) {
                	// Sensor presentation message.  Type 17 is S_ARDUINO_NODE, this doesn't need to be created, so just ignore it.
                	if (command == 0  && type !=17) {
                		// Create the sensor
	            		childCreated = createChildDevice(sensorDeviceId, payload, type, node, sensor)
    	        		if (!childCreated) {
	                        log.error "Child sensor ${sensorDeviceId} not created"
							//throw new Exception("Child sensor ${sensorDeviceId} not created");
        	    		}
                        else {
	                        log.info "Child sensor ${sensorDeviceId} created"
                        }
            	    }
                	else {
                		log.error "Child sensor ${sensorDeviceId} doesn't exist"
        				//throw new Exception("Child sensor ${sensorDeviceId} doesn't exist");
                	}
        		}
        		else {
                	// childs exists so check to see if this is an update to sensor value
                    switch (command) {
                    	case 1:
                        	processSetCommand(sensorDeviceId, type, payload)
                        	break
                            
                        case 2:
                        	break
                            
                        case 3:
                        	processInternalCommand(sensorDeviceId, type, payload)
                        	break
                            
                        default:
                        	log.debug "command unknown: ${command}"
                            
                    }
        		}
        	}
        	catch (e) {
        		log.error "Error processing sensor payload: ${e}"
        	}
        }  // command != 3
		*/
	return
    }

}

def configure() {
	log.debug "Executing 'configure()'"
}

def refresh() {
	log.debug "Executing 'refresh()'"
}

def boolean findChild(childSensor) {
    def exists = false
	//log.debug "finding: ${childSensor}"

  // loop through all the child devices to see if this one already exists
	try {
        childDevices.each {
           	if (it.deviceNetworkId == childSensor) {
                // sensor device exists
                exists = true
           	}
       	}
	}
   	catch (e) {
       	log.error "findChild error: " + e
    }
    
	return exists

}

def processSetCommand(sensorDeviceId, type, payload) {
	// Set is an update to a sensor value, so build the event map

    def childSensorDevice = null
	def eventMap = null
    def deviceType = null

	try {
		childDevices.each {
			if (it.deviceNetworkId == sensorDeviceId) {
					childSensorDevice = it
			}
		}
        
		deviceType = childSensorDevice.getTypeName()
		eventMap = buildEventMap(sensorDeviceId, deviceType, 2, type, payload)
	}
	catch (e) {
		log.error "Error finding child after building map: ${e}"
	}

	    
	log.debug "name: " + eventMap.name + " | value: " + eventMap.value
	childSensorDevice.sendEvent(name: eventMap.name, value: eventMap.value, isStateChanged: "true")
    log.debug "Device Type: ${deviceType}"
}

def processInternalCommand(sensorDeviceId, type, payload) {

	def childSensorDevice = null
    def eventMap = null
    
    
}

def Map buildEventMap(sensorDevice, deviceType, command, commandType, payload) {

	def mapResult = [:]

//	log.debug "buidldEventMap"
    
    String sensorAttribute = ""
    String sensorValue = ""
   
    log.debug "commandType: " + commandType
    log.debug "payload: " + payload

	try {

		switch (commandType) {
        	case 16:           //MySensors V_TRIPPED
            	mapResult = processMotion(payload)
            	break
        
        	case 24:           //MySensors V_VAR1
        		// Temperature / Humdity / Battery
             	mapResult = processTempHumidity(payload)
             	break

		  	default:
             	log.debug "type unknown: ${commandType}"

		}   

		log.debug "mapResult: ${mapResult.name} | ${mapResult.value}"

	}
   catch (Exception e) {
       log.error "buildEventMap: " + e
   }

return mapResult
}

def Map processMotion(String value) {
	
    def sensorData = value.replaceAll('\n', '')
    def mapReturn = [:]
    
	try {
    	switch (sensorData) {
        	case "0":
				mapReturn.put('name', "motion")
    			mapReturn.put('value', "inactive")
                break
            
        	case "1":
				mapReturn.put('name', "motion")
    			mapReturn.put('value', "active")
                break

			default:
           		log.debug "unknown sensor value type motion: ${value}"
        }
    }
    catch (e) {
    	log.error "processMotion error: ${e}"
    }
    
    return mapReturn
}

def Map processTempHumidity(String value) {
    // parse out payload 
    //     sensorData[0] is the sensor type i.e. T = temp, H = humidity
    //     sensorData[1] is the value i.e. 99.9
    
    def splitValue = value.replaceAll('\n', '')
	def sensorData = splitValue.split(":")
    def sensorAttribute = ""
    def mapReturn = [:]
    
    log.debug "sensorData[0]: ${sensorData[0]}"
    log.debug "sensorData[1]: ${sensorData[1]}"

	try {
		switch (sensorData[0]) {
        	case "T":
		    	sensorAttribute = "temperature"
            	break
                    
        	case "H":
           		sensorAttribute = "humidity"
           		break
                    
        	case "B":
           		sensorAttribute = "battery"
           		break

			default:
           		log.debug "unknown sensor value type (temp/humidity): ${sensorData[0]}"
    	}
        
    }
    catch (e) {
    	log.error "processTempHumidty error: ${e}"
    }
    
    mapReturn.put('name', sensorAttribute)
    mapReturn.put('value', sensorData[1])

	return mapReturn
}

private boolean createChildDevice(String deviceId, String deviceName, Integer deviceType, String nodeId, String sensorId) {

	def status = false

    if ( device.deviceNetworkId =~ /^[A-Z0-9]{12}$/)
    {
		log.debug "createChildDevice:  Creating Child Device ${deviceName} | ${deviceId} | ${deviceType}"

		try 
        {
        	def deviceHandlerName = ""
        	switch (deviceType) {
                case 1:               // MySensors S_MOTION
                	deviceHandlerName = "MySensors Motion Sensor"
                	break
				case 23:              // MySensors s_CUSTOM
              		deviceHandlerName = "MySensors Temperature Sensor" 
                	break
				default: 
                	log.error "No Child Device Handler case for ${deviceName}"
      		}
            log.debug "xxx deviceType:${deviceType} | deviceHandlerName:${deviceHandlerName} | deviceId:${deviceId} | deviceName:${deviceName}"
            if (deviceHandlerName != "") {
                log.debug "adding device"
				addChildDevice(deviceHandlerName, "${deviceId}", null,
		      		[completedSetup: true, label: "${deviceName}", 
                	isComponent: false, componentLabel: "${deviceName}"])

				status = true
        	}   
            else {
              // device handler didn't get set
              throw new Exception("deviceHandlerName not set");
            }
    	} catch (e) {
        	log.error "Child device creation failed with error = ${e}"
    	}
        
	} 
    
    return status
  
}

def httpGatewayRequest() {
	log.debug "httpGatewayRequest()"
	def host = gatewayIp
	def hosthex = convertIPtoHex(host).toUpperCase()
	def porthex = convertPortToHex(gatewayPort).toUpperCase()
	device.deviceNetworkId = "$hosthex:$porthex"
	def userpassascii = "${HTTPUser}:${HTTPPassword}"
	def userpass = "Basic " + userpassascii.encodeAsBase64().toString()
    def varCommand = "node?nodeId=255&sensorId=0&command=2&ack=0&type=1&payload=OK"
    def nodeId = "255"
    def sensorId = "0"
    def command = "2"
    def ack = "0"
    def type = "1"
    def payload = "status"
    //nodeId;sensorId;command;ack;type;payload
    def query = [:]
    
    query.put("nodeId", nodeId);
    query.put("sensorId", sensorId);
    query.put("commnad", command);
    query.put("ack", ack);
    query.put("type", type);
    query.put("payload", payload);

	log.debug "The device id configured is: $device.deviceNetworkId"

	//def path = DevicePath
	def path = gatewayURL
	log.debug "path is: $path"
	log.debug "Uses which method: $httpPostGet"
	def body = ""//varCommand
	log.debug "body is: $body"

	def headers = [:]
	headers.put("HOST", "$host:$gatewayPort")
	headers.put("Content-Type", "application/x-www-form-urlencoded")
	if (HTTPAuth) {
		headers.put("Authorization", userpass)
	}
	log.debug "The Header is $headers"
	def method = "POST"
	try {
		if (httpPostGet.toUpperCase() == "GET") {
			method = "GET"
			}
		}
	catch (Exception e) {
		settings.httpPostGet = "POST"
		log.debug e
		log.debug "You must not have set the preference for the httpPOSTGET option"
	}
	log.debug "The method is $method"
	try {
		def hubAction = new physicalgraph.device.HubAction(
			method: method,
			path: path,
			body: body,
			headers: headers,
            query: query
			)
		hubAction.options = [outputMsgToS3:false]
		log.debug hubAction
		hubAction
	}
	catch (Exception e) {
		log.debug "Hit Exception $e on $hubAction"
    }
}

private String convertIPtoHex(ipAddress) {
	String hex = ipAddress.tokenize( '.' ).collect {  String.format( '%02x', it.toInteger() ) }.join()
	//log.debug "IP address entered is $ipAddress and the converted hex code is $hex"
	return hex
}
private String convertPortToHex(port) {
	String hexport = port.toString().format( '%04x', port.toInteger() )
	//log.debug hexport
	return hexport
}
private Integer convertHexToInt(hex) {
	Integer.parseInt(hex,16)
}
private String convertHexToIP(hex) {
	//log.debug("Convert hex to ip: $hex")
	[convertHexToInt(hex[0..1]),convertHexToInt(hex[2..3]),convertHexToInt(hex[4..5]),convertHexToInt(hex[6..7])].join(".")
}
private getHostAddress() {
	def parts = device.deviceNetworkId.split(":")
	//log.debug device.deviceNetworkId
	def ip = convertHexToIP(parts[0])
	def port = convertHexToInt(parts[1])
	return ip + ":" + port
}