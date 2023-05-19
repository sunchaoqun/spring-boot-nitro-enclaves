#!/bin/sh
# Assign an IP address to local loopback
ifconfig lo 127.0.0.1

ifconfig lo:0 127.0.0.2

# Add a hosts record, pointing API endpoint to local loopback
echo "127.0.0.1   kms.eu-west-1.amazonaws.com" >> /etc/hosts

echo "127.0.0.2   dynamodb.eu-west-1.amazonaws.com" >> /etc/hosts

nohup python3 /app/traffic-forwarder.py 127.0.0.1 443 -1 8000 &
nohup python3 /app/traffic-forwarder.py 127.0.0.2 443 3 8001 &

java -jar /app/app.jar

