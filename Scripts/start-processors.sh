#!/usr/bin/env bash

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT_DIR"

GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[0;33m'
RED='\033[0;31m'
NC='\033[0m'
BOLD='\033[1m'

show_help() {
  echo -e "${BOLD}Usage:${NC} ./Scripts/start-processors.sh [kafka|spark] [ProcessorClassName]"
  echo ""
  echo "Available Kafka Processors:"
  echo "  - FraudDetectionConsumer"
  echo "  - InventoryAlertConsumer"
  echo "  - OrderEnrichmentProcessor"
  echo "  - SalesMetricsAggregator"
  echo "  - UserActivityStreamProcessor"
  echo ""
  echo "Available Spark Jobs:"
  echo "  - CustomerSegementationJob"
  echo "  - DailySalesAnalyticsJob"
  echo "  - ProductRecommendationJob"
  echo ""
  echo "Examples:"
  echo "  ./Scripts/start-processors.sh kafka FraudDetectionConsumer"
  echo "  ./Scripts/start-processors.sh spark CustomerSegementationJob"
}

if [ "$#" -lt 2 ]; then
  show_help
  exit 1
fi

TYPE=$1
CLASS_NAME=$2

# Required JVM arguments for Spark to run on Java 17+
SPARK_JVM_ARGS="--add-opens=java.base/java.lang=ALL-UNNAMED \
--add-opens=java.base/java.lang.invoke=ALL-UNNAMED \
--add-opens=java.base/java.lang.reflect=ALL-UNNAMED \
--add-opens=java.base/java.io=ALL-UNNAMED \
--add-opens=java.base/java.net=ALL-UNNAMED \
--add-opens=java.base/java.nio=ALL-UNNAMED \
--add-opens=java.base/java.util=ALL-UNNAMED \
--add-opens=java.base/java.util.concurrent=ALL-UNNAMED \
--add-opens=java.base/java.util.concurrent.atomic=ALL-UNNAMED \
--add-opens=java.base/sun.nio.ch=ALL-UNNAMED \
--add-opens=java.base/sun.nio.cs=ALL-UNNAMED \
--add-opens=java.base/sun.security.action=ALL-UNNAMED \
--add-opens=java.base/sun.util.calendar=ALL-UNNAMED"

if [ "$TYPE" = "kafka" ]; then
  echo -e "${BLUE}${BOLD}Starting Kafka Processor:${NC} $CLASS_NAME"
  cd Processors/Kafka-Processors
  
  # Find the correct package
  if grep -q "public class $CLASS_NAME" src/main/java/com/ecommerce/processors/consumer/*.java 2>/dev/null; then
    FULL_CLASS="com.ecommerce.processors.consumer.$CLASS_NAME"
  elif grep -q "public class $CLASS_NAME" src/main/java/com/ecommerce/processors/streams/*.java 2>/dev/null; then
    FULL_CLASS="com.ecommerce.processors.streams.$CLASS_NAME"
  else
    echo -e "${RED}Error: Could not find class $CLASS_NAME in Kafka Processors.${NC}"
    exit 1
  fi
  
  echo -e "${GREEN}Running $FULL_CLASS...${NC}"
  mvn compile exec:java -Dexec.mainClass="$FULL_CLASS"

elif [ "$TYPE" = "spark" ]; then
  echo -e "${BLUE}${BOLD}Starting Spark Job:${NC} $CLASS_NAME"
  cd Processors/Spark-Jobs
  
  FULL_CLASS="com.ecommerce.spark.$CLASS_NAME"
  
  # Inject Java 17 compatibility flags into Maven
  export MAVEN_OPTS="$SPARK_JVM_ARGS"
  
  echo -e "${GREEN}Running $FULL_CLASS with Java 17 compatibility flags...${NC}"
  # Using test-compile and test scope because Spark is marked as <scope>provided</scope>
  mvn test-compile exec:java -Dexec.mainClass="$FULL_CLASS" -Dexec.classpathScope="test"

else
  echo -e "${RED}Unknown type: $TYPE${NC}"
  show_help
  exit 1
fi
