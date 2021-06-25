FROM maven:3-openjdk-16-slim
ADD ./ /usr/src/maven
WORKDIR /usr/src/maven
RUN mvn install
RUN mvn exec:java -Dexec.mainClass=CompileTest
CMD mvn exec:java -Dexec.mainClass="RabbitMQ.QuestionConsumer"
