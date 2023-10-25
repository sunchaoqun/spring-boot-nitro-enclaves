# FROM amazoncorretto:8u302
FROM amazonlinux:2

RUN yum install python3 net-tools -y
RUN yum install java-1.8.0-openjdk -y

ARG arch

WORKDIR /app

COPY target/nitro-enclaves-simple-echo-enclave-1.0.0-SNAPSHOT.jar ./app.jar

COPY libs/libvsockj-native-1.0-SNAPSHOT-$arch.so /usr/lib/libvsockj-native-1.0-SNAPSHOT.so
COPY libs/nsm-cli ./nsm-cli

COPY traffic-forwarder.py ./
COPY run.sh ./

RUN chmod +x run.sh

CMD ["/app/run.sh"]
