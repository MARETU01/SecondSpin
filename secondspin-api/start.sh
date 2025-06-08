#!/bin/bash

export YOUR_DOCKER_SERVER_IP=39.108.112.191

SERVICES=("gateway" "user-service" "product-service" "order-service" "payment-service" "message-service")

for service in "${SERVICES[@]}"
do
    mvn -pl "$service" spring-boot:run &
    sleep 5
done

wait