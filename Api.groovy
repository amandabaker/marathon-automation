package myPackage

@Grab(group='org.codehaus.groovy.modules.http-builder', module='http-builder', version='0.7.1' )

import groovyx.net.http.*
import static groovyx.net.http.Method.*
import static groovyx.net.http.ContentType.TEXT
import static groovyx.net.http.ContentType.JSON
import groovy.json.JsonOutput

class Api {

    def thing 

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
                println "POST response status: ${res.statusLine}"
                println("Success: $res.success")
                println("Status:  $res.status")
                println("Reason:  $res.statusLine.reasonPhrase")
                println("Content: \n${JsonOutput.prettyPrint(JsonOutput.toJson(res.data))}")
                assert res.statusLine.statusCode == 201
                }
            }
        }
        catch (HttpResponseException e){
            def r = e.response
            println("Success: $r.success")
            println("Status:  $r.status")
            println("Reason:  $r.statusLine.reasonPhrase")
            println("Content: \n${JsonOutput.prettyPrint(JsonOutput.toJson(r.data))}")
        }
    }

    def restartApp(appId) {
        def http = new HTTPBuilder( 'http://localhost' )

        try {
            http.request( POST, JSON ) { req ->
                uri.path = '/marathon/v2/apps/' + appId + '/restart'
                headers.Accept = 'application/json'
                send JSON, []

                response.success = { res ->
                println "POST response status: ${res.statusLine}"
                println("Success: $res.success")
                println("Status:  $res.status")
                println("Reason:  $res.statusLine.reasonPhrase")
                println("Content: \n${JsonOutput.prettyPrint(JsonOutput.toJson(res.data))}")
                assert res.statusLine.statusCode == 200
                }
            }
        }
        catch (HttpResponseException e){
            def r = e.response
            println("Success: $r.success")
            println("Status:  $r.status")
            println("Reason:  $r.statusLine.reasonPhrase")
            println("Content: \n${JsonOutput.prettyPrint(JsonOutput.toJson(r.data))}")
        }  
    }

    def destroyApp(appId) {
        def http = new HTTPBuilder( 'http://localhost' )

        try {
            http.request( DELETE ) {
                uri.path = '/marathon/v2/apps/' + appId
                headers.Accept = 'application/json'

                response.success = { res ->
                println "POST response status: ${res.statusLine}"
                println("Success: $res.success")
                println("Status:  $res.status")
                println("Reason:  $res.statusLine.reasonPhrase")
                println("Content: \n${JsonOutput.prettyPrint(JsonOutput.toJson(res.data))}")
                assert res.statusLine.statusCode == 200
                }
            }
        }
        catch (HttpResponseException e){
            def r = e.response
            println("Success: $r.success")
            println("Status:  $r.status")
            println("Reason:  $r.statusLine.reasonPhrase")
            println("Content: \n${JsonOutput.prettyPrint(JsonOutput.toJson(r.data))}")
        }  
    }

    // deployLB
    //  make sure load balancer happens before anything else

    // can you have more than one internal/external load balancer?

    // whole life cycle of app pushing to internet

    // add load balancer marathon.json's to README of sample app'
}
