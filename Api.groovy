package myPackage

@Grab(group='org.codehaus.groovy.modules.http-builder', module='http-builder', version='0.7.1' )

import groovyx.net.http.*
import static groovyx.net.http.Method.POST
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
            // headers.Accept-Encoding = 'gzip, deflate'
            // headers.Content-Length = 562
            // headers.Content-Type = 'application/json; charset=utf-8'
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

    // deployLB
    //  make sure load balancer happens before anything else

    // can you have more than one internal/external load balancer?

    // whole life cycle of app pushing to internet

    // add load balancer marathon.json's to README of sample app'
}
