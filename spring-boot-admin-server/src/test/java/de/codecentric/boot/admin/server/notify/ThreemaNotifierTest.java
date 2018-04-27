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

    @InjectMocks
    private ThreemaNotifier threemaNotifier;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private InstanceRepository instanceRepository;

    private static final URI THREEMA_API_GATEWAY_URI = URI.create("https://msgapi.threema.ch/send_simple");

    private final Instance instance = Instance.create(InstanceId.of("-id-"))
            .register(Registration.create("Threema", "http://health").build());

    @Before
    public void setup() {
        threemaNotifier.setSenderID("*SENDERID");
        threemaNotifier.setApiSecret("mySecret");
        threemaNotifier.setRecipientID("SomeOne");
    }

    @Test
    public void givenStatusChangedEventExpectNotificationToThreemaApi() {
        InstanceStatusChangedEvent event = new InstanceStatusChangedEvent(instance.getId(), instance.getVersion(), StatusInfo.ofUp());
        threemaNotifier.notifyThreema(event, instance);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map= new LinkedMultiValueMap<>();
        map.add("from", "*SENDERID");
        map.add("text", "Threema -id- is UP.");
        map.add("to", "SomeOne");
        map.add("secret", "mySecret");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        verify(restTemplate).postForEntity(THREEMA_API_GATEWAY_URI, request, String.class);
    }
}
