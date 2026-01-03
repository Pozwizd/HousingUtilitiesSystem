package org.spacelab.housingutilitiessystemuser.config.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.spacelab.housingutilitiessystemuser.service.chat.ChatEventSubscriber;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;


@Configuration
public class RedisConfig {

    private static final String CHAT_EVENTS_CHANNEL = "chat_events";
    private static final String PRESENCE_EVENTS_CHANNEL = "presence_events";

    
    @Bean
    public ObjectMapper redisObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return objectMapper;
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory,
            ObjectMapper redisObjectMapper) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);

        
        
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer(redisObjectMapper);
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.afterPropertiesSet();
        return template;
    }

    
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory,
            ChatEventSubscriber chatEventSubscriber) {

        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);

        
        MessageListenerAdapter messageAdapter = new MessageListenerAdapter(chatEventSubscriber, "handleMessage");
        messageAdapter.setSerializer(new StringRedisSerializer());
        messageAdapter.afterPropertiesSet();
        container.addMessageListener(messageAdapter, new ChannelTopic(CHAT_EVENTS_CHANNEL));

        
        MessageListenerAdapter presenceAdapter = new MessageListenerAdapter(chatEventSubscriber, "handlePresence");
        presenceAdapter.setSerializer(new StringRedisSerializer());
        presenceAdapter.afterPropertiesSet();
        container.addMessageListener(presenceAdapter, new ChannelTopic(PRESENCE_EVENTS_CHANNEL));

        return container;
    }
}
