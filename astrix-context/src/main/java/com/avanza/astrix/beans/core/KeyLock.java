/*
 * Copyright 2014-2015 Avanza Bank AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.avanza.astrix.beans.core;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;

public final class KeyLock<K> {
	
	private final ConcurrentMap<Integer, ReentrantLock> lockedObjects = new ConcurrentHashMap<>();
	private int size = 17;
	
	/**
	 * Locks the given key. 
	 * 
	 * @param key
	 */
	public void lock(K key) {
		ReentrantLock lock = getLock(key);
		lock.lock();
	}

	/**
	 * Unlocks the given key
	 * @param key
	 */
	public void unlock(K key) {
		ReentrantLock lock = getLock(key);
		if (!lock.isHeldByCurrentThread()) {
			throw new IllegalStateException("Cannot release lock not held by current thread: " + key);
		}
		lock.unlock();
	}

	private ReentrantLock getLock(K key) {
		ReentrantLock lock = new ReentrantLock();
		int lockId = key.hashCode() % size;
		ReentrantLock existingLock = lockedObjects.putIfAbsent(lockId, lock);
		if (existingLock != null) {
			// Another thread is already created lock
			lock = existingLock;
		}
		return lock;
	}
}
