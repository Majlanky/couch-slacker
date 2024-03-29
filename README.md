# Couch Slacker 
[![Build Status](https://travis-ci.com/Majlanky/couch-slacker.svg?branch=master)](https://travis-ci.com/Majlanky/couch-slacker)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=com.groocraft%3Acouch-slacker&metric=coverage)](https://sonarcloud.io/dashboard?id=com.groocraft%3Acouch-slacker)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=com.groocraft%3Acouch-slacker&metric=sqale_rating)](https://sonarcloud.io/dashboard?id=com.groocraft%3Acouch-slacker)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=com.groocraft%3Acouch-slacker&metric=security_rating)](https://sonarcloud.io/dashboard?id=com.groocraft%3Acouch-slacker)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=com.groocraft%3Acouch-slacker&metric=reliability_rating)](https://sonarcloud.io/dashboard?id=com.groocraft%3Acouch-slacker)
[![Known Vulnerabilities](https://snyk.io/test/github/majlanky/couch-slacker/badge.svg)](https://snyk.io/test/github/majlanky/couch-slacker)  
![](https://img.shields.io/badge/compatibility-JDK8%20and%20higher-purple)

Couch Slacker project started in 2020. Basic idea and motivation was to create [Spring Data](https://spring.io/projects/spring-data) support for
[CouchDB](https://couchdb.apache.org/) which is awesome and light document database. We felt in love with it because of easy clustering, nice API and good
documentation. [Spring Data](https://spring.io/projects/spring-data) is an awesome tool how to make development faster. So we decided to join it together.

#### Why to use Couch Slacker?
Differently from other projects, Couch Slacker not only Spring Data like library. It is true [Spring Data](https://spring.io/projects/spring-data) 
implementation [CouchDB](https://couchdb.apache.org/) which makes Couch Slacker very powerful. You can start you project with Spring on 
[CouchDB](https://couchdb.apache.org/) with Couch Slacker and switch to MongoDB or other database engine by simple change of dependency (and probably 
some little changes in entity/POJO classes). *No massive refactorings needed.* The other reason is that developer does not need to know Couch Slacker well. 
Knowledge of [Spring Data](https://spring.io/projects/spring-data) is enough.

Simply said Couch Slacker let you relax on the [CouchDB](https://couchdb.apache.org/) as [Spring Data](https://spring.io/projects/spring-data) works for
 you.
 
Artifacts releases are available on maven central (and on pages indexing central):
* [central](https://repo1.maven.org/maven2/com/groocraft/couch-slacker/)
* [mvnRepository](https://mvnrepository.com/artifact/com.groocraft/couch-slacker)

## Wiki
This README contains only basic information about project. For more or detailed information, visit the [wiki](https://github.com/Majlanky/couch-slacker/wiki) 

## Project Focus
* Provide a basic client for [CouchDB](https://couchdb.apache.org/)
* Provide Spring data implementation for [CouchDB](https://couchdb.apache.org/)

### Basic Client Features
* Management of documents
* Management of indexes

### Spring Data Features
* Spring data repositories for [CouchDB](https://couchdb.apache.org/)
* Query methods
* Native queries triggered by query methods

## Getting Started 
First of all we have to add Maven dependency
```xml
<dependency>
    <groupId>com.groocraft</groupId>
    <artifactId>couch-slacker</artifactId>
  <version>${version}</version>
</dependency>
```
### POJO Classes
Both the basic client and repositories works above document POJO classes. Let`s create a POJO class for user. Do you need to map more entities to the same
 database, or you need to know more information about mapping? 
 Visit [this wiki](https://github.com/Majlanky/couch-slacker/wiki/CouchDB-layout-with-Couch-Slacker),
and [this wiki](https://github.com/Majlanky/couch-slacker/wiki/Documents-mapping) 
```java
@Document("user")
public class User {

    @JsonProperty("_id")
    @JsonInclude(Include.NON_NULL)
    private String id;

    @JsonProperty("_rev")
    @JsonInclude(Include.NON_NULL)
    private String revision;

    @JsonProperty("name")
    private String name;
}
```
It looks pretty talkative does not it? Ok, do not worry, here is way to make it shorter:
```java
@Document("user")
public class User extends DocumentBase {

    @JsonProperty("name")
    private String name;

}
```
It is much better. In both examples you can notice [Jackson](https://github.com/FasterXML/jackson) is used. (in the case of name, no annotation is really
 needed, and it is present only as example). If database annotation is missing, lower-cased name of class is used as database name.
### Spring Data Repositories
We have to start with a configuration of connectivity. URL must contain a port. Scheme can be elided, default value is HTTP. Username and password are
 mandatory. More information about the configuration you can find [here](https://github.com/Majlanky/couch-slacker/wiki/Configuration)
```yaml
couchdb:
  client:
    url: http://localhost:5984/
    username: user
    password: password
```
```java
@Configuration
class AppConfig extends CouchSlackerConfiguration {

}
```
Now we are ready to define Spring Data repository for [CouchDB](https://couchdb.apache.org/). Please notice, that [CouchDB](https://couchdb.apache.org/) supports only String IDs.
```java
interface UserRepository extends CrudRepository<User, String> {

}
```
The final step is to use the defined repository in your application.
```java
@SpringBootApplication
@EnableCouchDbRepositories
public class CouchSlackerDemoApplication {

	@Autowired
	UserRepository userRepository;

	public static void main(String[] args) {
		SpringApplication.run(CouchSlackerDemoApplication.class, args);
	}

	@Bean
	public CommandLineRunner runner(){
		return args -> userRepository.save(new User("Majlanky"));
	}

}
```

### Basic Access API
The basic repository is very easy to use. Everything is based on CouchDbClient class. First of all lets show the way completely without 
[Spring](https://spring.io/) framework.
```java
CouchDbProperties properties = new CouchDbProperties("http://localhost:5984/", "admin", "password");
CouchDbClient client = CouchDbClient.builder().properties(properties).build();
``` 

## Limitation
The project is still in its infancy, so there are limitations. There is a list of known limitations:
* Missing API for attachments and views.
* Projections are not tested and probably not working
* Query by example is not tested and probably not working
* Some operations are done very ineffectively (paging for example is done without view and startKey)
* Missing auditing feature
* Not following operations are not implemented for query method
  * Between
  * Near
  * Within

See [issues](https://github.com/Majlanky/couch-slacker/issues) what is known and when it is planned to solve it. If you need something faster than planned, **let us know**.

## Building from Source
Despite the fact you can use Couch Slacker as dependency of your project as it is available on maven central, you can build the 
project by you own. Couch Slacker is a maven project with prepared maven wrapper. Everything you need to do is call 
the following command in the root of the project.
```shell script
$ ./mwnw clean install
```
the previous command skips integration tests. For build with integration tests use (and check wiki for integration tests information):
```shell script
$ ./mwnw clean install -P ITs
```
More information about integration tests of Couch Slacker you can find [here](https://github.com/Majlanky/couch-slacker/wiki/Integration-tests)

However, every build contains integration (and junit) tests which are executed against [CouchDB](https://couchdb.apache.org/) in [Docker](https://www.docker.com/). It is reason, why
[Docker](https://www.docker.com/) must be installed on the machine. If you do not have Docker and do not want to install it, execute the first command.

## Backward Compatibility
Couch Slacker project follows [Apache versioning](https://apr.apache.org/versioning.html)

## Licence
Couch Slacker project is licensed under the [Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0)
