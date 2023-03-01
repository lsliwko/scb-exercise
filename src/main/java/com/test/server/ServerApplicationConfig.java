package com.test.server;

import com.test.server.controller.EnrichController;
import com.test.server.domain.Product;
import com.test.server.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.constraint.UniqueHashCode;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvBeanReader;
import org.supercsv.io.ICsvBeanReader;
import org.supercsv.prefs.CsvPreference;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Configuration
public class ServerApplicationConfig {

    final static private Logger logger = LoggerFactory.getLogger(ServerApplicationConfig.class);

    @Value("classpath:product.csv")
    private Resource productCsvResource;

    final static private CellProcessor[] PRODUCT_PROCESSORS = new CellProcessor[] {
            new NotNull(), // productId
            new NotNull() // productName
    };

    @ConditionalOnMissingBean
    @Bean
    ProductRepository productRepository() throws Exception {
        return new ProductRepository() {
            private Map<String, Product> productsMap = loadProductMapFromResourceCsv(productCsvResource);

            @Override
            public Optional<Product> findById(String productId) {
                return Optional.ofNullable(productsMap.get(productId));
            }
        };
    };

    private static Map<String, Product> loadProductMapFromResourceCsv(Resource csvResource) throws IOException {
        Map<String, Product> productsMap  = new HashMap<>();

        try (
                ICsvBeanReader beanReader = new CsvBeanReader(new FileReader(csvResource.getFile()), CsvPreference.STANDARD_PREFERENCE);
        ) {

            final String[] header = beanReader.getHeader(true);

            Product product;
            while( (product = beanReader.read(Product.class, header, PRODUCT_PROCESSORS)) != null ) {
                productsMap.put(product.getProductId(), product);
            }
        }

        logger.info(String.format("Loaded %d Product objects from %s", productsMap.size(), csvResource.getFilename()));

        return productsMap;
    }




}
