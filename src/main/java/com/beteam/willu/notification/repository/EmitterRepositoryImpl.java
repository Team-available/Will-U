package com.beteam.willu.notification.repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import lombok.NoArgsConstructor;

@Repository
@NoArgsConstructor
public class EmitterRepositoryImpl implements EmitterRepository {
	private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();
	private final Map<String, Object> eventCache = new ConcurrentHashMap<>();

	@Override
	public SseEmitter save(String emitterId, SseEmitter sseEmitter) {
		emitters.put(emitterId, sseEmitter);
		return sseEmitter;
	}

	@Override
	public void saveEventCache(String eventCacheId, Object event) {
		eventCache.put(eventCacheId, event);
	}

	@Override
	public Map<String, SseEmitter> findAllEmitterStartWithById(String userId) {
		return emitters.entrySet().stream()
			.filter(entry -> entry.getKey().startsWith(userId))
			.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	@Override
	public Map<String, Object> findAllEventCacheStartWithById(String userId) {
		return eventCache.entrySet().stream()
			.filter(entry -> entry.getKey().startsWith(userId))
			.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	@Override
	public void deleteById(String id) {
		emitters.remove(id);
	}

	@Override
	public void deleteAllEmitterStartWithId(String userId) {
		emitters.forEach(
			(key, emitter) -> {
				if (key.startsWith(userId)) {
					emitters.remove(key);
				}
			}
		);
	}

	@Override
	public void deleteAllEventCacheStartWithId(String userId) {
		eventCache.forEach(
			(key, event) -> {
				if (key.startsWith(userId)) {
					eventCache.remove(key);
				}
			}
		);
	}

	@Override
	public void deleteAllEventCacheEndsWithNotificationId(Long notificationId) {
		eventCache.forEach(
			(key, event) -> {
				if (key.endsWith(String.valueOf(notificationId))) {
					eventCache.remove(key);
				}
			}
		);
	}
}
