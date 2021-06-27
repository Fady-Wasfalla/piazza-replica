FROM benoamgad/basepiazzaproject:latest
RUN mvn install
CMD mvn exec:java -Dexec.mainClass=NettyHTTP.NettyHTTPServer
