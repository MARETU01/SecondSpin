#!/bin/bash

SERVICES=("gateway" "user-service" "product-service" "order-service" "payment-service" "message-service")

for service in "${SERVICES[@]}"
do
    (cd "$service" && mvn spring-boot:run &)
    sleep 5
done

wait