package marathonAutomation

@Grab(group='org.codehaus.groovy.modules.http-builder', module='http-builder', version='0.7.1' )

import groovyx.net.http.*
import static groovyx.net.http.Method.*
import static groovyx.net.http.ContentType.TEXT
import static groovyx.net.http.ContentType.JSON
import groovy.json.*

class Api {

    def baseUrl
    def http
    def autoDeployMLB   // MLB == Marathon Load Balancer

    def init (String appBaseUrl, boolean appAutoDeployMLB) {
        baseUrl = appBaseUrl
        http = new HTTPBuilder (baseUrl)
        autoDeployMLB = appAutoDeployMLB
    }

    /* ----------------------------- App Stuff ----------------------------- */

    // Deploy an app according to the specified properties 'p' of the application 
    def deployApp (Map p) {
        // 'p' is a map of the properties of the app

        //  It might be useful to check the properties 'p' passed in to ensure that
        //    the data is valid. Maybe warn if p does not contain:
        //    cpu, mem, container.docker.image, constraints
        
        // If user chooses to deploy marathon LB's automatically
        if (autoDeployMLB) {
            // Check if there is already a LB of specified type (external/internal)
            if (!checkHasLoadBalancer(p.appHaproxyGroup)) {
                deployLoadBalancer(p.appHaproxyGroup)
                sleep (5000)
            }
        }

        def postBody = deployAppBodyBuilder (p)
        try {
            http.request (POST, JSON) { req ->
                uri.path = '/marathon/v2/apps'
                headers.Accept = 'application/json'
                send JSON, postBody

                response.success = { res ->
                printSuccess ('POST', res)
                assert res.statusLine.statusCode == 201
                }
            }
        }
        catch (HttpResponseException e) {
            def res = e.response
            printFailure ('POST', res)
        }
    }

    def deployAppBodyBuilder (Map p) {
        // 'p' is a map of the properties of the app

        def postBody = [
            id: p.appId,
            cpus: p.appCpus ?: 1,
            mem: p.appMem ?: 512,
            requirePorts: p.requirePorts ?: false,
            instances: p.appInstances ?: 1, 
            executor: p.appExecutor ?: '',
            container: [
                type: 'DOCKER',
                docker: [
                    image: p.appImage,
                    network: 'BRIDGE',
                    privileged: p.appIsPrivileged ?: false,
                    forcePullImage: p.appForcePullImage ?: false
                ],
                volumes: [] // can have Multiple volumes
            ]
        ]

        if (p?.appCmd) {
            postBody.put ('cmd', p.appCmd)
        }   // Can have cmd OR args, but not both
        else if (p?.appArgs) {
            // This is just an array of arguments
            postBody.put ('args', p.appArgs)
        }

        // TODO: many of these would benefit from verification
        if (p?.appLabels) {
            postBody.put ('labels', p.appLabels)
        }
        if (p?.appEnv) {
            postBody.put ('env', p.appEnv)
        }
        if (p?.appAcceptedResourceRoles) {
            postBody.put ('acceptedResourceRoles', p.appAcceptedResourceRoles)
        }
        if (p?.appConstraints) {
            postBody.put ('constraints', p.appConstraints)
        }
        if (p?.appFetch) {
            postBody.put ('fetch', p.appFetch)
        }
        if (p?.appDependencies) {
            postBody.put ('dependencies', p.appDependencies)
        }
        if (p?.appHealthChecks) {
            postBody.put ('healthChecks', p.appHealthChecks)
        }
        if (p?.appPortMappings) {
            postBody.container.docker.put('portMappings', p.appPortMappings)
        }
        if (p?.appParameters) {
            postBody.container.docker.put('parameters', p.appParameters)
        }

        // TODO: Figure out what volumes are about so you can test this before committing to it
        if (p?.appVolumes) {
            // postBody.container.put('volumes', p.appVolumes)
        }

        println postBody

        return postBody
    }

    // Restart an app by the appId
    def restartApp (String appId) {
        // 'appId' is the properties.appId used to create the app

        try {
            http.request (POST, JSON) { req ->
                uri.path = '/marathon/v2/apps/' + appId + '/restart'
                headers.Accept = 'application/json'
                send JSON, []

                response.success = { res ->
                printSuccess ('POST', res)
                assert res.statusLine.statusCode == 200
                }
            }
        }
        catch (HttpResponseException e){
            def res = e.response
            printFailure ('POST', res)
        }  
    }

    // Destroy all instances of an app by appId
    def destroyApp (String appId) {
        // 'appId' is the properties.appId used to create the app

        try {
            http.request (DELETE) {
                uri.path = '/marathon/v2/apps/' + appId
                headers.Accept = 'application/json'

                response.success = { res ->
                printSuccess ('DELETE', res)
                assert res.statusLine.statusCode == 200
                }
            }
        }
        catch (HttpResponseException e){
            def res = e.response
            printFailure ('DELETE', res)
        }  
    }

    // Scale an app with appId to numInstances
    def scaleApp (String appId, int numInstances) {
        // 'appId' is the properties.appId used to create the app
        // 'numInstances' is the number of instances to which the app will be scaled
        
        def postBody = [
            cmd: 'sleep 55',
            instances: numInstances
        ]

        try {
            http.request (PUT, JSON) { req ->
                uri.path = '/marathon/v2/apps/' + appId
                headers.Accept = 'application/json'
                send JSON, postBody

                response.success = { res ->
                    printSuccess ('PUT', res)
                    assert res.statusLine.statusCode == 200
                }
            }
        }
        catch (HttpResponseException e){
            def res = e.response
            printFailure('PUT', res)
        }  
    }



    /* ------------------- Marathon Load Balancer Stuff -------------------- */

    // Before automatically creating a load balancer, check to see if one exists
    def checkHasLoadBalancer (String type) {
        // 'type' is either 'internal' or 'external'
        // get all applications running
        // search for load balancer
        // return whats up

        try {
            http.request (GET, TEXT) { req ->
                uri.path = '/marathon/v2/apps/marathon-lb-' + type + '/tasks'
                headers.Accept = 'application/json'

                response.success = { res, reader ->
                    assert res.statusLine.statusCode == 200
                    println 'Load balancer of type ' + type + ' already exists. Not deploying another.'
                    return true
                }
            }
        }
        catch (HttpResponseException e) {
            def res = e.response
            if (res.statusLine.statusCode == 404) {
                println 'Load balancer of type ' + type + ' does not exist. Deploying load balancer'
            }
            else {
                printFailure('GET', res)
            }
            return false
        }
    }

    // If we need a load balancer, deploy it with a specified type
    def deployLoadBalancer (p) {
        // 'p' is a map of the properties of the app

        def postBody 
        def jsonSlurper = new JsonSlurper()
        
        if (p?.type == 'external') {
            postBody = new File('json/marathon-lb-external.json').text
        }
        else {
            postBody = new File('json/marathon-lb-internal.json').text
        }
        
        postBody = jsonSlurper.parseText(postBody)

        if (p?.cpus)    postBody['cpus'] = p.cpus
        if (p?.mem)     postBody['mem'] = p.mem

        try {
            http.request (POST, JSON) { req ->
                uri.path = '/marathon/v2/apps'
                headers.Accept = 'application/json'
                send JSON, postBody

                response.success = { res ->
                printSuccess ('POST', res)
                assert res.statusLine.statusCode == 201
                }
            }
        }
        catch (HttpResponseException e) {
            def res = e.response
            printFailure ('POST', res)
        }
    }




    /* ---------------------------- Group Stuff ---------------------------- */

    def deployGroup () {}
    def scaleGroup  () {}
    def updateGroup () {}
    def removeGroup () {}



    /* ---------------------------- Output Stuff --------------------------- */

    def printSuccess (method, res) {
        println ("$method response status: ${res.statusLine}")
        println ("Success: $res.success")
        println ("Status:  $res.status")
        println ("Reason:  $res.statusLine.reasonPhrase")
        println ("Content: \n${JsonOutput.prettyPrint(JsonOutput.toJson(res.data))}")
    }

    def printFailure (method, res) {
        println ("$method failure")
        println ("Success: $res.success")
        println ("Status:  $res.status")
        println ("Reason:  $res.statusLine.reasonPhrase")
        println ("Content: \n${JsonOutput.prettyPrint(JsonOutput.toJson(res.data))}")
    }
}
