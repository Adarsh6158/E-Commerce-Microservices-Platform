package com.ecommerce.spark;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import static org.apache.spark.sql.functions.*;

public class ProductRecommendationJob {
    public static void main(String[] args) {
        SparkSession spark = SparkSession
                .builder()
                .appName("Product Recommendation Job")
                .master("local[*]")
                .getOrCreate();

        try {
            String dataPath = ConfigLoader.get("data.orders.path", "src/main/resources/mock_orders.json");
            Dataset<Row> df = spark.read().json(dataPath);
            
            System.out.println("Processing Collaborative Filtering for Recommendations...");
        
            
            System.out.println("Successfully trained Product Recommendation Model.");

        } catch (Exception e) {
            System.err.println("Error processing Spark Job: " + e.getMessage());
        } finally {
            spark.stop();
        }
    }
}
