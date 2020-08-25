# Couch Slacker

Couch slacker as project started in 2020. Basic idea and motivation was to create [Spring Data](https://spring.io/projects/spring-data) support for 
[CouchDB](https://couchdb.apache.org/) which is awesome and light document database. We felt in love with it because of easy clustering, nice API and good
 documentation. Spring Data is an awesome tool how to make development faster.

Couch slacker simply let you relax on the [CouchDB](https://couchdb.apache.org/) as [Spring Data](https://spring.io/projects/spring-data) works for you.

## Project Focus
* Provide basic connectivity to CouchDB
* Provide Spring data features for CouchDB

### Basic Features
* API for managing documents
* API for managing indexes

### Spring Data Features
* Spring data repositories for CouchDB
* Native queries triggered by query methods
* Dynamic query generation from query method names
* Implementation of CRUD methods for CouchDB documents

## Getting Started 
First of all we have to add Maven dependency
```xml
<dependency>
   <groupId>com.groocraft.dev-ops</groupId>
   <artifactId>couch-slacker</artifactId>
  <version>${version}-RELEASE</version>
</dependency>
```

### Basic Access API
TODO

### Spring Data Repositories
We have to start with a configuration of connectivity.
```yaml
couchdb:
  client:
    url: http://localhost:5984/
    username: user
    password: password
```
```java
@Configuration
@EnableCouchDbrepositories
class AppConfig extends CouchSlackerConfiguration{

}
```
Now we are ready to use Spring Data repositories for CouchDB
```java
class UserRepository extends CrudRepository<User, String>{

}
```
## Building from Source
Despite the fact you can use Couch Slacker as dependency of your project as it is available on maven central, you can build the 
project by you own. Couch Slacker is a maven project with prepared maven wrapper. Everything you need to do is call 
the following command in the root of the project.
```shell script
$ ./mwnw clean verify
```
However, every build contains integration (and junit) tests which are executed against [CouchDB](https://couchdb.apache.org/) in [Docker](https://www.docker.com/). It is reason, why
[Docker](https://www.docker.com/) must be installed on the machine. If you do not have Docker and do not want to install it, execute the following 
command instead of the previous one.
```shell script
$ ./mwnw clean package
``` 

## Examples

## Troubles solving
 