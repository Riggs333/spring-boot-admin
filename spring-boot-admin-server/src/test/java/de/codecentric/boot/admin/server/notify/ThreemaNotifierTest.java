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
import de.codecentric.boot.admin.server.domain.events.InstanceStatusChangedEvent;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.domain.values.Registration;
import de.codecentric.boot.admin.server.domain.values.StatusInfo;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ThreemaNotifierTest {

    private static final URI THREEMA_API_GATEWAY_URI = URI.create("https://msgapi.threema.ch/send_simple");

    private final Instance instance = Instance.create(InstanceId.of("-id-"))
            .register(Registration.create("Threema", "http://health").build());

    @InjectMocks
    private ThreemaNotifier threemaNotifier;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private InstanceRepository instanceRepository;

    @Before
    public void setup() {
        threemaNotifier.setSenderID("*SENDERID");
        threemaNotifier.setApiSecret("mySecret");
        threemaNotifier.setRecipientID("SomeOne");
    }

    @Test
    public void givenStatusChangedEventExpectNotificationToThreemaApi() {
        InstanceStatusChangedEvent event =
                new InstanceStatusChangedEvent(instance.getId(), instance.getVersion(), StatusInfo.ofUp());
        threemaNotifier.notifyThreema(event, instance);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("from", "*SENDERID");
        map.add("text", "Threema -id- is UP.");
        map.add("to", "SomeOne");
        map.add("secret", "mySecret");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        verify(restTemplate).postForEntity(THREEMA_API_GATEWAY_URI, request, String.class);
    }
}
