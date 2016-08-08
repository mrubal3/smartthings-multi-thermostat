# SmartThings Multi-Sensor Thermostat

                                  Provided by
                            Multi-Sensor Thermostat
                        |-------------------------------|

                        +-------------+   +-------------+
                        |             |   |             |       +------------+
                        | Target Temp |<->| MultiSensor |<----->| Thermostat |
                        |  Tile       |   |  SmartApp   |       |   Device   |
    +-----------+   +-->|   Device    |   |             |<---+  +------------+
    | Keep Me   |+  |   |             |   |             |    |
    |  Cozy     ||--+   |             |   |             |<-+ |  +------------+
    +-----------+|      +-------------+   +-------------+  | +--| Temp       |-+
     +-----------+                                         |    |   Sensors  | |
                                                           |    +------------+ |
                                                           |     +-------------+
                                                           |
                                                           |    +------------+
                                                           +----| Presense   |-+
                                                                |   Sensors  | |
                                                                +------------+ |
                                                                 +-------------+

Use the Multi-Sensor Thermostat SmartApp to monitor any number of temperature
sensor. The effective temperature is then calculated by averaging, taking the
maximum or taking the minimum of all the sensors. The target temperature can be
specified in the Multi-Sensor Thermostat configuration, or you can use the
Target Temp Virtual Device to easily specify heating and cooling set points,
and monitor the effective temperature.

Each temperature sensor can be associated with a presence sensor.  The
temperature sensor will be ignored unless the presence sensor detects a
presence.

Changes to the desired set points on changing modes (for example, when the mode
changes from Home to Away) can be done through the SmartThings provided Keep Me
Cozy SmartApp.

## License

Copyright 2016 Mark Grimes

Licensed under the Apache License, Version 2.0 (the "License"); you may not use
this file except in compliance with the License. You may obtain a copy of the
License at:

        http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed
under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
CONDITIONS OF ANY KIND, either express or implied. See the License for the
specific language governing permissions and limitations under the License.
