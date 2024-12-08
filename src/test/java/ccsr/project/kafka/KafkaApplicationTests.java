package ccsr.project.kafka;

import ccsr.project.kafka.Models.Consumer;
import ccsr.project.kafka.Models.Publisher;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadFactory;

@SpringBootTest
class KafkaApplicationTests {

	@Test
	void contextLoads() {

	}

	@Test
	void TestMultiToOne(){

		Runnable runnable = new Runnable() {

			@Override
			public void run() {
                try {
                    Consumer.searchTopicsByInterest("sport");
                } catch (ExecutionException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
		};

		ThreadFactory newThreadFactory = new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				return new Thread(r);
			}
		};

		for (int i = 0; i < 1000; i++) {
			newThreadFactory.newThread(runnable);

		}

	}

}
