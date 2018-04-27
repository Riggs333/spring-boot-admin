package de.codecentric.boot.admin.server.notify;

import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.entities.InstanceRepository;
import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceStatusChangedEvent;
import de.codecentric.boot.admin.server.domain.values.Registration;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Objects;

public class ThreemaNotifier extends AbstractStatusChangeNotifier {

    private static final URI THREEMA_API_GATEWAY = URI.create("https://msgapi.threema.ch/send_simple");

    private final RestTemplate restTemplate;

    //TODO @NotBlank ?
    private String senderID;

    //TODO @NotBlank?
    private String apiSecret;

    //TODO @NotBlank?
    private String recipientID;

    //TODO why not inject RestTemplate here? Why do other notifiers create the RestTemplate on its own?
    public ThreemaNotifier(InstanceRepository repository) {
        this(repository, new RestTemplate());
    }

    public ThreemaNotifier(InstanceRepository repository, RestTemplate restTemplate) {
        super(repository);
        this.restTemplate = Objects.requireNonNull(restTemplate, "Parameter restTemplate must not be null.");
    }

    @Override
    protected Mono<Void> doNotify(InstanceEvent event, Instance instance) {
        return Mono.fromRunnable(() -> notifyThreema((InstanceStatusChangedEvent) event, instance));
    }

    protected void notifyThreema(InstanceStatusChangedEvent event, Instance instance) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = createMessage(event, instance);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        //TODO Fehlerhandling?
        restTemplate.postForEntity(THREEMA_API_GATEWAY, request, String.class);
    }

    private MultiValueMap<String, String> createMessage(InstanceStatusChangedEvent event, Instance instance) {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("from", senderID);
        map.add("text", createText(event, instance));
        map.add("to", recipientID);
        map.add("secret", apiSecret);
        return map;
    }

    private String createText(InstanceStatusChangedEvent event, Instance instance) {
        Registration registration = instance.getRegistration();
        return registration.getName() + " " + instance.getId() + " is " + event.getStatusInfo()
                .getStatus() + ".";
    }

    public void setApiSecret(String apiSecret) {
        this.apiSecret = apiSecret;
    }

    public void setRecipientID(String recipientID) {
        this.recipientID = recipientID;
    }

    public void setSenderID(String senderID) {
        this.senderID = senderID;
    }
}
