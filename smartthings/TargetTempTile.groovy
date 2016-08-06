/**
 *  Copyright 2016 Mark Grimes
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
 *  Resource from the SmartThings repo:
 *    - testing/.../simulated-thermostat.groovy
 *    - testing/.../simulated-temperature-sensor.groovy
 *    - zwave-thermostat.groovy
 *    - Create virtual device: https://community.smartthings.com/t/faq-creating-a-virtual-device/11282/2
 */

metadata {
  definition (
      name: "Target Temperature Tile",
      namespace: "mvgrimes",
      author: "mgrimes@cpan.org",
      description: "Tile to set the target temperature for use by smartapps like MultiSensorTherm",
      category: "Green Living",
      version: "0.1",
      iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/temp_thermo.png",
      iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/temp_thermo@2x.png"
    ) {

    capability "Thermostat"     // defines a bunch of attr and commands
    attribute "combiningFunction", "enum", [ "ave", "min", "max" ]

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

    standardTile("mode", "device.thermostatMode", inactiveLabel: false, decoration: "flat") {
      state "off", label:'${name}', action:"thermostat.heat", backgroundColor:"#ffffff"
      state "heat", label:'${name}', action:"thermostat.cool", backgroundColor:"#ffa81e"
      state "cool", label:'${name}', action:"thermostat.auto", backgroundColor:"#269bd2"
      state "auto", label:'${name}', action:"thermostat.off", backgroundColor:"#79b821"
    }

    valueTile("combiningFunction", "device.combiningFunction", inactiveLabel: false, decoration: "flat") {
      state "combining", label:'${currentValue}', backgroundColor:"#ffffff"
    }

    controlTile("heatSliderControl", "device.heatingSetpoint", "slider", height: 1, width: 2, inactiveLabel: false) {
      state "setHeatingSetpoint", action:"setHeatingSetpoint", backgroundColor:"#d04e00"
    }
    valueTile("heatingSetpoint", "device.heatingSetpoint", inactiveLabel: false, decoration: "flat") {
      state "heat", label:'${currentValue}° heat', backgroundColor:"#ffffff"
    }
    controlTile("coolSliderControl", "device.coolingSetpoint", "slider", height: 1, width: 2, inactiveLabel: false) {
      state "setCoolingSetpoint", action:"setCoolingSetpoint", backgroundColor: "#1e9cbb"
    }
    valueTile("coolingSetpoint", "device.coolingSetpoint", inactiveLabel: false, decoration: "flat") {
      state "cool", label:'${currentValue}° cool', backgroundColor:"#ffffff"
    }

    main "temperature"
    details(["temperature", "mode","combiningFunction", "heatSliderControl",
      "heatingSetpoint", "coolSliderControl", "coolingSetpoint"])
    // "refresh", "configure"])
  }
}

def installed() {
  sendEvent(name: "temperature", value: 72, unit: "F")
  sendEvent(name: "combiningFunction", value: "max")
  sendEvent(name: "heatingSetpoint", value: 68, unit: "F")
  sendEvent(name: "coolingSetpoint", value: 73, unit: "F")
  sendEvent(name: "thermostatMode", value: "off")
  sendEvent(name: "thermostatFanMode", value: "fanAuto")
}

// Parse incoming device messages to generate events
def parse(String description) {
  log.debug "parse( $description )"
  // def pair = description.split(":")
  // createEvent(name: pair[0].trim(), value: pair[1].trim(), unit:"F")
}

def setTemperature(value) {
  log.debug "setTemperature( $value )"
  sendEvent(name:"temperature", value: value)
  // evaluate(value, degreesF, device.currentValue("coolingSetpoint"))
}

def setThermostatMode(String value) {
  log.debug "setThermostatMode( $value )"
  sendEvent(name: "thermostatMode", value: value)
  // evaluate(device.currentValue("temperature"), device.currentValue("heatingSetpoint"), device.currentValue("coolingSetpoint"))
}

def setCombiningFunc(value) {
  log.debug "setCombiningFunc( $value )"
  sendEvent(name:"combiningFunction", value: value)
  // evaluate(value, degreesF, device.currentValue("coolingSetpoint"))
}

def setHeatingSetpoint(Double degreesF) {
  log.debug "setHeatingSetpoint($degreesF)"

  sendEvent(name: "heatingSetpoint", value: degreesF)
  // evaluate(device.currentValue("temperature"), degreesF, device.currentValue("coolingSetpoint"))
}

def setCoolingSetpoint(Double degreesF) {
  log.debug "setCoolingSetpoint($degreesF)"

  sendEvent(name: "coolingSetpoint", value: degreesF)
  // evaluate(device.currentValue("temperature"), device.currentValue("heatingSetpoint"), degreesF)
}

def evaluate(temp, heatingSetpoint, coolingSetpoint) {
  log.debug "evaluate($temp, $heatingSetpoint, $coolingSetpoint)"

  // def threshold = 1.0
  // def current = device.currentValue("thermostatOperatingState")
  // def mode = device.currentValue("thermostatMode")
  //
  // def heating = false
  // def cooling = false
  // def idle = false
  //
  // if (mode in ["heat","emergency heat","auto"]) {
  //   if (heatingSetpoint - temp >= threshold) {
  //     heating = true
  //     sendEvent(name: "thermostatOperatingState", value: "heating")
  //   }
  //   else if (temp - heatingSetpoint >= threshold) {
  //     idle = true
  //   }
  //   sendEvent(name: "thermostatSetpoint", value: heatingSetpoint)
  // }
  //
  // if (mode in ["cool","auto"]) {
  //   if (temp - coolingSetpoint >= threshold) {
  //     cooling = true
  //     sendEvent(name: "thermostatOperatingState", value: "cooling")
  //   }
  //   else if (coolingSetpoint - temp >= threshold && !heating) {
  //     idle = true
  //   }
  //   sendEvent(name: "thermostatSetpoint", value: coolingSetpoint)
  // }
  // else {
  //   sendEvent(name: "thermostatSetpoint", value: heatingSetpoint)
  // }
  //
  // if (idle && !heating && !cooling) {
  //   sendEvent(name: "thermostatOperatingState", value: "idle")
  // }
}
