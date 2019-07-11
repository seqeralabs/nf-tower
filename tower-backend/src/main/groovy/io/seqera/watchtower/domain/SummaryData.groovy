/*
 * Copyright (c) 2019, Seqera Labs.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as
 * defined by the Mozilla Public License, v. 2.0.
 */

package io.seqera.watchtower.domain

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import grails.gorm.annotation.Entity
import groovy.transform.CompileDynamic

@Entity
@JsonIgnoreProperties(['dirtyPropertyNames', 'errors', 'dirty', 'attached', 'version', 'tasks'])
@CompileDynamic
class SummaryData {

    Float mean
    Float min
    Float q1
    Float q2
    Float q3
    Float max
    String minLabel
    String maxLabel
    String q1Label
    String q2Label
    String q3Label



}