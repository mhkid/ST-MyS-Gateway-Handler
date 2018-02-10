/**
 *  Child Temperature Sensor
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
 *    2017-09-17  Eric Frame  Original Creation
 *
 * 
 */

metadata {
	definition (name: "MySensors Temperature Sensor", namespace: "ecframe", author: "Eric Frame") {
		capability "Temperature Measurement"
		capability "Relative Humidity Measurement"
        capability "Battery"
		capability "Sensor"

	}

	// simulator metadata
	simulator {
		for (int i = 0; i <= 100; i += 10) {
			status "${i}F": "temperature: $i F"
		}

		for (int i = 0; i <= 100; i += 10) {
			status "${i}%": "humidity: ${i}%"
		}

		for (int i = 0; i <= 100; i += 10) {
			status "${i}%": "battery: ${i}%"
		}
	}

	// UI tile definitions
	tiles(scale:2) {
       	valueTile("temperature", "device.temperature", width:6, height:4) {
			state("temperature", label:'${currentValue}°', unit:"dF", icon:"st.Weather.weather2",
				backgroundColors:[
					[value: 31, color: "#153591"],
					[value: 44, color: "#1e9cbb"],
					[value: 59, color: "#90d2a7"],
					[value: 74, color: "#44b621"],
					[value: 84, color: "#f1d801"],
					[value: 95, color: "#d04e00"],
					[value: 96, color: "#bc2323"]
				]
            )
       	}
        valueTile("humidity", "device.humidity", width:2, height:2) {
        	state ("humidity", label:'${currentValue}%', icon:"st.Weather.weather12", unit:"%")
        }
        valueTile("battery", "device.battery", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
        	state ("battery", label:'${currentValue}% Battery', unit:"%")
        }
        
    }

	main(["temperature"])

	details(["temperature", "humidity", "battery"])
}

def parse(String description) {
	log.debug "temp parse"
}

def updated() {
	log.debug "Executing 'updated'"
}