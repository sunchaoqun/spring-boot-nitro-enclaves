#!/bin/bash

# 定义要运行的请求数量
NUM_REQUESTS=100000

# 定义目标URL
TARGET_URL="http://127.0.0.1:7070/echo"

# 使用循环发送多个请求
for ((i=1; i<=NUM_REQUESTS; i++))
do
    echo "Sending request $i to $TARGET_URL"
    
    # 使用curl发送POST请求，包含一些示例数据
    curl -s $TARGET_URL &
done

# 等待所有后台进程完成
wait

echo "All requests have been sent"

# ... existing code ...

java -Xmx8g -Djava.library.path=/home/ec2-user/spring-boot-nitro-enclaves/prebuild-libs \
    -Dvsockj.debug=true \
    -Dvsockj.timeout=30000 \
    -jar /home/ec2-user/spring-boot-nitro-enclaves/examples/simple-echo/simple-echo-host/target/nitro-enclaves-simple-echo-host-1.0.0-SNAPSHOT.jar