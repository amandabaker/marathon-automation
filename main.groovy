
package myPackage

// println 'hello world'
@Grab(group='org.codehaus.groovy.modules.http-builder', module='http-builder', version='0.7.1' )
import groovyx.net.http.*
import static groovyx.net.http.Method.POST
import static groovyx.net.http.ContentType.TEXT
import static groovyx.net.http.ContentType.JSON
import groovy.json.JsonOutput

app = new Api()

// Mongo app for testing
def appProperties = [
    appId: 'mongooble',
    appCpus: 0.1,
    appMem: 128,
    appInstances: 1,
    appImage: 'mongo',
    appContainerPort: 0,
    appHostPort: 0,
    appServicePort: 0,
    appHaproxyGroup: 'internal'
]

def groupProperties = [
        [
            appId: 'mongooble',
            appCpus: 0.1,
            appMem: 256,
            appInstances: 1,
            appImage: 'mongo',
            appContainerPort: 0,
            appHostPort: 0,
            appServicePort: 0,
            appHaproxyGroup: 'internal'
        ],
        [
            appId: 'mongoblet',
            appCpus: 0.1,
            appMem: 256,
            appInstances: 1,
            appImage: 'mongo',
            appContainerPort: 0,
            appHostPort: 0,
            appServicePort: 0,
            appHaproxyGroup: 'internal',
        ]
]

// Deploy Mongo app set in properties above
app.deployApp(appProperties)

// Wait to restart to Marathon doesn't get mad
sleep(1000)

// Restart Mongo app
app.restartApp('mongooble')

sleep(5000)

app.scaleApp('mongooble', 5)

sleep(5000)

// Delete Mongo app
app.destroyApp('mongooble')