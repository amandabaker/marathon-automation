
package myPackage

app = new Api()
app.baseUrl = 'http://localhost'

// Mongo app for testing
def appProperties = [
    appId: 'mongo',
    appCpus: 0.1,
    appMem: 128,
    appInstances: 1,
    appImage: 'mongo',
    appContainerPort: 27017,
    appHostPort: 0,
    appServicePort: 0,
    appHaproxyGroup: 'internal'
]

    // removed from above:
        // appCmd: 'env && sleep 300',
        // appEnv: [
        //     LD_LIBRARY_PATH: '/usr/local/lib/myLib',
        //     HA_HE_HE_HA: '/usr/lol/no/thx'
        // ],
        // appAcceptResourceRoles: [
        //     'role1', '*'
        // ],
        // appLabels: [
        //     environment: 'staging'
        // ]
        
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
// app.deployApp(appProperties)

// Wait to restart to Marathon doesn't get mad
sleep(1000)

// Restart Mongo app
//app.restartApp(appProperties.appId)

//sleep(5000)

//app.scaleApp(appProperties.appId, scaleUp)

//sleep(5000)

// Delete Mongo app
//app.destroyApp(appProperties.appId)

app.deployApp(appProperties)
app.deployLoadBalancer('external')
app.deployLoadBalancer('internal')

