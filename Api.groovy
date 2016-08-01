package marathonAutomation

@Grab(group='org.codehaus.groovy.modules.http-builder', module='http-builder', version='0.7.1' )

import groovyx.net.http.*
import static groovyx.net.http.Method.*
import static groovyx.net.http.ContentType.TEXT
import static groovyx.net.http.ContentType.JSON
import groovy.json.JsonOutput

class Api {

    def baseUrl
    def http
    def autoDeployMLB   // MLB == Marathon Load Balancer

    def init (appBaseUrl, appAutoDeployMLB) {
        baseUrl = appBaseUrl
        http = new HTTPBuilder (baseUrl)
        autoDeployMLB = appAutoDeployMLB
    }

    /* ----------------------------- App Stuff ----------------------------- */

    // Deploy an app according to the specified properties 'p' of the application 
    def deployApp (p) {
        // 'p' is a map of the properties of the app

        //  It might be useful to check the properties 'p' passed in to ensure that
        //    the data is valid. Maybe warn if p does not contain:
        //    cpu, mem, container.docker.image, constraints
        
        // If user chooses to deploy marathon LB's automatically
        if (autoDeployMLB) {
            // Check if there is already a LB of specified type (external/internal)
            if (!checkHasLoadBalancer(p.appHaproxyGroup)) {
                deployLoadBalancer(p.appHaproxyGroup)
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

    def deployAppBodyBuilder (p) {
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
                    portMappings: [[ // what if multiple port mappings?
                        containerPort: p.appContainerPort ?: 0,
                        hostPort: p.appHostPort ?: 0,
                        servicePort: p.appServicePort ?: 0,
                        protocol: 'tcp'
                    ]],
                    privileged: p.appIsPrivileged ?: false,
                    parameters: [], // can have multiple parameters
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


        // TODO: these are nested within something else and are being sassy so they can hold up
        if (p?.appPortMappings) {}
        if (p?.appParameters) {}
        if (p?.appVolumes) {}
        return postBody
    }

    // Restart an app by the appId
    def restartApp (appId) {
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
    def destroyApp (appId) {
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
    def scaleApp (appId, numInstances) {
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
    def checkHasLoadBalancer (type) {
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
    def deployLoadBalancer (type) {
        // 'type' is either 'internal' or 'external' 
            //def deployApp (p) {
        // 'p' is a map of the properties of the app

        //  It might be useful to check the properties 'p' passed in to ensure that
        //    the data is valid. Maybe warn if p does not contain:
        //    cpu, mem, container.docker.image, constraints

        def postBody

        if (type == 'external') {
            postBody = new File('json/marathon-lb-external.json').text

        }
        else {
            postBody = new File('json/marathon-lb-internal.json').text
        }

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

    // Read the configuration for a load balancer from json file
    def deployLoadBalancerBodyBuilder (p) {}



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

    // deployLB
    //  make sure load balancer happens before anything else

    // can you have more than one internal/external load balancer?

    // whole life cycle of app pushing to internet
}
