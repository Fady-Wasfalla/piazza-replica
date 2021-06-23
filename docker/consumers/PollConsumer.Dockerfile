FROM maven:3-openjdk-16
ADD ./ /usr/src/maven
WORKDIR /usr/src/maven
RUN mvn install
CMD mvn exec:java -Dexec.mainClass="RabbitMQ.PollConsumer" 
