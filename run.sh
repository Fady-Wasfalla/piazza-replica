sudo kill -9 $(sudo lsof -t -i:8086)
sudo kill -9 $(sudo lsof -t -i:8081)
sudo kill -9 $(sudo lsof -t -i:8080)
mvn clean compile
xterm -e "mvn exec:java -Dexec.mainClass=NettyHTTP.NettyHTTPServer"
xterm -e "mvn exec:java -Dexec.mainClass=RabbitMQ.UserConsumer"
xterm -e "mvn exec:java -Dexec.mainClass=RabbitMQ.Course"