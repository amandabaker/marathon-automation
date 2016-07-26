package myPackage

@Grab(group='org.codehaus.groovy.modules.http-builder', module='http-builder', version='0.7.1' )

import groovyx.net.http.*
import static groovyx.net.http.Method.*
import static groovyx.net.http.ContentType.TEXT
import static groovyx.net.http.ContentType.JSON
import groovy.json.JsonOutput

class Api {

    /* ----------------------------- App Stuff ----------------------------- */

    // Deploy an app according to the specified properties of the application 
    def deployApp(properties) {
        def http = new HTTPBuilder( 'http://localhost' )

        def postBody = [
            id: properties.appId,
            cpus: properties.appCpus,
            mem: properties.appMem,
            instances: properties.appInstances, 
            container: [
                type: 'DOCKER',
                docker: [
                    image: properties.appImage,
                    network: 'BRIDGE',
                    portMappings: [[
                        containerPort: properties.appContainerPort,
                        hostPort: properties.appHostPort,
                        servicePort: properties.appServicePort,
                        protocol: 'tcp'
                    ]]
                ]
            ],
            labels: [
                HAPROXY_GROUP: properties.appHaproxyGroup
            ]
        ]
        try {
            http.request( POST, JSON ) { req ->
                uri.path = '/marathon/v2/apps'
                headers.Accept = 'application/json'
                send JSON, postBody

                response.success = { res ->
                printSuccess('POST', res)
                assert res.statusLine.statusCode == 201
                }
            }
        }
        catch (HttpResponseException e){
            def res = e.response
            printFailure('POST', res)
        }
    }

    // Restart an app by the appId
    def restartApp(appId) {
        def http = new HTTPBuilder( 'http://localhost' )

        try {
            http.request( POST, JSON ) { req ->
                uri.path = '/marathon/v2/apps/' + appId + '/restart'
                headers.Accept = 'application/json'
                send JSON, []

                response.success = { res ->
                printSuccess('POST', res)
                assert res.statusLine.statusCode == 200
                }
            }
        }
        catch (HttpResponseException e){
            def res = e.response
            printFailure('POST', res)
        }  
    }

    // Destroy all instances of an app by appId
    def destroyApp(appId) {
        def http = new HTTPBuilder( 'http://localhost' )

        try {
            http.request( DELETE ) {
                uri.path = '/marathon/v2/apps/' + appId
                headers.Accept = 'application/json'

                response.success = { res ->
                printSuccess('DELETE', res)
                assert res.statusLine.statusCode == 200
                }
            }
        }
        catch (HttpResponseException e){
            def res = e.response
            printFailure('DELETE', res)
        }  
    }

    // Scale an app with appId to numInstances
    def scaleApp(appId, numInstances) {
        def http = new HTTPBuilder( 'http://localhost' )

        def postBody = [
            cmd: 'sleep 55',
            instances: numInstances
        ]

        try {
            http.request( PUT, JSON ) { req ->
                uri.path = '/marathon/v2/apps/' + appId
                headers.Accept = 'application/json'
                send JSON, postBody

                response.success = { res ->
                printSuccess('PUT', res)
                assert res.statusLine.statusCode == 200
                }
            }
        }
        catch (HttpResponseException e){
            def res = e.response
            printFailure('PUT', res)
        }  
    }

    /* ---------------------------- Group Stuff ---------------------------- */

    def deployGroup() {

    }

    def scaleGroup() {

    }

    def updateGroup() {

    }

    def removeGroup() {

    }

    /* ---------------------------- Output Stuff --------------------------- */
    def printSuccess(method, res) {
        println("$method response status: ${res.statusLine}")
        println("Success: $res.success")
        println("Status:  $res.status")
        println("Reason:  $res.statusLine.reasonPhrase")
        println("Content: \n${JsonOutput.prettyPrint(JsonOutput.toJson(res.data))}")
    }

    def printFailure(method, res) {
        println("$method failure")
        println("Success: $res.success")
        println("Status:  $res.status")
        println("Reason:  $res.statusLine.reasonPhrase")
        println("Content: \n${JsonOutput.prettyPrint(JsonOutput.toJson(res.data))}")
    }

    // deployLB
    //  make sure load balancer happens before anything else

    // can you have more than one internal/external load balancer?

    // whole life cycle of app pushing to internet

    // add load balancer marathon.json's to README of sample app'
}
