# Marathon Automation for Azure

This groovy application exists to simplify the process of integrating container services 
using Marathon into existing programs. It specifically targets Azure and relies on the 
Marathon REST API for communication with the cluster. 

## Setup 

First you have to go through the steps to set up your ACS DC/OS cluster in Azure, including
setting up the SSH tunnel. Once you have gone through those steps, all you need is to install 
Groovy and then run:

``` 
groovyc Api.groovy
groovy Main.groovy
```
This compiles the API and runs the groovy script `Main.groovy`

## Resources

* [Marathon REST API](https://mesosphere.github.io/marathon/docs/rest-api.html)
* [Setting up DC/OS Cluster in Azure](https://azure.microsoft.com/en-us/documentation/articles/container-service-deployment/)
* [Get Groovy](http://www.groovy-lang.org/download.html)