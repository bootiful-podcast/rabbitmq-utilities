package fm.bootifulpodcast.rabbitmq;

import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.util.HashMap;

@Log4j2
public class RabbitMqEnvironmentPostProcessor implements EnvironmentPostProcessor {

	private String replacePeriodsWithUnderscores(String inputString) {
		var sb = new StringBuilder();
		for (var i = 0; i < inputString.length(); i++) {
			if (inputString.charAt(i) == '.') {
				sb.append('_');
			}
			else {
				sb.append(inputString);
			}
		}
		return sb.toString();
	}

	@Override
	public void postProcessEnvironment(ConfigurableEnvironment env,
			SpringApplication application) {

		var configurationKey = "podcast.rmq.address";
		var envVariable = replacePeriodsWithUnderscores(configurationKey).toUpperCase();
		var resolvedEnvironmentProperty = System.getenv(envVariable);
		var resolvedConfigurationProperty = env.getProperty(configurationKey);
		var rmqAddress = StringUtils.hasText(resolvedEnvironmentProperty)
				? resolvedEnvironmentProperty
				: (StringUtils.hasText(resolvedConfigurationProperty)
						? resolvedConfigurationProperty : null);

		log.debug("PODCAST_RMQ_ADDRESS: " + rmqAddress
				+ ". Detected RMQ_ADDRESS environment variable");

		if (!StringUtils.hasText(rmqAddress)) {
			return;
		}
		var uri = URI.create(rmqAddress);
		var userInfo = uri.getUserInfo();
		var vhost = uri.getPath();
		var host = uri.getHost();
		var port = uri.getPort();
		var user = (String) null;
		var pw = (String) null;
		if (StringUtils.hasText(userInfo) && userInfo.contains(":")) {
			var parts = userInfo.split(":");
			user = parts[0];
			pw = parts[1];
		}

		var map = new HashMap<String, Object>();
		map.put("host", host);

		if (port != -1) {
			map.put("port", port);
		}

		if (StringUtils.hasText(vhost)) {
			vhost = vhost.trim();
			if (!vhost.equalsIgnoreCase("/")) {
				if (vhost.startsWith("/")) {
					vhost = vhost.substring(1);
				}
				map.put("virtual-host", vhost);
			}
		}

		if (StringUtils.hasText(pw)) {
			map.put("password", pw);
		}

		if (StringUtils.hasText(user)) {
			map.put("username", user);
		}

		var newMap = new HashMap<String, Object>();
		map.forEach((p, k) -> newMap.put("spring.rabbitmq." + p, k));

		var propertySource = new PropertySource<String>("rmq-environment") {

			@Override
			public Object getProperty(String name) {
				if (newMap.containsKey(name))
					return newMap.get(name);
				return null;
			}
		};

		env.getPropertySources().addLast(propertySource);
	}

}
