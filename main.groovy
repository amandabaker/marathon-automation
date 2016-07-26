
package myPackage

// println 'hello world'
@Grab(group='org.codehaus.groovy.modules.http-builder', module='http-builder', version='0.7.1' )
import groovyx.net.http.*
import static groovyx.net.http.Method.POST
import static groovyx.net.http.ContentType.TEXT
import static groovyx.net.http.ContentType.JSON
import groovy.json.JsonOutput

app = new Api()

def properties = [
    appId: 'mongooble',
    appCpus: 0.1,
    appMem: 256,
    appInstances: 1,
    appImage: 'mongo',
    appContainerPort: 0,
    appHostPort: 0,
    appServicePort: 0,
    appHaproxyGroup: 'internal',
]

app.deployApp(properties)

// class Main {
//   static void main(String... args) {
//     def app = new Api()
//     app.deployMongo()
//   }
// }

// @Grab(group='org.codehaus.groovy.modules.http-builder', module='http-builder', version='0.7.1' )

// import groovyx.net.http.*
// import static groovyx.net.http.Method.POST
// import static groovyx.net.http.ContentType.TEXT
// import static groovyx.net.http.ContentType.JSON
// import groovy.json.JsonOutput
 
// def http = new HTTPBuilder( 'http://localhost' )
// def postBody = [
//   id: 'mongoop',
//   cpus: 0.1,
//   mem: 512.0,
//   instances: 1, 
//   container: [
//     type: 'DOCKER',
//     docker: [
//       image: 'mongo',
//       network: 'BRIDGE',
//       portMappings: [[
//         containerPort: 0,
//         hostPort: 0,
//         servicePort: 10001,
//         protocol: 'tcp'
//       ]]
//     ]
//   ],
//   labels: [
//     HAPROXY_GROUP: 'internal'
//   ]
// ]
// try {
//   http.request( POST, JSON ) { req ->
//     uri.path = '/marathon/v2/apps'
//     headers.Accept = 'application/json'
//     // headers.Accept-Encoding = 'gzip, deflate'
//     // headers.Content-Length = 562
//     // headers.Content-Type = 'application/json; charset=utf-8'
//     send JSON, postBody

//     response.success = { res ->
//       println "POST response status: ${res.statusLine}"
//       println("Success: $res.success")
//       println("Status:  $res.status")
//       println("Reason:  $res.statusLine.reasonPhrase")
//       println("Content: \n${JsonOutput.prettyPrint(JsonOutput.toJson(res.data))}")
//       assert res.statusLine.statusCode == 201
//     }
//   }
// }
// catch (HttpResponseException e){
//     r = e.response
//     println("Success: $r.success")
//     println("Status:  $r.status")
//     println("Reason:  $r.statusLine.reasonPhrase")
//     println("Content: \n${JsonOutput.prettyPrint(JsonOutput.toJson(r.data))}")
// }


// NOT USED
// http.post( path: 'marathon/v2/apps', body: postBody,
//             requestContentType: JSON ) { res ->
//   println "POST Success: ${res.statusLine}"
//   // println ${res}
//   // assert res.statusLine.statusCode == 201
// }

// http.request(GET,JSON) { req ->
//   uri.path = '/' // overrides any path in the default URL
//   headers.'User-Agent' = 'Mozilla/5.0'
 
//   response.success = { resp, reader ->
//     assert resp.status == 200
//     println "My response handler got response: ${resp.statusLine}"
//     println "Response length: ${resp.headers.'Content-Length'}"
//     System.out << reader // print response reader
//   }
 
//   // called only for a 404 (not found) status code:
//   response.'404' = { resp ->
//     println 'Not found'
//   }
// }