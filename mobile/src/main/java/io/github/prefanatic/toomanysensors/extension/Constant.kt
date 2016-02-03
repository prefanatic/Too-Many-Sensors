/*
 * Copyright 2015-2016 Cody Goldberg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.prefanatic.toomanysensors.extension

val PATH_START = "teletremor/start"
val PATH_STOP = "teletremor/stop"

val PATH_TRANSFER_DATA = "teletremor/transfer/data"
val PATH_TRANSFER_SENSOR_LIST = "teletremor/transfer/sensor/list"

val PATH_REQUEST_SENSORS = "teletremor/request/sensors"

val PATH_ERROR = "teletremor/error"

val BUFFER_SIZE = 60

val STATE_ACTIVE = "state.active"
val STATE_NODE_NAMES = "state.node.names"
val STATE_NODE_ID = "state.node.id"
val STATE_NODE_SELECTED = "state.node.selected"
val STATE_SENSOR_NAMES = "state.sensor.names"
val STATE_SENSOR_TYPES = "state.sensor.types"
val STATE_SENSOR_SELECTED = "state.sensor.selected"

val STATE_NAV = "state.navigation"

val PREF_SAMPLING_RATE = "pref.sampling.rate"
val PREF_SAMPLING_RATE_UNIT = "pref.sampling.rate.unit"