/**
 *  Virtual Presence Device
 *
 *  Copyright 2015 Mark Grimes
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
 *  Based on similated-presence-sensor.groovy by SmartThings. Thanks!
 */

metadata {
  definition (name: "Virtual Presence Device",
            namespace: "mvgrimes", author: "Mark Grimes") {
    capability "Presence Sensor"  // attr: presence
    capability "Switch"           // attr: switch; cmds: on, off
    capability "Sensor"           // attr: none
  }

  simulator {
    status "present": "presence: present"
    status "not present": "presence: not present"
  }

  tiles {
    standardTile("presence", "device.presence", width: 3, height: 2, canChangeBackground: true) {
      state("not present", label:'not present', icon:"st.presence.tile.not-present", backgroundColor:"#ffffff", action:"on")
      state("present", label:'present', icon:"st.presence.tile.present", backgroundColor:"#53a7c0", action:"off")
    }
    standardTile("switch", "device.switch", width: 3, height: 2, canChangeIcon: true, canChangeBackground: true) {
      state "on", label: '${name}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#79b821"
      state "off", label: '${name}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
    }
    main "presence"
    details "presence", "switch"
  }
}

def parse(String description) {
  def pair = description.split(":")
  createEvent(name: pair[0].trim(), value: pair[1].trim())
}

// handle commands
def on() {
  log.trace "Executing 'on'"
  sendEvent(name: "presence", value: "present")
  sendEvent(name: "switch", value: "on")
}

def off() {
  log.trace "Executing 'off'"
  sendEvent(name: "presence", value: "not present")
  sendEvent(name: "switch", value: "off")
}

// Ensure we are in a consisent state
def ensureConsistency() {
  if( device.currentValue("presence") == "present" ){
    on()
  } else {
    off();
  }
}

// Debugging helper
def logState() {
  log.debug "presence: ${device.currentValue('presence')}"
  log.debug "switch: ${device.currentValue('switch')}"
}

