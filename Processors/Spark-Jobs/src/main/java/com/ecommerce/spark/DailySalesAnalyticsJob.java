package com.ecommerce.spark;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import static org.apache.spark.sql.functions.*;

public class DailySalesAnalyticsJob {
    public static void main(String[] args) {
        SparkSession spark = SparkSession
                .builder()
                .appName("Daily Sales Analytics Job")
                .master("local[*]")
                .getOrCreate();

        try {
            String dataPath = ConfigLoader.get("data.orders.path", "src/main/resources/mock_orders.json");
            Dataset<Row> df = spark.read().json(dataPath);

            Dataset<Row> dailySales = df
                    .withColumn("date", to_date(col("createdAt")))
                    .groupBy("date")
                    .agg(
                            sum("totalAmount").alias("daily_revenue"),
                            count("orderId").alias("orders_count")
                    )
                    .orderBy(desc("date"));

            dailySales.show();
            System.out.println("Successfully processed Daily Sales Analytics.");

        } catch (Exception e) {
            System.err.println("Error processing Spark Job: " + e.getMessage());
        } finally {
            spark.stop();
        }
    }
}
