package myPackage

app = new Api()
app.init('http://localhost', true)

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
    appHaproxyGroup: 'internal',
    appCmd: 'env && sleep 300',
    appEnv: [
        LD_LIBRARY_PATH: '/usr/local/lib/myLib',
        HA_HE_HE_HA: '/usr/lol/no/thx'
    ],
    appAcceptResourceRoles: [
        'role1', '*'
    ],
    appLabels: [
        HAPROXY_GROUP: 'internal'
    ]
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

// Wait for Marathon to do stuff
sleep(5000)

// Scale app for show
app.scaleApp(appProperties.appId, scaleUp)

// More waiting
sleep(5000)

// Delete Mongo app
app.destroyApp(appProperties.appId)

// app.deployLoadBalancer('external')
// app.deployLoadBalancer('internal')

app.checkHasLoadBalancer('external')
app.checkHasLoadBalancer('internal')
