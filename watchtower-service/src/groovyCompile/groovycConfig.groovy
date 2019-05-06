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