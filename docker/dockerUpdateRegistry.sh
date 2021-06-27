cd ~/Desktop/piazzainit/
docker-compose build
docker image tag redis:alpine localhost:5000/redis:alpine
docker image tag rabbitmq:management-alpine localhost:5000/rabbitmq:management-alpine
docker image tag postgres:alpine localhost:5000/postgres:alpine

docker push -a localhost:5000/redis
docker push -a localhost:5000/rabbitmq
docker push -a localhost:5000/postgres
docker push -a localhost:5000/netty
docker push -a localhost:5000/courseconsumer
docker push -a localhost:5000/mediaconsumer
docker push -a localhost:5000/notificationconsumer
docker push -a localhost:5000/pollconsumer
docker push -a localhost:5000/questionconsumer
docker push -a localhost:5000/userconsumer