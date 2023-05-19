FROM amazonlinux:2

RUN yum install java-1.8.0-openjdk -y

WORKDIR /app

COPY target/nitro-enclaves-simple-echo-host-1.0.0-SNAPSHOT.jar ./app.jar

COPY libs/libvsockj-native-1.0-SNAPSHOT.so /usr/lib/prebuild-libs/

COPY run.sh ./

RUN chmod +x run.sh

CMD ["/app/run.sh"]
