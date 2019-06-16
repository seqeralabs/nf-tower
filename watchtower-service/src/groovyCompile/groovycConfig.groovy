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

import groovy.transform.CompileStatic
import groovy.transform.ToString
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.control.SourceUnit

withConfig(configuration) {
    //CompileStatic by default. Can be opt-out'ed by using @CompileDynamic
    ast(CompileStatic)

    //@ToString(includeNames = true, includeSuper = true, includePackage = false, ignoreNulls = true). Interfaces excluded
    source(unitValidator: { SourceUnit unit -> !unit.AST.classes.any { ClassNode node -> node.isInterface() } }) {
        ast(includeNames: true, includePackage: false, ignoreNulls: true, ToString)
    }

}