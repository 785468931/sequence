package cn.com.sequence.service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Component;

@Component
public class LockSquence {
	AtomicLong atomic = new AtomicLong();
	ConcurrentMap<String, Long> currentMaxId = new ConcurrentHashMap<>();

	public AtomicLong getAtomic() {
		return atomic;
	}

	public void setAtomic(AtomicLong atomic) {
		this.atomic = atomic;
	}

	public ConcurrentMap<String, Long> getCurrentMaxId() {
		return currentMaxId;
	}

	public void setCurrentMaxId(ConcurrentMap<String, Long> currentMaxId) {
		this.currentMaxId = currentMaxId;
	}

}
