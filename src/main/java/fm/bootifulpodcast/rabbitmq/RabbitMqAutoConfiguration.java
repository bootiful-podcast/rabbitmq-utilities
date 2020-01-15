package fm.bootifulpodcast.rabbitmq;

import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "rabbitmq.utils", name = "enabled", havingValue = "true")
class RabbitMqAutoConfiguration {

  @Bean
  RabbitMqHelper rabbitMqHelper(AmqpAdmin amqpAdmin) {
    return new RabbitMqHelper(amqpAdmin);
  }

  @Bean
  RabbitMqEnvironmentPostProcessor rabbitMqEnvironmentPostProcessor() {
    return new RabbitMqEnvironmentPostProcessor();
  }
}
