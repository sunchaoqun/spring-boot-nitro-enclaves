#!/bin/bash

# 定义要运行的进程数量
NUM_PROCESSES=100

# 定义基础Java命令
BASE_JAVA_CMD="java -Djava.library.path=/home/ec2-user/spring-boot-nitro-enclaves/prebuild-libs -jar /home/ec2-user/spring-boot-nitro-enclaves/examples/simple-echo/simple-echo-host/target/nitro-enclaves-simple-echo-host-1.0.0-SNAPSHOT.jar"

# 使用循环启动多个进程
for ((i=1; i<=NUM_PROCESSES; i++))
do
    # 生成随机端口号（范围：8000-9000）
    RANDOM_PORT=$(shuf -i 8000-9000 -n 1)
    
    echo "Starting process $i on port $RANDOM_PORT"
    
    # 使用随机端口启动Java进程
    $BASE_JAVA_CMD --server.port=$RANDOM_PORT &
done

# 等待所有后台进程完成
wait

echo "All processes have finished"