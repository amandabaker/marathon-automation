package myPackage

app = new Api()
app.init('http://localhost', true)

// Mongo app for testing
def mongoProperties = [
    appId: 'mongo',
    appCpus: 0.1,
    appMem: 128,
    appInstances: 1,
    appImage: 'mongo',
    appContainerPort: 27017,
    appHostPort: 0,
    appServicePort: 10001,
    appHaproxyGroup: 'internal'
]

def nodeProperties = [
    appId: 'node-web-app',
    appCpus: 0.1,
    appMem: 128,
    appInstances: 1,
    appImage: 'amandabaker/node-web-app',
    appContainerPort: 80,
    appHostPort: 80,
    appServicePort: 10000,
    appHaproxyGroup: 'external'    
]

// Deploy a mongo and a node-web-app container, and loadBalancers if necessary
app.deployApp (mongoProperties)
app.deployApp (nodeProperties)

