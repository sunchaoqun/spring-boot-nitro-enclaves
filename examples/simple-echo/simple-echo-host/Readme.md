cd ~/spring-boot-nitro-enclaves/examples/simple-echo/simple-echo-host

docker build -t kafka-consumer .

aws ecr-public get-login-password --region us-east-1 | docker login --username AWS --password-stdin public.ecr.aws/s0k4s5w7

docker tag kafka-consumer:latest public.ecr.aws/s0k4s5w7/kafka-consumer:latest

docker push public.ecr.aws/s0k4s5w7/kafka-consumer:latest



