#!/bin/bash

# Deploy microservices on OVH Cloud
# Author: Claude
# Date: April 30, 2025

set -e

# Color codes for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${GREEN}Starting deployment of microservices on OVH Cloud...${NC}"

# Check if Docker is installed
if ! command -v docker &> /dev/null; then
    echo -e "${RED}Docker is not installed. Installing Docker...${NC}"
    # Update package index
    sudo apt-get update
    # Install required packages
    sudo apt-get install -y apt-transport-https ca-certificates curl software-properties-common
    # Add Docker's official GPG key
    curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo apt-key add -
    # Add Docker repository
    sudo add-apt-repository "deb [arch=amd64] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable"
    # Update package index
    sudo apt-get update
    # Install Docker CE
    sudo apt-get install -y docker-ce
    # Add current user to docker group
    sudo usermod -aG docker $USER
    echo -e "${GREEN}Docker installed successfully!${NC}"
else
    echo -e "${GREEN}Docker is already installed.${NC}"
fi

# Check if Docker Compose is installed
if ! command -v docker-compose &> /dev/null; then
    echo -e "${RED}Docker Compose is not installed. Installing Docker Compose...${NC}"
    # Download Docker Compose
    sudo curl -L "https://github.com/docker/compose/releases/download/v2.15.1/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
    # Apply executable permissions
    sudo chmod +x /usr/local/bin/docker-compose
    echo -e "${GREEN}Docker Compose installed successfully!${NC}"
else
    echo -e "${GREEN}Docker Compose is already installed.${NC}"
fi

# Create directory structure
echo -e "${YELLOW}Creating directory structure...${NC}"
mkdir -p config/{discoveryserver,gateway,authorizationserver,userservice,ebanking,ecreditservice,notificationservice}
mkdir -p keys

# Create keys for JWT authentication
echo -e "${YELLOW}Generating keys for JWT authentication...${NC}"
openssl genrsa -out keys/private.key 2048
openssl rsa -in keys/private.key -pubout -out keys/public.key
chmod 600 keys/private.key keys/public.key

# Copy specific application-prod.yml files for each service
echo -e "${YELLOW}Creating configuration files for each service...${NC}"

# Creating discoveryserver config
cat > config/discoveryserver/application-prod.yml << 'EOF'
spring:
  application:
    name: discoveryserver

  datasource:
    url: jdbc:postgresql://postgresdb:5432/localdb
    username: user2711
    password: admin2711
    hikari:
      connection-test-query: SELECT 1

  jpa:
    database: POSTGRESQL
    open-in-view: false
    generate-ddl: false
    database-platform: org.hibernate.dialect.PostgreSQLDialect
    show-sql: false
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        format_sql: false
        dialect: org.hibernate.dialect.PostgreSQLDialect
        default_schema: localdb

eureka:
  instance:
    hostname: discoveryserver
    prefer-ip-address: true
    ip-address: ${SERVER_IP}
  client:
    fetch-registry: false
    register-with-eureka: false
    service-url:
      defaultZone: http://admin:${EUREKA_PASSWORD}@discoveryserver:5002/eureka/
  server:
    enable-self-preservation: true
    renewal-percent-threshold: 0.85
    wait-time-in-ms-when-sync-empty: 5000
    peer-node-read-timeout-ms: 30000

server:
  port: 5002

logging:
  level:
    com.netflix.discovery: INFO
    org.springframework.cloud.netflix: INFO
EOF

# Creating gateway config
cat > config/gateway/application-prod.yml << 'EOF'
spring:
  application:
    name: gateway
  cloud:
    discovery:
      enabled: true
    gateway:
      discovery:
        locator:
          enabled: true
          lower-case-service-id: true
      routes:
        - id: user-service
          uri: lb://USERSERVICE
          predicates:
            - Path=/user/**
        - id: ebanking
          uri: lb://EBANKING
          predicates:
            - Path=/ebanking/**
        - id: notification-service
          uri: lb://NOTIFICATIONSERVICE
          predicates:
            - Path=/notification/**
        - id: ecredit-service
          uri: lb://ECREDITSERVICE
          predicates:
            - Path=/ecredit/**

eureka:
  instance:
    prefer-ip-address: true
    hostname: gateway
    ip-address: ${SERVER_IP}
    instance-id: ${spring.application.name}:${server.port}
  client:
    service-url:
      defaultZone: http://admin:${EUREKA_PASSWORD}@${EUREKA_HOST}:${EUREKA_PORT}/eureka/
    fetch-registry: true
    register-with-eureka: true
    eureka-server-connect-timeout-seconds: 30
    eureka-server-read-timeout-seconds: 30

logging:
  level:
    com.netflix.discovery: INFO
    org.springframework.cloud.netflix: INFO

server:
  port: 8000

jwks:
  uri: http://${AUTH_SERVER_HOST}:${AUTH_SERVER_PORT}/oauth2/jwks
EOF

# Creating authorizationserver config
cat > config/authorizationserver/application-prod.yml << 'EOF'
spring:
  thymeleaf:
    prefix: classpath:/templates/
    suffix: .html
    cache: true
  application:
    name: authorizationserver
  cloud:
    discovery:
      client:
        composite-indicator:
          enabled: true

  datasource:
    url: jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    hikari:
      connection-test-query: SELECT 1
      maximum-pool-size: 20
      minimum-idle: 5
      idle-timeout: 30000
      connection-timeout: 30000

  main:
    allow-bean-definition-overriding: true

  jpa:
    database: POSTGRESQL
    open-in-view: false
    generate-ddl: false
    database-platform: org.hibernate.dialect.PostgreSQLDialect
    show-sql: false
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        format_sql: false
        dialect: org.hibernate.dialect.PostgreSQLDialect
        default_schema: ${DB_SCHEMA}

eureka:
  instance:
    prefer-ip-address: true
    hostname: authorizationserver
    ip-address: ${SERVER_IP}
  client:
    service-url:
      defaultZone: http://admin:${EUREKA_PASSWORD}@${EUREKA_HOST}:${EUREKA_PORT}/eureka/

ui:
  app:
    url: ${UI_URL}

key:
  private: keys/private.key
  public: keys/public.key

server:
  port: ${SERVER_PORT}
  tomcat:
    max-threads: 200
    min-spare-threads: 20
    max-connections: 10000
    accept-count: 100
    connection-timeout: 20000
EOF

# Creating userservice config
cat > config/userservice/application-prod.yml << 'EOF'
spring:
  thymeleaf:
    prefix: classpath:/templates/
    suffix: .html
    cache: true
  application:
    name: userservice
  cloud:
    discovery:
      client:
        composite-indicator:
          enabled: true

  datasource:
    url: jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    hikari:
      connection-test-query: SELECT 1
      maximum-pool-size: 20
      minimum-idle: 5
      idle-timeout: 30000
      connection-timeout: 30000

  main:
    allow-bean-definition-overriding: true

  jpa:
    database: POSTGRESQL
    open-in-view: false
    generate-ddl: false
    database-platform: org.hibernate.dialect.PostgreSQLDialect
    show-sql: false
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        format_sql: false
        dialect: org.hibernate.dialect.PostgreSQLDialect
        default_schema: ${DB_SCHEMA}

  kafka:
    bootstrap-servers:
      - ${KAFKA_HOST}:${KAFKA_PORT}
    template:
      default-topic: NOTIFICATION_TOPIC
    producer:
      group-id: topicgroupid
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
      properties:
        max.request.size: 1300000000
        acks: all
        retries: 3
    properties:
      spring:
        json:
          type:
            mapping: Notification:io.digiservices.userservice.domain.Notification
      message.max.bytes: 1300000000
      max.request.size: 1300000000

eureka:
  instance:
    prefer-ip-address: true
    hostname: userservice
    ip-address: ${SERVER_IP}
  client:
    service-url:
      defaultZone: http://admin:${EUREKA_PASSWORD}@${EUREKA_HOST}:${EUREKA_PORT}/eureka/

ui:
  app:
    url: ${UI_URL}

key:
  private: keys/private.key
  public: keys/public.key

server:
  port: ${SERVER_PORT}

jwks:
  uri: http://${AUTH_SERVER_HOST}:${AUTH_SERVER_PORT}/oauth2/jwks
EOF

# Creating notificationservice config
cat > config/notificationservice/application-prod.yml << 'EOF'
spring:
  application:
    name: notificationservice

  thymeleaf:
    prefix: classpath:/templates/
    suffix: .html
    cache: true

  mail:
    host: smtp.gmail.com
    port: 587
    username: ${MAIL_USERNAME}
    password: ${MAIL_PASSWORD}
    properties:
      mail:
        smtp:
          writetimeout: 5000
          connectiontimeout: 5000
          timeout: 5000
          auth: true
          starttls:
            enable: true
            required: true
    verify:
      host: ${UI_URL}
    default-encoding: UTF-8

  cloud:
    discovery:
      client:
        composite-indicator:
          enabled: true

  datasource:
    url: jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    hikari:
      connection-test-query: SELECT 1
      maximum-pool-size: 10
      minimum-idle: 3

  main:
    allow-bean-definition-overriding: true

  jpa:
    database: POSTGRESQL
    open-in-view: false
    generate-ddl: false
    database-platform: org.hibernate.dialect.PostgreSQLDialect
    show-sql: false
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        format_sql: false
        dialect: org.hibernate.dialect.PostgreSQLDialect
        default_schema: ${DB_SCHEMA}

  kafka:
    bootstrap-servers:
      - ${KAFKA_HOST}:${KAFKA_PORT}
    template:
      default-topic: NOTIFICATION_TOPIC
    consumer:
      group-id: topicgroupid
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
      properties:
        fetch.message.max.bytes: 1300000000
        max.partition.fetch.bytes: 1300000000
        spring.json.trusted.packages: "io.digiservices.notificationservice.domain,io.digiservices.userservice.domain"
    properties:
      spring:
        json:
          type:
            mapping: Notification:io.digiservices.notificationservice.domain.Notification
      fetch.message.max.bytes: 1300000000

eureka:
  instance:
    prefer-ip-address: true
    hostname: notificationservice
    ip-address: ${SERVER_IP}
  client:
    service-url:
      defaultZone: http://admin:${EUREKA_PASSWORD}@${EUREKA_HOST}:${EUREKA_PORT}/eureka/

ui:
  app:
    url: ${UI_URL}

key:
  private: private.key
  public: public.key

server:
  port: ${SERVER_PORT}

jwks:
  uri: http://${AUTH_SERVER_HOST}:${AUTH_SERVER_PORT}/oauth2/jwks
EOF

# Creating ebanking config
#cat > config/ebanking/application-prod.yml << 'EOF'
#spring:
#  application:
#    name: ebanking
#  cloud:
#    discovery:
#      client:
#        composite-indicator:
#          enabled: true
#
#eureka:
#  instance:
#    prefer-ip-address: true
#    hostname: ebanking
#    ip-address: ${SERVER_IP}
#  client:
#    service-url:
#      defaultZone: http://admin:${EUREKA_PASSWORD}@${EUREKA_HOST}:${EUREKA_PORT}/eureka/
#
#server:
#  port: ${SERVER_PORT}
#
#jwks:
#  uri: http://${AUTH_SERVER_HOST}:${AUTH_SERVER_PORT}/oauth2/jwks
#EOF

# Creating ecreditservice config
cat > config/ecreditservice/application-prod.yml << 'EOF'
spring:
  thymeleaf:
    prefix: classpath:/templates/
    suffix: .html
    cache: true
  application:
    name: ecreditservice
  cloud:
    discovery:
      client:
        composite-indicator:
          enabled: true

  datasource:
    url: jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    hikari:
      connection-test-query: SELECT 1
      maximum-pool-size: 15
      minimum-idle: 5
      idle-timeout: 30000
      connection-timeout: 30000
  main:
    allow-bean-definition-overriding: true

  jpa:
    database: POSTGRESQL
    open-in-view: false
    generate-ddl: false
    database-platform: org.hibernate.dialect.PostgreSQLDialect
    show-sql: false
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        format_sql: false
        dialect: org.hibernate.dialect.PostgreSQLDialect
        default_schema: ${DB_SCHEMA}
        jdbc.batch_size: 30
        order_inserts: true
        order_updates: true

  kafka:
    bootstrap-servers:
      - ${KAFKA_HOST}:${KAFKA_PORT}
    template:
      default-topic: NOTIFICATION_TOPIC
    producer:
      group-id: topicgroupid
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
      properties:
        max.request.size: 1300000000
        acks: all
        retries: 3
    properties:
      spring:
        json:
          type:
            mapping: Notification:io.digiservices.userservice.domain.Notification
      message.max.bytes: 1300000000
      max.request.size: 1300000000

eureka:
  instance:
    prefer-ip-address: true
    hostname: ecreditservice
    ip-address: ${SERVER_IP}
  client:
    service-url:
      defaultZone: http://admin:${EUREKA_PASSWORD}@${EUREKA_HOST}:${EUREKA_PORT}/eureka/

ui:
  app:
    url: ${UI_URL}

key:
  private: keys/private.key
  public: keys/public.key

server:
  port: ${SERVER_PORT}

jwks:
  uri: http://${AUTH_SERVER_HOST}:${AUTH_SERVER_PORT}/oauth2/jwks
EOF

echo -e "${GREEN}All service configuration files created successfully!${NC}"

# Configure firewall
echo -e "${YELLOW}Configuring firewall...${NC}"
sudo ufw allow 22/tcp
sudo ufw allow 2181/tcp  # Zookeeper
sudo ufw allow 5002/tcp  # Discovery Server
sudo ufw allow 5432/tcp  # PostgreSQL
sudo ufw allow 6000/tcp  # PgAdmin
sudo ufw allow 7000/tcp  # PgAdmin mapped port
sudo ufw allow 8000/tcp  # Gateway
sudo ufw allow 8080/tcp  # Authorization Server
#sudo ufw allow 8081/tcp  # EBanking
sudo ufw allow 8085/tcp  # User Service
sudo ufw allow 8086/tcp  # Notification Service
sudo ufw allow 8087/tcp  # ECredit Service
sudo ufw allow 9092/tcp  # Kafka

# Enable firewall if not already enabled
sudo ufw --force enable

echo -e "${GREEN}Firewall configured successfully!${NC}"

# Start containers
echo -e "${YELLOW}Starting containers...${NC}"
docker-compose -f docker-compose-prod.yml up -d

# Check if containers are running
echo -e "${YELLOW}Checking if containers are running...${NC}"
sleep 10
docker ps

echo -e "${GREEN}Deployment completed successfully!${NC}"
echo -e "${GREEN}Access your services at http://51.91.254.218:8000${NC}"

# Print URLs for accessing services
echo -e "\n${YELLOW}Service URLs:${NC}"
echo -e "${GREEN}Gateway:${NC} http://51.91.254.218:8000"
echo -e "${GREEN}Discovery Server:${NC} http://51.91.254.218:5002"
echo -e "${GREEN}PgAdmin:${NC} http://51.91.254.218:7000"
echo -e "${GREEN}Authorization Server:${NC} http://51.91.254.218:8080"
echo -e "${GREEN}User Service:${NC} http://51.91.254.218:8085"
#echo -e "${GREEN}EBanking:${NC} http://51.91.254.218:8081"
echo -e "${GREEN}Notification Service:${NC} http://51.91.254.218:8086"
echo -e "${GREEN}ECredit Service:${NC} http://51.91.254.218:8087"
