package in.makelabs.tasklist.toolkit.rest;
import java.util.HashMap;
import java.util.List;

import io.camunda.zeebe.client.api.ZeebeFuture;
import io.camunda.zeebe.client.api.response.Topology;
import io.camunda.zeebe.client.api.search.filter.UserTaskFilter;
import io.camunda.zeebe.client.api.search.response.SearchQueryResponse;
import io.camunda.zeebe.client.api.search.response.UserTask;
import io.camunda.zeebe.client.protocol.rest.UserTaskVariableFilterRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.command.TopologyRequestStep1;


@RestController
@RequestMapping("/ml-api")
public class ToolkitRestController {

    @Autowired
    private ZeebeClient client;


    private String recipientDemoEmail;

    @GetMapping("/fetchTopology")
    public ResponseEntity<Topology> fetchTopology(ServerWebExchange exchange) {

        return ResponseEntity
                .accepted()
                .body(fetchTopology())
                ;
    }

    @GetMapping("/searchTasks")
    public ResponseEntity<SearchQueryResponse<UserTask>> searchTasks(ServerWebExchange exchange) {


        ZeebeFuture<SearchQueryResponse<UserTask>> zf = client.newUserTaskQuery().send();

        return ResponseEntity
                .accepted()
                .body(zf.join());
    }

    public Topology fetchTopology() {

        io.camunda.zeebe.client.api.ZeebeFuture<Topology> zf =
        client.newTopologyRequest().send();
        return zf.join();
    }

}