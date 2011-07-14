/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
 * Copyright (c) Ericsson AB, 2004-2008. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package org.ironrhino.core.util;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * http://fisheye5.cenqua.com/browse/~raw,r=1.11/sailfin/clb/src/main/java/org/
 * jvnet/glassfish/comms/clb/core/util/ConsistentHash.java
 */
public class ConsistentHash<K, V> {
	private Map<V, Integer> nodes = new HashMap<V, Integer>();
	private int totalWeights;
	private Point<V>[] points;
	private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock(true);
	private ReentrantReadWriteLock.ReadLock readLock = lock.readLock();
	private ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();
	private Hash hash;
	private int pointsPerWeight = 100;

	public ConsistentHash(Map<V, Integer> nodes) {
		this(nodes, null);
	}

	public ConsistentHash(Map<V, Integer> nodes, Hash hash) {
		if (nodes != null)
			this.nodes = nodes;
		if (hash == null)
			hash = new MurmurHash();
		this.hash = hash;
		setup();
	}

	public ConsistentHash(Collection<V> nodes) {
		this(nodes, null);
	}

	public ConsistentHash(Collection<V> nodes, Hash hash) {
		if (nodes != null && nodes.size() > 0) {
			for (V v : nodes)
				this.nodes.put(v, 1);
		}
		if (hash == null)
			hash = new MurmurHash();
		this.hash = hash;
		setup();
	}

	public void addNode(V node, int weight) {
		if (weight < 1)
			return;
		writeLock.lock();
		try {
			nodes.put(node, weight);
			points = null;
			setup();
		} finally {
			writeLock.unlock();
		}
	}

	public void addNode(V node) {
		addNode(node, 1);
	}

	public void removeNode(V node) {
		writeLock.lock();
		try {
			nodes.remove(node);
			points = null;
			setup();
		} finally {
			writeLock.unlock();
		}
	}

	private void setup() {
		writeLock.lock();
		try {
			if (points != null)
				return;
			totalWeights = 0;
			for (Iterator<Integer> it = nodes.values().iterator(); it.hasNext();)
				totalWeights += (it.next()).intValue();
			totalWeights *= pointsPerWeight;
			points = new Point[totalWeights];
			int point = 0;
			for (Iterator<V> keys = nodes.keySet().iterator(); keys.hasNext();) {
				Object node = keys.next();
				int n = nodes.get(node).intValue() * pointsPerWeight;
				for (int i = 0; i < n; i++)
					points[point++] = new Point(hash.hash(node.toString() + "/"
							+ i), node);
			}
			Arrays.sort(points);
		} finally {
			writeLock.unlock();
		}
	}

	public V get(K key) {
		if (key == null) {
			return null;
		}
		String tobeHash = key.toString();
		int i = tobeHash.indexOf('{');
		int j = tobeHash.indexOf('}');
		if (i > -1 && j > i)
			tobeHash = tobeHash.substring(i + 1, j);
		readLock.lock();
		try {
			long hashValue = hash.hash(tobeHash);
			if (points == null || points.length == 0)
				return null;
			if (hashValue < points[0].start)
				return points[totalWeights - 1].node;
			int lo = 0;
			int hi = totalWeights;

			while (lo < (hi - 1)) {
				int mid = (lo + hi) >>> 1;
				if (points[mid].start > hashValue)
					hi = mid;
				else
					lo = mid;
			}
			return points[lo].node;
		} finally {
			readLock.unlock();
		}
	}

	public static interface Hash {
		public long hash(String s);
	}

	/**
	 * This is a very fast, non-cryptographic hash suitable for general
	 * hash-based lookup. See http://murmurhash.googlepages.com/ for more
	 * details.
	 * <p/>
	 * <p>
	 * The C version of MurmurHash 2.0 found at that site was ported to Java by
	 * Andrzej Bialecki (ab at getopt org).
	 * </p>
	 */
	public static class MurmurHash implements Hash {

		public static int hash(byte[] data, int seed) {
			return hash(ByteBuffer.wrap(data), seed);
		}

		public static int hash(byte[] data, int offset, int length, int seed) {
			return hash(ByteBuffer.wrap(data, offset, length), seed);
		}

		public static int hash(ByteBuffer buf, int seed) {
			ByteOrder byteOrder = buf.order();
			buf.order(ByteOrder.LITTLE_ENDIAN);

			int m = 0x5bd1e995;
			int r = 24;

			int h = seed ^ buf.remaining();

			int k;
			while (buf.remaining() >= 4) {
				k = buf.getInt();

				k *= m;
				k ^= k >>> r;
				k *= m;

				h *= m;
				h ^= k;
			}

			if (buf.remaining() > 0) {
				ByteBuffer finish = ByteBuffer.allocate(4).order(
						ByteOrder.LITTLE_ENDIAN);
				// for big-endian version, use this first:
				// finish.position(4-buf.remaining());
				finish.put(buf).rewind();
				h ^= finish.getInt();
				h *= m;
			}

			h ^= h >>> 13;
			h *= m;
			h ^= h >>> 15;

			buf.order(byteOrder);
			return h;
		}

		public static long hash64A(byte[] data, int seed) {
			return hash64A(ByteBuffer.wrap(data), seed);
		}

		public static long hash64A(byte[] data, int offset, int length, int seed) {
			return hash64A(ByteBuffer.wrap(data, offset, length), seed);
		}

		public static long hash64A(ByteBuffer buf, int seed) {
			ByteOrder byteOrder = buf.order();
			buf.order(ByteOrder.LITTLE_ENDIAN);

			long m = 0xc6a4a7935bd1e995L;
			int r = 47;

			long h = seed ^ (buf.remaining() * m);

			long k;
			while (buf.remaining() >= 8) {
				k = buf.getLong();

				k *= m;
				k ^= k >>> r;
				k *= m;

				h ^= k;
				h *= m;
			}

			if (buf.remaining() > 0) {
				ByteBuffer finish = ByteBuffer.allocate(8).order(
						ByteOrder.LITTLE_ENDIAN);
				// for big-endian version, do this first:
				// finish.position(8-buf.remaining());
				finish.put(buf).rewind();
				h ^= finish.getLong();
				h *= m;
			}

			h ^= h >>> r;
			h *= m;
			h ^= h >>> r;

			buf.order(byteOrder);
			return h;
		}

		public long hash(byte[] key) {
			return hash64A(key, 0x1234ABCD);
		}

		public long hash(String key) {
			try {
				return hash(key.getBytes("UTF-8"));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				return 0;
			}
		}
	}

	private static class Point<V> implements Comparable<Point<V>> {

		Long start;
		V node;

		Point(long start, V node) {
			this.start = start;
			this.node = node;
		}

		public int compareTo(Point<V> o) {
			return start.compareTo(o.start);
		}

		@Override
		public boolean equals(Object o) {
			return o instanceof Point && o.toString().equals(this.toString());
		}

		@Override
		public int hashCode() {
			return start.hashCode();
		}

		@Override
		public String toString() {
			return "{" + node + "," + start + "}";
		}
	}

}