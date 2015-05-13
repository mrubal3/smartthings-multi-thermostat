/**
 *  Based on Keep Me Cozy II by SmartThings
 */

definition(
    name: "Multi-Sensor Thermostat",
    namespace: "mvgrimes",
    author: "mgrimes@cpan.org",
    description: "Use multiple sensors to run thermostat. Use the average, minimum or maximum of multiple sensors.",
    category: "My Apps",
    version: "0.2",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/temp_thermo.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/temp_thermo@2x.png"
)

preferences() {
    section("Choose thermostat... ") {
        input "thermostat", "capability.thermostat"
        input "threshold", "decimal", title: "Theshold (default: 1)", defaultValue: 1
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
    evaluate()
}

def changedLocationMode(evt) {
    log.debug "changedLocationMode mode: $evt.value, heat: $heat, cool: $cool"
    evaluate()
}

def temperatureHandler(evt) {
    evaluate()
}

private evaluate() {
    log.trace("executing evaluate()")

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
    def temps = [ tstatTemp ] + sensors.collect{ it.currentTemp }

    log.debug("therm[${thermostat}] mode: $tstatMode, temp: $tstatTemp, heat: $thermostat.currentHeatingSetpoint, cool: $thermostat.currentCoolingSetpoint")
    sensors.each{ log.debug( "sensor[${it}] temp: ${it.currentTemperature}") }

    if (tstatMode in ["cool","auto"]) {     // air conditioner
        evaluateCooling( tstatTemp, temps )
    }

    if (tstatMode in ["heat","emergency heat","auto"]) {  // heater
        evaluateHeating( tstatTemp, temps )
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
}

private calcTemperature( String func, List temps ){
    def calcTemp
    switch( func ){
        case "average":
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
