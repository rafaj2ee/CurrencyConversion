# CurrencyConversion
--- There are two ways to execute the application, first one is using docker containers <br/><br/>
1- Here what should be done to run the project, first you will need git and docker desktop installed,
clone the project with <br/>
git clone https://github.com/rafaj2ee/CurrencyConversion.git [Enter] <br/>on the project´s root directory, type <br/>
2- docker-compose build [Enter] <br/>
after building the project <br/>
3- docker-compose up [Enter] <br/>
this should put the containers on line, postgres on port 5432 and the springboot microservice on 9090
you can see the swagger generated documentation on <br/>
4- You can import postman collection to access the endpoints wex.postman_collection.json or access<br/>
http://localhost:9090/v2/api-docs<br/>
Enjoy!<br/><br/>
--- Second way is to use the spring boot application directly using sqlite<br/><br/>
1- Here what should be done to run the project, first you will need git and jdk8 installed,
clone the project with <br/>
git clone https://github.com/rafaj2ee/CurrencyConversion.git [Enter] <br/>
on the target directory whatch out for the jar on the root directory that one is the service with Postgres, type <br/>
2- java -jar ./CurrencyConversion-0.0.1-SNAPSHOT.jar [Enter] <br/>
this should put the spring boot app on line, the springboot microservice on 9090
you can see the swagger generated documentation on <br/>
3- You can import postman collection to access the endpoints wex.postman_collection.json or access<br/>
http://localhost:9090/v2/api-docs<br/>
Enjoy! 