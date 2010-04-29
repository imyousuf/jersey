package com.sun.jersey.samples.hypermedia;

import com.sun.grizzly.http.SelectorThread;
import com.sun.jersey.api.container.grizzly.GrizzlyWebContainerFactory;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.core.UriBuilder;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.filter.LoggingFilter;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.samples.hypermedia.client.model.Address;

public class Main {

    public static final URI BASE_URI = UriBuilder.fromUri("http://localhost/").port(9998).build();

    public static SelectorThread startServer() throws IOException {
        final Map<String, String> initParams = new HashMap<String, String>();

        initParams.put("com.sun.jersey.config.property.packages",
                "com.sun.jersey.samples.hypermedia.server.controller");
        initParams.put("com.sun.jersey.config.feature.Formatted", "true");

        // Register HypermediaFilterFactory
        initParams.put(ResourceConfig.PROPERTY_RESOURCE_FILTER_FACTORIES,
                "com.sun.jersey.server.hypermedia.filter.HypermediaFilterFactory");

        System.out.println("Starting grizzly...");
        SelectorThread threadSelector = GrizzlyWebContainerFactory.create(BASE_URI, initParams);
        return threadSelector;
    }

    public static void main(String[] args) throws Exception {
        // Grizzly initialization
        SelectorThread threadSelector = startServer();

        try {
            // Create Client and configure it
            Client client = new Client();
            client.addFilter(new LoggingFilter());

            // Create order view
            OrderView orderView = client.view("http://localhost:9998/orders/1",
                    OrderView.class);

            // Approve order
            orderView = orderView.review("approve");

            // Activate customer in order 1 (which was suspended)
            CustomerView customerView = orderView.getCustomer();
            if (!customerView.isActive()) {
                customerView.activate();
            }

            // Pay order
            orderView = orderView.pay("123456789");

            // Ship order (returns new order)
            Address newAddress = new Address();
            newAddress.setCity("Springfield");
            newAddress.setStreet("Main");
            orderView.ship(newAddress);
        }
        finally {
            threadSelector.stopEndpoint();
        }
    }
}
