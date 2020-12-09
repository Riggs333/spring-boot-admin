package de.codecentric.boot.admin.server.config;

import de.codecentric.boot.admin.server.domain.entities.InstanceRepository;
import de.codecentric.boot.admin.server.notify.ThreemaNotifier;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = AdminServerNotifierAutoConfiguration.ThreemaNotifierConfiguration.class)
@TestPropertySource("threema.properties")
public class ThreemaNotifierConfigurationTest {

    @Autowired
    ThreemaNotifier notifier;

    @MockBean
    InstanceRepository instanceRepository;

    @Test
    public void givenBlankSecretExpect() {

    }

}
