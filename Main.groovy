package marathonAutomation

api = new Api()
api.init('http://localhost', true)

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
    ],
    appPortMappings: [
        [
            containerPort: 1000,
            hostPort: 0,
            servicePort: 0,
            protocol: 'tcp'
        ],
        [
            containerPort: 27017,
            hostPort: 0,
            servicePort: 0,
            protocol: 'tcp'
        ]
    ],
    appParameters: [
        [key: 'a-docker-option', value: 'xxx'],
        [key: 'b-docker-option', value: 'yyy']
    ],
    appVolumes: [
        [
            containerPath: 'data',
            mode: 'RW',
            persistent: [
                size: 64
            ]
        ]
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
api.deployApp(appProperties)

// Wait to restart to Marathon doesn't get mad
sleep(1000)

// Restart Mongo app
api.restartApp(appProperties.appId)

// Wait for Marathon to do stuff
sleep(5000)

// Scale app for show
api.scaleApp(appProperties.appId, scaleUp)

// More waiting
sleep(5000)

// Delete Mongo app
api.destroyApp(appProperties.appId)

// api.deployLoadBalancer('external')
// api.deployLoadBalancer('internal')

api.checkHasLoadBalancer('external')
api.checkHasLoadBalancer('internal')
