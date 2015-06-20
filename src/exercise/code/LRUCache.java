package exercise.code;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class LRUCache<K, V> {
	
	private static class Node<K, T> {
		private T item;
		private Node<K, T> previous;
		private Node<K, T> next;
		private K key;
		
		public Node(K key, T item) {
			this.item = item;
			this.key = key;
		}
		
		public Node() {
			
		}
	}
	
	private Map<K, Node<K, V>> map = new ConcurrentHashMap<K, Node<K, V>>();
	private final int capacity;
	private Node<K, V> head;
	private Node<K, V> tail;
	private int count;
	private ReentrantLock lock;
	
	public LRUCache(int capacity) {
		this.capacity = capacity;
		this.count = 0;
		head = new Node<K, V>();
		tail = head;
	}
	
	public V get(K key) {
		Node<K, V> node = map.get(key);
		if (node != null) {
			V ret = node.item;
			if (node != tail) {
				lock.lock();
				node.previous.next = node.next;
				node.next.previous = node.previous;
				node.previous = tail;
				tail.next = node;
				node.next = null;
				tail = tail.next;
			}
			return ret;
		}
		return null;
	}
	
	public void set(K key, V value) {
		Node<K, V> node = map.get(key);
		if (node != null) {
			node.item = value;
			if (node != tail) {
				node.previous.next = node.next;
				node.next.previous = node.previous;
				tail.next = node;
				node.previous = tail;
				node.next = null;
				tail = tail.next;
			}
		} else {
			Node<K, V> toAdd = new Node<K, V>(key, value);
			map.put(key,  toAdd);
			tail.next = toAdd;
			toAdd.previous = tail;
			tail = tail.next;
			count++;
		}
		
		if (count > capacity) {
			map.remove(head.next.key);
			head.next = head.next.next;
			head.next.previous = head;
			count--;
		}
	}
}
