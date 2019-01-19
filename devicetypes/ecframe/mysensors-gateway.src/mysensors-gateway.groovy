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
    def sensorNodeId
    def sensorId
    def evt = [:]
    
	log.debug " "

    if (body) {
        def param = body.split(";")
        def node = param[0]
        def sensor = param[1]
        def command = param[2].toInteger()
        def ack = param[3]
        def type = param[4].toInteger()
        def payload = param[5]
        
        log.debug "node:${node} | sensor:${sensor} | command:${command} | ack:${ack} | type:${type} | payload:${payload}"
        
		sensorDeviceId = device.deviceNetworkId + "-" + node + "-" + sensor

		try {
			switch (command) {
        		case 0: //Presentation
					processPresentationCommand(sensorDeviceId, node, sensor, payload, type)
	            	break
        
        		case 1: //Set
					processSetCommand(sensorDeviceId, command, type, payload)
	            	break

        		case 2: //Request
					log.debug "Request command - Not implemented"
	            	break
					
        		case 3: //Request
					log.debug "Internal command - Not implemented"
	            	break
					
		  		default:
             		log.debug "command not implemented: ${command}"

			}   
		}
   		catch (Exception e) {
       		log.error "switch try/catch command ${command}"
   		}
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
	log.debug "finding: ${childSensor}"

    // loop through all the child devices to see if this one already exists
	try {
    	log.debug "before childDevices.each"
        childDevices.each {
            log.debug "device: ${it.deviceNetworkId}"
           	if (it.deviceNetworkId == childSensor) {
                // sensor device exists
                exists = true
           	}
       	}
	}
   	catch (e) {
       	log.error "findChild error ${childSensor}: " + e
    }
    
	return exists

}

def processPresentationCommand(sensorDeviceId, node, sensor, payload, type) 
{
	log.debug "Processing present command"

	def childFound = false
	def childCreated = false

	try 
	{
	    if (sensor != "255")  // don't do this for the gateway
		{
			childFound = findChild(sensorDeviceId)
			if (!childFound) 
			{
				log.debug "Sensor not found, createing a new one."
				childCreated = createChildDevice(sensorDeviceId, payload, type, node, sensor)
       			if (!childCreated) 
				{
                	log.error "Child sensor ${sensorDeviceId} not created"
    			}
            	else 
				{
	            	log.info "Child sensor ${sensorDeviceId} created"
            	}
			} 
			else 
			{
				log.debug "Sensor presented (${sensorDeviceId})"
			}
		}
	}
	catch (e) 
	{
		log.error "Presentation error ${sensorDeviceId}: " + e
	}
}

//def processSetCommand(sensorDeviceId, type, payload) {
def processSetCommand(sensorDeviceId, command, type, payload) {
	// Update the sensor data
	if (command == 1)
	{
		log.debug "Processing set command for sensorDeviceId: ${sensorDeviceId}"

    	def childSensorDevice = null
		def eventMap = null
    	def deviceType = null

		try 
		{
			childDevices.each 
			{
				if (it.deviceNetworkId == sensorDeviceId) 
				{
					childSensorDevice = it
				}
			}
        
			deviceType = childSensorDevice.getTypeName()
			eventMap = buildEventMap(sensorDeviceId, deviceType, command, type, payload)

			log.debug "name: " + eventMap.name + " | value: " + eventMap.value
			childSensorDevice.sendEvent(name: eventMap.name, value: eventMap.value, isStateChanged: "true")
		    log.debug "event sent"
		}
		catch (e) 
		{
			log.error "Error finding child device: ${e}"
		}
	}
	else
	{
		log.debug "processSetCommand invalid command: ${command}"
	}
	    
}

def processInternalCommand(sensorNodeId, type, payload) {

    def childSensorDevice = null
	def eventMap = null
    def deviceType = null

	try {
		childDevices.each {
			if (it.deviceNetworkId == sensorNodeId) {
					childSensorDevice = it
			}
		}
        
		deviceType = childSensorDevice.getTypeName()
		eventMap = buildEventMap(sensorNodeId, deviceType, 3, type, payload)
	}
	catch (e) {
		log.error "Error finding child after building map: ${e}"
	}

	    
	log.debug "name: " + eventMap.name + " | value: " + eventMap.value
	//childSensorDevice.sendEvent(name: eventMap.name, value: eventMap.value)
    log.debug "Device Type: ${deviceType}"
}

def Map buildEventMap(sensorDevice, deviceType, command, commandType, payload) {

	def mapResult = [:]

    String sensorAttribute = ""
    String sensorValue = ""
   
    log.debug "commandType: " + commandType
    log.debug "payload: " + payload

	try {

        if (command==1) {
			switch (commandType) {
        		case 0:           //V_TEMP
             		mapResult = processTemperature(payload)
             		break
                
        		case 1:           //V_HUM
            		mapResult = processHumidity(payload)
            		break
        
		  		default:
             		log.debug "type unknown: ${commandType}"

			}   
        } else if (command==3) {
			switch (commandType) {
        		case 0:           //I_BATTERY_LEVEL
             		mapResult = processBatteryLevel(payload)
             		break
                    
		  		default:
             		log.debug "type unknown: ${commandType}"
			}   
        }

		log.debug "mapResult: ${mapResult.name} | ${mapResult.value}"

	}
   catch (Exception e) {
       log.error "buildEventMap: " + e
   }

return mapResult
}

def Map processTemperature(String value) {
    
    def mapReturn = [:]
    
    mapReturn.put('name', 'temperature')
    mapReturn.put('value', value)

	return mapReturn
}

def Map processHumidity(String value) {
    
    def mapReturn = [:]
    
    mapReturn.put('name', 'humidity')
    mapReturn.put('value', value)

	return mapReturn
}

def Map processBatteryLevel(String value) {
    
    def mapReturn = [:]
    
    mapReturn.put('name', 'battery')
    mapReturn.put('value', value)

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
   	def deviceHandlerName = ""

	if (deviceName != "" && deviceName != null)
	{
    	if ( device.deviceNetworkId =~ /^[A-Z0-9]{12}$/)
    	{
			log.debug "createChildDevice:  Creating Child Device ${deviceName} | ${deviceId} | ${deviceType}"

			try 
        	{
				deviceHandlerName = getHandlerName(deviceType)
			
            	log.debug "xxx deviceType:${deviceType} | deviceHandlerName:${deviceHandlerName} | deviceId:${deviceId} | deviceName:${deviceName}"

            	if (deviceHandlerName != "") 
				{
                	log.debug "adding device"
					addChildDevice(deviceHandlerName, "${deviceId}", null,
		      		 	[completedSetup: true, label: "${deviceName}", 
                	 	isComponent: false, componentLabel: "${deviceName}"])

					status = true
        		}   
            	else 
				{
              		// device handler didn't get set
              		throw new Exception("deviceHandlerName not set");
            	}
    		} 
			catch (e) 
			{
        		log.error "Child device creation failed with error = ${e}"
    		}
		} 
	}
	else
	{
		log.error "Can't create child device without a device name"
	}

    return status
}

private String getHandlerName(deviceType) {

	def handlerName = ""

	log.debug "getHandlerName deviceType ${deviceType}"

  	switch (deviceType) {
        case 1:               // MySensors S_MOTION
           	handlerName = "MySensors Motion Sensor"
           	break
		case 23:              // MySensors s_CUSTOM
       		handlerName = "MySensors Temperature Sensor" 
           	break
	    default: 
           	log.error "No Child Device Handler case for ${deviceName}"
			handlerName = ""
	}

	return handlerName

}
