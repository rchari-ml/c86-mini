package in.makelabs.tasklist.toolkit.lead_record;
import java.time.Duration;
import java.util.Map;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.camunda.zeebe.spring.client.annotation.JobWorker;
import io.camunda.zeebe.spring.client.annotation.Variable;

@Component
public class NewLeadRecordWorker {

    private final static Logger LOG = LoggerFactory.getLogger(NewLeadRecordWorker.class);

    @JobWorker(type = "new-lead-record", autoComplete = false)
    public Map<String, String> handle(
            final JobClient client, final ActivatedJob job,
            @Variable(name = "companyName")         String companyName,
            @Variable(name = "companyPincode")      String companyPincode,
            @Variable(name = "companyContactPhone") String companyContactPhone,
            @Variable(name = "companyContactEmail") String companyContactEmail
    ) {
        LOG.info("company name: {} company pincode: {}", companyName, companyPincode);

        // i am simulating an error condition - although it is not so elegant
        if ("600001".equals(companyPincode)){

            if (job.getRetries() <= 1){
                // hmm - this is last retry. throw as bpmn error so it is handled in process definition
                client.newThrowErrorCommand(job)
                        .errorCode("CUSTOMER_PINCODE_ERROR")
                        .errorMessage("Pincode is not allowed. Policy violation error. Data: " + companyPincode)
                        .send()
                ;
                LOG.info("company name: {} company pincode: {} Sent error back to bpmn process", companyName, companyPincode);

                return Map.of("status", "throw");
            }
            else {
                // handle as fail condition and apply retry policy
                client.newFailCommand(job)
                        .retries(job.getRetries() - 1)
                        .retryBackoff(Duration.ofMinutes(1))
                        .errorMessage("Pincode is not allowed. Policy violation error. Data: " + companyPincode)
                        .send()
                ;
                LOG.info("company name: {} company pincode: {} Sent error back with retry policy", companyName, companyPincode);

                return Map.of("status", "retry");
            }

        }


        // doing some work - add a pause for 5 seconds
        try {
            Thread.currentThread().sleep(1000 * 5);
        } catch (InterruptedException e) {
            ; // just carry on with it
        }

        client.newCompleteCommand(job.getKey()).send();
        LOG.info("company name: {} company pincode: {} Return after complete command", companyName, companyPincode);

        return Map.of("status", "success");

    } // end of handler method
}
