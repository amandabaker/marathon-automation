package marathonAutomation

// mongo app
def mongoProperties = [
    appId: 'mongo',
    appCpus: 0.1,
    appMem: 128,
    appInstances: 1,
    appImage: 'mongo',
    appContainerPort: 27017,
    appHostPort: 0,
    appServicePort: 10001,
    appHaproxyGroup: 'internal',
    appLabels: [
        HAPROXY_GROUP: 'internal'
    ]
]

// node-web-app
def nodeProperties = [
    appId: 'node-web-app',
    appCpus: 0.5,
    appMem: 512,
    appInstances: 1,
    appImage: 'amandabaker/node-web-app',
    appContainerPort: 80,
    appHostPort: 0,
    appServicePort: 10000,
    appHaproxyGroup: 'external',
    appLabels: [
        HAPROXY_GROUP: 'external',
        HAPROXY_0_MODE: 'http',
        HAPROXY_0_VHOST: 'abacsprodagents.westus.cloudapp.azure.com'
    ],
    appForcePullImage: true,
    appHealthChecks: [
        [
            protocol: 'HTTP',
            path: '/',
            portIndex: 0,
            timeoutSeconds: 10,
            gracePeriodSeconds: 10,
            intervalSeconds: 2,
            maxConsecutiveFailures: 10
        ]
    ]
]

// Deploy a mongo and a node-web-app container, and loadBalancers if necessary

app = new Api()
app.init('http://localhost', true)
//app.deployApp (mongoProperties)
app.deployApp (nodeProperties)

