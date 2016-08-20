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
 *  Resources from the SmartThings repo:
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
      description: "Tile to set the target temperature for use by SmartApps like MultiSensorTherm",
      category: "Green Living",
      version: "2.4",
      iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/temp_thermo.png",
      iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/temp_thermo@2x.png"
    ) {

    capability "Thermostat"     // defines a bunch of attr and commands
    attribute "combiningFunction", "enum", [ "ave", "min", "max" ]

    command "setTemperature", ["number"]
    command "setCombiningFunc", ["number"]
    command "tempUp"
    command "tempDown"
  }


  // UI tile definitions
  tiles(scale: 2) {

    multiAttributeTile(name:"thermostatFull", type:"thermostat", width:6, height:4) {
        tileAttribute("device.temperature", key: "PRIMARY_CONTROL") {
            attributeState("default", label:'${currentValue}', unit:"dF")
        }
        tileAttribute("device.temperature", key: "VALUE_CONTROL") {
            attributeState("VALUE_UP", action: "tempUp")
            attributeState("VALUE_DOWN", action: "tempDown")
        }
        // tileAttribute("device.humidity", key: "SECONDARY_CONTROL") {
        //     attributeState("default", label:'${currentValue}%', unit:"%")
        // }
        tileAttribute("device.thermostatOperatingState", key: "OPERATING_STATE") {
            attributeState("idle", backgroundColor:"#44b621")
            attributeState("heating", backgroundColor:"#ffa81e")
            attributeState("cooling", backgroundColor:"#269bd2")
        }
        tileAttribute("device.thermostatMode", key: "THERMOSTAT_MODE") {
            attributeState("off", label:'${name}')
            attributeState("heat", label:'${name}')
            attributeState("cool", label:'${name}')
            attributeState("auto", label:'${name}')
        }
        tileAttribute("device.heatingSetpoint", key: "HEATING_SETPOINT") {
            attributeState("default", label:'${currentValue}', unit:"dF")
        }
        tileAttribute("device.coolingSetpoint", key: "COOLING_SETPOINT") {
            attributeState("default", label:'${currentValue}', unit:"dF")
        }
    }

    // valueTile("temperature", "device.temperature", width: 2, height: 2) {
    //   state("temperature", label:'${currentValue}', unit:"F",
    //     backgroundColors:[
    //       [value: 31, color: "#153591"],
    //       [value: 44, color: "#1e9cbb"],
    //       [value: 59, color: "#90d2a7"],
    //       [value: 74, color: "#44b621"],
    //       [value: 84, color: "#f1d801"],
    //       [value: 95, color: "#d04e00"],
    //       [value: 96, color: "#bc2323"]
    //     ]
    //   )
    // }

    standardTile("mode", "device.thermostatMode", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
      state "off", label:'${name}', action:"thermostat.heat", backgroundColor:"#ffffff"
      state "heat", label:'${name}', action:"thermostat.cool", backgroundColor:"#ffa81e"
      state "cool", label:'${name}', action:"thermostat.auto", backgroundColor:"#269bd2"
      state "auto", label:'${name}', action:"thermostat.off", backgroundColor:"#79b821"
    }

    valueTile("combiningFunction", "device.combiningFunction", inactiveLabel: false, decoration: "flat", width: 4, height: 2) {
      state "combining", label:'${currentValue}', backgroundColor:"#ffffff"
    }

    controlTile("heatSliderControl", "device.heatingSetpoint", "slider", height: 2, width: 4, inactiveLabel: false) {
      state "setHeatingSetpoint", action:"setHeatingSetpoint", backgroundColor:"#d04e00"
    }
    valueTile("heatingSetpoint", "device.heatingSetpoint", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
      state "heat", label:'${currentValue}° heat', backgroundColor:"#ffffff"
    }
    controlTile("coolSliderControl", "device.coolingSetpoint", "slider", height: 2, width: 4, inactiveLabel: false) {
      state "setCoolingSetpoint", action:"setCoolingSetpoint", backgroundColor: "#1e9cbb"
    }
    valueTile("coolingSetpoint", "device.coolingSetpoint", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
      state "cool", label:'${currentValue}° cool', backgroundColor:"#ffffff"
    }

    main "thermostatFull"
    details(["thermostatFull", "mode", "combiningFunction",
      "heatSliderControl", "heatingSetpoint", "coolSliderControl", "coolingSetpoint"])
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

def updated(){
  installed()
}

// Parse incoming device messages to generate events
def parse(String description) {
  log.debug "parse( $description )"
}

def setTemperature(value) {
  log.debug "setTemperature( $value )"
  sendEvent(name:"temperature", value: value)
}

def setThermostatMode(String value) {
  log.debug "setThermostatMode( $value )"
  sendEvent(name: "thermostatMode", value: value)
}

def setCombiningFunc(value) {
  log.debug "setCombiningFunc( $value )"
  sendEvent(name:"combiningFunction", value: value)
}

def tempUp(){ tempAdjust(1) }
def tempDown(){ tempAdjust(-1) }

def tempAdjust(value){
  log.debug( "tempAdjust = ${value}" )
  log.debug( "thermostatMode = ${device.currentState("thermostatMode")}" )

  if( device.currentState("thermostatMode") == "cool" ){
    def ts = device.currentState("coolingSetpoint")
    def degreesF = ts ? ts.integerValue + value : 76
    sendEvent(name: "coolingSetpoint", value: degreesF)
  } else if( device.currentState("thermostatMode") == "heat" ){
    def ts = device.currentState("heatingSetpoint")
    def degreesF = ts ? ts.integerValue + value : 68
    sendEvent(name: "heatingSetpoint", value: degreesF)
  }
}


def setHeatingSetpoint(Double degreesF) {
  log.debug "setHeatingSetpoint($degreesF)"
  sendEvent(name: "heatingSetpoint", value: degreesF)
}

def setCoolingSetpoint(Double degreesF) {
  log.debug "setCoolingSetpoint($degreesF)"
  sendEvent(name: "coolingSetpoint", value: degreesF)
}
