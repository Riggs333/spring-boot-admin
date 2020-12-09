/*
 * Copyright 2014-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.Mono;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.net.URI;
import java.util.Objects;

@Validated
public class ThreemaNotifier extends AbstractStatusChangeNotifier {

    private static final URI THREEMA_API_GATEWAY = URI.create("https://msgapi.threema.ch/send_simple");

    private final RestTemplate restTemplate;//TODO

    @NotBlank
    @NotNull
    private String senderID;

    @NotBlank
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
