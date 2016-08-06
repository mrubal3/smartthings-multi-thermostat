/**
 *  Copyright 2014 SmartThings
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
  definition (
      name: "Target Temperature Time",
      namespace: "mvgrimes",
      author: "mgrimes@cpan.org",
      description: "Tile to set the target temperature for use by SmartApps like MultiSensorTherm",
      category: "Green Living",
      version: "0.1",
      iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/temp_thermo.png",
      iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/temp_thermo@2x.png"
    ) {

    capability "Temperature Measurement"   // defines temperature attr
    capability "Switch Level"              // defines level attr, setLevel(num,num) command

    command "setTemperature", ["number"]
  }


  // UI tile definitions
  tiles {
    valueTile("temperature", "device.temperature", width: 2, height: 2) {
      state("temperature", label:'${currentValue}', unit:"F",
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

    controlTile("heatSliderControl", "device.heatingSetpoint", "slider", height: 1, width: 2, inactiveLabel: false) {
      state "setHeatingSetpoint", action:"quickSetHeat", backgroundColor:"#d04e00"
    }
    valueTile("heatingSetpoint", "device.heatingSetpoint", inactiveLabel: false, decoration: "flat") {
      state "heat", label:'${currentValue}° heat', backgroundColor:"#ffffff"
    }
    controlTile("coolSliderControl", "device.coolingSetpoint", "slider", height: 1, width: 2, inactiveLabel: false) {
      state "setCoolingSetpoint", action:"quickSetCool", backgroundColor: "#1e9cbb"
    }
    valueTile("coolingSetpoint", "device.coolingSetpoint", inactiveLabel: false, decoration: "flat") {
      state "cool", label:'${currentValue}° cool', backgroundColor:"#ffffff"
    }

    main "temperature"
    details(["temperature", "heatSliderControl", "heatingSetpoint", "coolSliderControl", "coolingSetpoint"])
    // "refresh", "configure"])
  }
}

// Parse incoming device messages to generate events
def parse(String description) {
  def pair = description.split(":")
  createEvent(name: pair[0].trim(), value: pair[1].trim(), unit:"F")
}

def setLevel(value) {
  sendEvent(name:"temperature", value: value)
}

// def up() {
//   def ts = device.currentState("temperature")
//   def value = ts ? ts.integerValue + 1 : 72
//   sendEvent(name:"temperature", value: value)
// }
//
// def down() {
//   def ts = device.currentState("temperature")
//   def value = ts ? ts.integerValue - 1 : 72
//   sendEvent(name:"temperature", value: value)
// }

def setTemperature(value) {
  sendEvent(name:"temperature", value: value)
}
