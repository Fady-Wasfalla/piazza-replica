sudo kill -9 $(sudo lsof -t -i:4000)
sudo kill -9 $(sudo lsof -t -i:8080)
sudo kill -9 $(sudo lsof -t -i:8081)
sudo kill -9 $(sudo lsof -t -i:8082)
sudo kill -9 $(sudo lsof -t -i:8083)
sudo kill -9 $(sudo lsof -t -i:8084)
sudo kill -9 $(sudo lsof -t -i:8085)
sudo kill -9 $(sudo lsof -t -i:8086)
sudo kill -9 $(sudo lsof -t -i:8087)
mvn clean compile

gnome-terminal -- mvn exec:java -Dexec.mainClass=NettyHTTP.NettyHTTPServer
gnome-terminal -- mvn exec:java -Dexec.mainClass=RabbitMQ.UserConsumer
gnome-terminal -- mvn exec:java -Dexec.mainClass=RabbitMQ.QuestionConsumer
gnome-terminal -- mvn exec:java -Dexec.mainClass=RabbitMQ.CourseConsumer
gnome-terminal -- mvn exec:java -Dexec.mainClass=RabbitMQ.PollConsumer
gnome-terminal -- mvn exec:java -Dexec.mainClass=ChatServer.ChatServer