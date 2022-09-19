package vn.youmed.api.worker;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.core.shareddata.SharedData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vn.youmed.api.apis.studentApi.dto.DTOMessage.StudentMessage;
import vn.youmed.api.apis.studentApi.dto.StudentDTO;
import vn.youmed.api.apis.studentApi.pattern.Facade;
import vn.youmed.api.apis.studentApi.services.StudentService;

import java.util.ArrayList;
import java.util.List;

public class StudentVerticle extends AbstractVerticle {
    private static final Logger LOGGER = LoggerFactory.getLogger(StudentVerticle.class);
    private static int Numss = 40;

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        JsonObject msg = new JsonObject()
                .put("workerName", System.getProperties().getProperty("address"))
                .put("numberRequest", System.getProperties().getProperty("numberRequest"));
        LOGGER.info("worker send information {}", msg);
        vertx.eventBus().send("gateway", msg);
        getRequestFromEventBus(msg);
    }

    public void getRequestFromEventBus(JsonObject jsonObject) {
//        moi lan comsumer se tao ra mot thread de chay

        vertx.eventBus().consumer(System.getProperties().getProperty("address"), msg -> {
            List<JsonObject> studentDTOList = new ArrayList<>();
            StudentMessage studentMessage = JsonObject.mapFrom(msg.body()).mapTo(StudentMessage.class);
            LOGGER.info("student Mess {}", studentMessage);
            Facade facade = new Facade(vertx);
            StudentService service = facade.getStudentService();
            if (studentMessage.getPath().equals("students") && studentMessage.getMethodType().equals(HttpMethod.GET)) {
                service.getAll().setHandler(ar -> {
                    if (ar.succeeded()) {
                        for (StudentDTO dto : ar.result()) {
                            JsonObject jsonObject1 = JsonObject.mapFrom(dto);
                            studentDTOList.add(jsonObject1);
                        }
                        LOGGER.info("studentList {}", studentDTOList);
                        msg.reply(Json.encodePrettily(studentDTOList));
                    } else {
                        LOGGER.error("error {}", ar.cause());
                        msg.reply(Json.encodePrettily(ar.cause()));
                    }
                });
            }
        });
    }

    public final int divideRequestEachWorker(int numb) {
//        formula: (total * value) / 100
        int total = 1000;
        int result = (total * numb) / 100;
        return result;
    }


}
