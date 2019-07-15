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

package io.seqera.tower.validation

import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import org.grails.datastore.mapping.validation.ValidationException
import org.springframework.validation.FieldError

/**
 * Helper methods to handle domain validation rules
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@CompileStatic
class ValidationHelper {

    enum Code {
        nullable,
        unique,
        invalid,
        url_invalid('url.invalid'),
        email_invalid('email.invalid');

        final String code
        Code() { code=null }
        Code(String code) { this.code=code }
        String toString() { code ?: super.toString() }
    }

    /**
     * Given an {@link ValidationException} find the al {@link FieldError} for the given validation code eg.
     * {@code
     * @param exception
     * @param code
     * @return
     */
    static List<FieldError> findErrors(ValidationException exception, Code code) {
        List<FieldError> errors = exception.getErrors().getFieldErrors()
        errors.findAll { it.code == code.toString() }
    }

    static FieldError findError(ValidationException exception, Code code) {
        findErrors(exception, code)[0]
    }

    static FieldError findError(ValidationException exception, Code code, String field) {
        List<FieldError> errors = exception.getErrors().getFieldErrors()
        errors.find { it.code == code.toString() && it.field == field }
    }

    @PackageScope
    static Map<String,List<FieldError>> groupErrorsByField(ValidationException exception) {
        List<FieldError> errors = exception.getErrors().getFieldErrors()
        def result = new HashMap<String,List<FieldError>>(errors.size())
        // group by field name
        for( FieldError err : errors ) {
            if( !result.containsKey(err.field) )
                result.put(err.field, new ArrayList(5))
            result.get(err.field) << err
        }

        return result
    }

    static String formatErrors(ValidationException exception) {
        def map = groupErrorsByField(exception)
        if( !map )
            return null
        def result = new ArrayList(map.size())
        map.each { field, errs -> result << formatFieldError(field, errs) }
        return result.join('\n')
    }

    @PackageScope
    static String formatFieldError(String field, List<FieldError> errors) {
        if( !errors )
            return null
        if( errors.size() )
            return formatFieldError(field, errors[0])
        else
            return errors.collect { formatFieldError(field, it) }.join('\n')
    }

    @PackageScope
    static String formatFieldError(String field, FieldError error ) {
        if( error.code=="nullable" ) {
            return "Duplicate record having field '$field' with value: ${error.rejectedValue}"
        }
        else if( error.code == 'unique' ) {
            return "Field '$field' cannot be empty"
        }
        else if( error.code.contains("invalid") ) {
            return "Field '$field' contains an invalid value: ${error.rejectedValue}"
        }
        else {
            return error.getDefaultMessage()
        }

    }

}
