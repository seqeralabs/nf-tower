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

package io.seqera.tower.util

import java.time.Instant
import java.time.OffsetDateTime

import io.seqera.tower.domain.AccessToken
import io.seqera.tower.domain.ResourceData
import io.seqera.tower.domain.Role
import io.seqera.tower.domain.Task
import io.seqera.tower.domain.User
import io.seqera.tower.domain.UserRole
import io.seqera.tower.domain.Workflow
import io.seqera.tower.domain.WorkflowComment
import io.seqera.tower.domain.WorkflowMetrics
import io.seqera.tower.enums.TaskStatus
import org.grails.datastore.mapping.validation.ValidationException
import org.hibernate.Session

class DomainCreator {

    Boolean save = true
    Boolean validate = true
    Boolean failOnError = true
    Boolean withNewTransaction = true

    static void cleanupDatabase() {
        Workflow.withNewTransaction {
            WorkflowComment.deleteAll(WorkflowComment.list())
            WorkflowMetrics.deleteAll(WorkflowMetrics.list())
            Task.deleteAll(Task.list())
            Workflow.deleteAll(Workflow.list())
            AccessToken.deleteAll(AccessToken.list())
            UserRole.deleteAll(UserRole.list())
            Role.deleteAll(Role.list())
            User.deleteAll(User.list())
        }
    }

    static void cleanupMysqlDb() {
        User.withNewSession { Session session ->
            User.withNewTransaction {
                def tables = session.createSQLQuery("select t.table_name from information_schema.tables t where t.table_schema = 'tower' and t.table_name not like 'flyway%'").list()
                session.createSQLQuery("SET FOREIGN_KEY_CHECKS=0;").executeUpdate()
                try {
                    tables.each {
                        session.createSQLQuery("truncate table $it").executeUpdate()
                    }
                }
                finally {
                    session.createSQLQuery("SET FOREIGN_KEY_CHECKS=1;").executeUpdate()
                }
            }
        }
    }

    Workflow createWorkflow(Map fields = [:]) {
        Workflow workflow = new Workflow()

        fields.owner = fields.containsKey('owner') ? fields.owner : createUser()

        fields.sessionId = fields.containsKey('sessionId') ? fields.sessionId : "35cce421-4712-4da5-856b-6557635e54${generateUniqueNamePart()}d".toString()
        fields.runName = fields.containsKey('runName') ? fields.runName : "astonishing_majorana${generateUniqueNamePart()}".toString()
        fields.submit = fields.containsKey('submit') ? fields.submit : OffsetDateTime.now()
        fields.start = fields.containsKey('start') ? fields.start : fields.submit

        fields.projectDir = fields.containsKey('projectDir') ? fields.projectDir : "/home/user/.nextflow/assets/nextflow-io/hello"
        fields.profile = fields.containsKey('profile') ? fields.profile : "standard"
        fields.homeDir = fields.containsKey('homeDir') ? fields.homeDir : "/home/user"
        fields.workDir = fields.containsKey('workDir') ? fields.workDir : "/home/user/Programs/nextflow/work"
        fields.container = fields.containsKey('container') ? fields.container : "nextflow/bash"
        fields.commitId = fields.containsKey('commitId') ? fields.commitId : "a9012339ce857d6ec7a078281813d8a93645a3e7"
        fields.repository = fields.containsKey('repository') ? fields.repository : "https://github.com/nextflow-io/hello.git"
        fields.scriptFile = fields.containsKey('scriptFile') ? fields.scriptFile : "/home/user/.nextflow/assets/nextflow-io/hello/main.nf"
        fields.userName = fields.containsKey('userName') ? fields.userName : "user"
        fields.launchDir = fields.containsKey('launchDir') ? fields.launchDir : "/home/user/Programs/nextflow"
        fields.scriptId = fields.containsKey('scriptId') ? fields.scriptId : "537c2298d228991eb8a3f7dc1001816f"
        fields.revision = fields.containsKey('revision') ? fields.revision : "master"
        fields.commandLine = fields.containsKey('commandLine') ? fields.commandLine : "./nextflow-19.05.0-TOWER-all run hello -with-tower"
        fields.projectName = fields.containsKey('projectName') ? fields.projectName : "nextflow-io/hello"
        fields.scriptName = fields.containsKey('scriptName') ? fields.scriptName : "main.nf"

        createInstance(workflow, fields)
    }

    Task createTask(Map fields = [:]) {
        Task task = new Task()

        fields.workflow = fields.containsKey('workflow') ? fields.workflow : createWorkflow()
        fields.taskId = fields.containsKey('taskId') ? fields.taskId : generateUniqueNumber()
        fields.name = fields.containsKey('name') ? fields.name : "taskName_${generateUniqueNamePart()}"
        fields.hash = fields.containsKey('hash') ? fields.hash : "taskHash_${generateUniqueNamePart()}"
        fields.status = fields.containsKey('status') ? fields.status : TaskStatus.SUBMITTED
        fields.submit = fields.containsKey('submit') ? fields.submit : OffsetDateTime.now()

        createInstance(task, fields)
    }

    WorkflowMetrics createWorkflowMetrics(Workflow workflow, Map fields = [:]) {
        WorkflowMetrics metrics = new WorkflowMetrics()
        metrics.workflow = workflow

        fields.process = fields.containsKey('process') ? fields.process: "magnitude_${generateUniqueNamePart()}"
        fields.cpu = fields.containsKey('cpu') ? fields.cpu : embedResourceData()

        createInstance(metrics, fields)
    }


    ResourceData embedResourceData(Map fields = [:]) {
        ResourceData resource = new ResourceData()

        fields.mean = fields.containsKey('mean') ? fields.mean : 0.0
        fields.min = fields.containsKey('min') ? fields.min : 0.0
        fields.q1 = fields.containsKey('q1') ? fields.q1 : 0.0
        fields.q2 = fields.containsKey('q2') ? fields.q2 : 0.0
        fields.q2 = fields.containsKey('q2') ? fields.q2 : 0.0
        fields.q3 = fields.containsKey('q3') ? fields.q3 : 0.0
        fields.max = fields.containsKey('max') ? fields.max : 0.0
        fields.minLabel = fields.containsKey('minLabel') ? fields.minLabel : 'minLabel'
        fields.maxLabel = fields.containsKey('maxLabel') ? fields.maxLabel : 'maxLabel'
        fields.q1Label = fields.containsKey('q1Label') ? fields.q1Label : 'q1Label'
        fields.q2Label = fields.containsKey('q2Label') ? fields.q2Label : 'q2Label'
        fields.q3Label = fields.containsKey('q3Label') ? fields.q3Label : 'q3Label'

        populateInstance(resource, fields)
    }

    User createUser(Map fields = [:]) {
        User user = new User()

        fields.email =  fields.containsKey('email') ? fields.email : "user${generateUniqueNamePart()}@email.com"
        fields.userName =  fields.containsKey('userName') ? fields.userName : "user${generateUniqueNamePart()}"
        fields.authToken = fields.containsKey('authToken') ? fields.authToken : "authToken${generateUniqueNamePart()}"
        fields.authTime =  fields.containsKey('authTime') ? fields.authTime : Instant.now()
        fields.accessTokens = fields.containsKey('accessTokens') ? fields.accessTokens : [new DomainCreator(save: false).createAccesToken(user: user)]
        
        createInstance(user, fields)
    }

    AccessToken createAccesToken(Map fields = [:]) {
        AccessToken accessToken = new AccessToken()

        fields.token =  fields.containsKey('token') ? fields.token : "accessToken${generateUniqueNamePart()}"
        fields.name =  fields.containsKey('name') ? fields.name : 'default'
        fields.dateCreated = fields.containsKey('dateCreated') ? fields.dateCreated : Instant.now()
        fields.user = fields.containsKey('user') ? fields.user : createUser(acessTokens: accessToken)

        createInstance(accessToken, fields)
    }

    User createUserWithRole(Map fields = [:], String authority) {
        User user = createUser(fields)
        createUserRole(user: user, role: createRole(authority: authority))
        return user
    }

    UserRole createUserRole(Map fields = [:]) {
        UserRole userRole = new UserRole()

        fields.user =  fields.containsKey('user') ? fields.user : createUser()
        fields.role =  fields.containsKey('role') ? fields.role : createRole()

        createInstance(userRole, fields)
    }

    Role createRole(Map fields = [:]) {
        Role role = new Role()

        fields.authority =  fields.containsKey('authority') ? fields.authority : "ROLE_${generateUniqueNamePart()}"

        createInstance(role, fields)
    }

    User generateAllowedUser() {
        createUserWithRole([:], 'ROLE_USER')
    }

    User generateNotAllowedUser() {
        createUserWithRole([:], 'ROLE_INVALID')
    }

    /**
     * Populates and persists (if the meta params say so) an instance of a class in the database given their params
     * @param clazz the class to create the instance of
     * @param params the params to create the instance, it can contain lists too
     * @param persist if the instance to save is persisted in the database (true by default)
     * @param validate if the instance to save needs to be validated (true by default)
     * @return the persisted instance
     */
    private def createInstance(def instance, Map params) {
        populateInstance(instance, params)
        persistInstance(instance)
    }

    private def populateInstance(def instance, Map params) {
        Map regularParams = [:]
        Map listParams = [:]
        extractListsFromMap(params, regularParams, listParams)

        regularParams.each { String k, def v ->
            if (instance.hasProperty(k)) {
                try {
                    instance[k] = v
                }
                catch( Exception e ) {
                    throw new IllegalStateException("Cannot set field=$k with value=$v", e)
                }
            }
        }
        listParams.each { String k, List v ->
            addAllInstances(instance, k, v)
        }

        instance
    }

    private def persistInstance(def instance) {
        if (!save) {
            return instance
        }

        def saveEntity = {
            // call explicitly entity validation due to gorm bug
            // https://github.com/grails/gorm-hibernate5/issues/110
            if( validate && !instance.validate() ) {
                if( failOnError )
                    throw new ValidationException("Validation Error(s) occurred during save()", instance.errors)
                return instance
            }
            instance.save(validate: validate, failOnError: failOnError)
            return instance
        }

        if (withNewTransaction) {
            return instance.withNewTransaction(saveEntity)
        }

        return saveEntity()
    }

    /**
     * Separates the entries whose value is a regular instance from the entries whose value is a list instance of a map
     * @param params the map with all the params for an instance
     * @param regularParams the map to populate with entries whose value is a regular instance
     * @param listsParams noLists the map to populate with entries whose value is a list instance
     */
    private void extractListsFromMap(Map params, Map regularParams, Map listsParams) {
        params?.each { k, v ->
            if (v instanceof List) {
                listsParams[k] = v
            } else {
                regularParams[k] = v
            }
        }
    }

    /**
     * Associates the objects contained in the collection to the corresponding property of the given instance
     * @param instance the instance to populate its collection
     * @param collectionName the name of the collection property
     * @param collection the collection which contains the instances to add
     */
    private void addAllInstances(def instance, String collectionName, List collection) {
        collection?.each {
            instance."addTo${collectionName.capitalize()}"(it)
        }
    }

    private Long generateUniqueNumber() {
        UniqueIdentifierGenerator.generateUniqueId()
    }

    /**
     * Generate a unique string in order to make a name distinguishable
     * @return a random string with a low probability of collision with other generated strings
     */
    private String generateUniqueNamePart() {
        "${UniqueIdentifierGenerator.generateUniqueId()}"
    }
}

class UniqueIdentifierGenerator {

    private static long uniqueIdentifier = 0

    /**
     * Generates an unique numeric id
     * @return an unique numeric id
     */
     static long generateUniqueId() {
        uniqueIdentifier++
    }
}
