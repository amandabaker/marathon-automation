
package myPackage

app = new Api()
app.baseUrl = 'http://localhost'

// Mongo app for testing
def appProperties = [
    appId: 'mongoats',
    appCpus: 0.1,
    appMem: 128,
    appInstances: 1,
    appImage: 'mongo',
    appContainerPort: 27017,
    appHostPort: 0,
    appServicePort: 0,
    appHaproxyGroup: 'internal'
]

// Test scaling by scaling up to <scaleUp> instances
def scaleUp = 5 
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
app.restartApp(appProperties.appId)

sleep(5000)

app.scaleApp(appProperties.appId, scaleUp)

sleep(5000)

// Delete Mongo app
app.destroyApp(appProperties.appId)