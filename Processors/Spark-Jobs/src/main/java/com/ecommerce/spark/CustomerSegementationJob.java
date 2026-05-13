package com.ecommerce.spark;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import static org.apache.spark.sql.functions.*;

public class CustomerSegementationJob {
    public static void main(String[] args) {
        SparkSession spark = SparkSession
                .builder()
                .appName("Customer Segmentation Job")
                .master("local[*]")
                .getOrCreate();

        try {
            String dataPath = ConfigLoader.get("data.orders.path", "src/main/resources/mock_orders.json");
            Dataset<Row> df = spark.read().json(dataPath);

            Dataset<Row> segmentedCustomers = df.groupBy("userId")
                    .agg(
                            sum("totalAmount").alias("lifetime_value"),
                            count("orderId").alias("total_orders")
                    )
                    .withColumn("segment", 
                            when(col("lifetime_value").gt(1000), "VIP")
                            .when(col("lifetime_value").gt(200), "Regular")
                            .otherwise("Occasional")
                    );

            segmentedCustomers.show();
            System.out.println("Successfully processed Customer Segmentation.");
            
        } catch (Exception e) {
            System.err.println("Error processing Spark Job: " + e.getMessage());
        } finally {
            spark.stop();
        }
    }
}
