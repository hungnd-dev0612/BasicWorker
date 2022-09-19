package vn.youmed.api.worker;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StudentApp {
    public static Logger LOGGER = LoggerFactory.getLogger(StudentApp.class);

    public static void main(String[] args) {
        ClusterManager mgr = new HazelcastClusterManager();
        VertxOptions options = new VertxOptions().setClusterManager(mgr);
        Vertx.clusteredVertx(options, cluster -> {
            if (cluster.succeeded()) {
                cluster.result().deployVerticle(new StudentVerticle(), res -> {
                    if (res.succeeded()) {
                        LOGGER.info("deployment id is : {}", res.result());
                        LOGGER.warn("you can use this : {} id to undeploy verticle", res.result());
                    } else {
                        LOGGER.error("deployment failed : ", res.cause());
                    }
                });
            } else {
                LOGGER.error("Cluster fail: {}", cluster.cause());
            }

        });
        LOGGER.info(System.getProperties().getProperty("address"));
        LOGGER.info(System.getProperties().getProperty("numberRequest"));

//        System.out.println(args[2]);
//        System.out.println(args[3]);
//        System.out.println(Arrays.asList());
    }
}
