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

package io.seqera.tower.service

import javax.inject.Inject

import groovy.transform.CompileStatic
import groovy.transform.Memoized
import groovy.util.logging.Slf4j
import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Infrastructure
import io.micronaut.context.annotation.Value
/**
 * Implements core Tower services
 * 
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@Slf4j
@Infrastructure
@CompileStatic
class TowerServiceImpl implements TowerService {

    static String DEFAULT_SERVER_URL = 'https://tower.nf'

    static String DEFAULT_API_URL = 'https://api.tower.nf'

    @Value('${tower.launch.container:`docker.io/seqeralabs/nf-launcher:20.05.0-edge`}')
    String launchContainer

    @Value('${tower.serverUrl}')
    String serverUrl

    @Inject ApplicationContext ctx

    @Override
    String getLaunchContainer() {
        return launchContainer
    }

    @Override
    String getTowerApiEndpoint() {
        towerApiEndpoint0(DEFAULT_API_URL)
    }

    String getTowerApiEndpointEmptyDefault() {
        towerApiEndpoint0(null)
    }

    protected String towerApiEndpoint0(String defApiUrl) {
        assert serverUrl
        if( serverUrl == DEFAULT_SERVER_URL )
            return defApiUrl
        else
            return "$serverUrl/api"
    }

    Properties readBuildInfo() {
        final BUILD_INFO = '/META-INF/build-info.properties'
        final props = new Properties()
        try {
            props.load( this.class.getResourceAsStream(BUILD_INFO) )
        }
        catch( Exception e ) {
            log.warn "Unable to parse $BUILD_INFO - Cause ${e.message ?: e}"
        }
        return props
    }

    String getLoginPath(ApplicationContext ctx) {
        final envs =  ctx
                .getEnvironment()
                .getActiveNames()
                .findAll{ it.startsWith('auth-') }

        return envs.size()==1 && envs[0].startsWith('auth-oidc')
                ? '/oauth/login/oidc'
                : '/login'
    }

    List<String> getAuthTypes(ApplicationContext ctx) {
        ctx.getEnvironment()
                .getActiveNames()
                .findAll{ it.startsWith('auth-') }
                .collect{ it.substring(5) }
    }

    @Memoized
    @Override
    ServiceInfo getServiceInfo() {
        final info = readBuildInfo()
        final result = new ServiceInfo()
        result.version = info.getProperty('version')
        result.commitId = info.getProperty('commit')
        result.authTypes = getAuthTypes(ctx)
        result.loginPath = getLoginPath(ctx)
        return result
    }
}
