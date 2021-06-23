FROM maven
ADD ./ /usr/src/maven
WORKDIR /usr/src/maven
RUN mvn install
CMD mvn exec:java -Dexec.mainClass=NettyHTTP.NettyHTTPServer
