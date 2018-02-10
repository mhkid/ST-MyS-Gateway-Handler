/**
 *  MySensors Motion Sensor
 *
 *  Copyright 2018 Eric Frame
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
 */

metadata {
	definition (name: "MySensors Motion Sensor", namespace: "ecframe", author: "Eric Frame") {
		capability "Motion Sensor"
        capability "Battery"
        capability "Sensor"
	}

	tiles(scale: 2) {
		multiAttributeTile(name:"motion", type: "generic"){
			tileAttribute ("device.motion", key: "PRIMARY_CONTROL") {
				attributeState "active", label:"Motion", icon:"st.motion.motion.active", backgroundColor:"#00A0DC"
				attributeState "inactive", label:"No Motion", icon:"st.motion.motion.inactive", backgroundColor:"#ffffff"
            }
            
            tileAttribute("device.battery", key: "SECONDARY_CONTROL") {
            	attributeState ("battery", label:'${currentValue}% Battery')
            }
        }
	}
}

