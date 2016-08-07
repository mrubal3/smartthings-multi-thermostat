/**
 *  Based on Keep Me Cozy II by SmartThings
 */

definition(
    name: "Multi-Sensor Thermostat",
    namespace: "mvgrimes",
    author: "mgrimes@cpan.org",
    description: "Use multiple sensors to run thermostat. Use the average, minimum or maximum of multiple sensors.",
    category: "Green Living",
    version: "0.3",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/temp_thermo.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/temp_thermo@2x.png"
)

preferences() {
    page(name: "page1", title: "Select thermostat and heat/cool settings", nextPage: "page2", uninstall: true){
        section("Choose thermostat... ") {
            input "thermostat", "capability.thermostat"
            input "threshold", "decimal", title: "Theshold (default: 1)", defaultValue: 1
        }
        section("Target Temps..." ) {
            input "targetControl", "capability.thermostat"
        }
        section("Heat setting..." ) {
            input "heatingSetpoint", "decimal", title: "Degrees"
            input "heatingFunction", "enum", title: "Combine via (default: min)",
            required: true, options: [ "ave", "min", "max" ], defaultValue: "min"
        }
        section("Air conditioning setting...") {
            input "coolingSetpoint", "decimal", title: "Degrees"
            input "coolingFunction", "enum", title: "Combine via (default: max)",
            required: true, options: [ "ave", "min", "max" ], defaultValue: "max"
        }
        section("Optionally choose temperature sensors to use instead of the thermostat's... ") {
            input "sensors", "capability.temperatureMeasurement", title: "Temp Sensors", multiple: true, required: false
        }
    }
    page(name: "page2", title: "Presence sensors", nextPage: "page3", install: false, uninstall: false )
    page(name: "page3", title: "Name app and configure modes", install: true, uninstall: true){
        section {
            label title: "Assign a name", required: false
            input "modes", "mode", title: "Set for specific mode(s)", multiple: true, required: false
        }
    }
}

def page2(){
    dynamicPage(name: "page2", install: false, uninstall: false ){
        section("Optionally choose presence sensors to toggle temperature sensors...") {
            sensors.each{
                input "presenceSensorFor$it", "capability.switch",
                    title: "Presence Sensor for $it", multiple: false, required: false
            }
        }
    }
}

def installed() {
    log.debug "enter installed, state: $state"
    subscribeToEvents()
}

def updated() {
    log.debug "enter updated, state: $state"
    unsubscribe()
    subscribeToEvents()
}

def subscribeToEvents() {
    subscribe(location, changedLocationMode)
    sensors.each{ subscribe(it, "temperature", temperatureHandler) }
    subscribe(thermostat, "temperature", temperatureHandler)
    subscribe(thermostat, "thermostatMode", temperatureHandler)
    sensors.each{
        if( settings["presenceSensorFor$it"] ){
            log.debug( "Subscribing to presenceSensorFor${it}" )
            subscribe(settings["presenceSensorFor$it"], "switch", temperatureHandler)
        }
    }
    if( targetControl ){
      subscribe( targetControl, "coolingSetpoint", coolingSetpointHandler )
      subscribe( targetControl, "heatingSetpoint", heatingSetpointHandler )
    }
    evaluate()
}

def changedLocationMode(evt) {
    log.debug "changedLocationMode: mode change to $evt.value"
    evaluate()
}

def temperatureHandler(evt) {
    evaluate()
}

def coolingSetpointHandler(evt){
    log.debug "coolingSetpointHandler: $evt.value"
    settings.coolingSetpoint = evt.value
    sendPush( "Set cooling target to $evt.value" )
    evaluate()
}

def heatingSetpointHandler(evt){
    log.debug "heatingSetpointHandler: $evt.value"
    settings.heatingSetpoint = evt.value
    sendPush( "Set heat target to $evt.value" )
    evaluate()
}

private evaluate() {
    log.trace("executing evaluate()")

    log.debug("location mode: ${location.mode}");
    log.debug(settings);
    // modes.contains( location.mode );
    if( modes && ! modes.contains(location.mode) ) return;

    // If there are no sensors, then just adjust the thermostat's setpoints
    if(! sensors){
        log.info( "setPoints( ${coolingSetpoint} - ${heatingSetpoint} ), no sensors" )
        thermostat.setHeatingSetpoint(heatingSetpoint)
        thermostat.setCoolingSetpoint(coolingSetpoint)
        thermostat.poll()
        return
    }

    def tstatMode = thermostat.currentThermostatMode
    def tstatTemp = thermostat.currentTemperature

    sensors.each{ log.debug( "sensor[${it}] temp: ${it.currentTemperature}") }
    sensors.each{
        log.debug( "presenceSensorFor${it}: ${settings["presenceSensorFor$it"]}")
        if( settings["presenceSensorFor$it"] ){
            log.debug( "- is ${settings["presenceSensorFor$it"].currentSwitch}")
        }
    }

    def temps = sensors.findResults {
        def presenceSensor = settings["presenceSensorFor$it"]
        ( !presenceSensor  || presenceSensor.currentSwitch == "on" ) ? it.currentTemperature : null
    }

    log.debug("therm[${thermostat}] mode: $tstatMode, temp: $tstatTemp, heat: $thermostat.currentHeatingSetpoint, cool: $thermostat.currentCoolingSetpoint")
    log.debug(temps);

    if (tstatMode in ["cool","auto"]) {     // air conditioner
        def virtualTemp = evaluateCooling( tstatTemp, temps )
        if( targetControl ){
          targetControl.setTemperature( virtualTemp )
          targetControl.setCombiningFunc( coolingFunction )
          targetControl.setThermostatMode( tstatMode )
        }
    }

    if (tstatMode in ["heat","emergency heat","auto"]) {  // heater
        def virtualTemp = evaluateHeating( tstatTemp, temps )
        if( targetControl ){
          targetControl.setTemperature( virtualTemp )
          targetControl.setCombiningFun( heatingFunction )
          targetControl.setThermostatMode( tstatMode )
        }
    }
}

private evaluateCooling( Float tstatTemp, List temps ){
    def calcTemp = calcTemperature( coolingFunction, temps )
    log.debug( "target: ${coolingSetpoint}, current ${coolingFunction} temp: ${calcTemp}" )

    if (calcTemp - coolingSetpoint >= threshold) {
        thermostat.setCoolingSetpoint(tstatTemp - 2)
        log.debug( "thermostat.setCoolingSetpoint(${tstatTemp - 2}), ON" )
    }
    else if (coolingSetpoint - calcTemp >= threshold && tstatTemp - thermostat.currentCoolingSetpoint >= threshold) {
        thermostat.setCoolingSetpoint(tstatTemp + 2)
        log.debug( "thermostat.setCoolingSetpoint(${tstatTemp + 2}), OFF" )
    }

    return calcTemp
}

private evaluateHeating( Float tstatTemp, List temps ){
    def calcTemp = calcTemperature( heatingFunction, temps )
    log.debug( "target: ${heatingSetpoint}, curent ${heatingFunction} temp: ${calcTemp}" )

    if (heatingSetpoint - calcTemp >= threshold) {
        thermostat.setHeatingSetpoint(tstatTemp + 2)
        log.debug( "thermostat.setHeatingSetpoint(${tstatTemp + 2}), ON" )
    }
    else if (calcTemp - heatingSetpoint >= threshold && thermostat.currentHeatingSetpoint - tstatTemp >= threshold) {
        thermostat.setHeatingSetpoint(tstatTemp - 2)
        log.debug( "thermostat.setHeatingSetpoint(${tstatTemp - 2}), OFF" )
    }

    return calcTemp
}

private calcTemperature( String func, List temps ){
    def calcTemp
    switch( func ){
        case "ave":
            calcTemp = temps.sum() / temps.size()
            break
        case "max":
            calcTemp = temps.max()
            break
        case "min":
            calcTemp = temps.min()
            break
        default:
            log.error( "bad function: ${func}" )
    }

    return calcTemp
}
