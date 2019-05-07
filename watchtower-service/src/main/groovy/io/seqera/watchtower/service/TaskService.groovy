package io.seqera.watchtower.service

import grails.gorm.transactions.Transactional
import groovy.transform.CompileDynamic
import io.seqera.watchtower.domain.Task

interface TaskService {

    Task processTaskJsonTrace(Map taskJson)

}