package org.ironrhino.core.search;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;
import org.springframework.stereotype.Component;

@Component("searchStat")
public class SearchStat {

	public static final String SEARCH_STAT_THREAD_NAME = "SEARCH_STAT_THREAD_NAME";

	public static final long TIME_INTERVAL = 3600;

	public static final int MAX_RESULT = 5;

	private Log log = LogFactory.getLog(getClass());

	private final Lock timerLock = new ReentrantLock();

	private final Condition condition = timerLock.newCondition();

	private Thread thread;

	private Directory directory;

	private Analyzer analyzer = new SimpleAnalyzer();

	private Map<String, Integer> map = new HashMap<String, Integer>(1000);

	@PostConstruct
	public void afterPropertiesSet() {
		try {
			File dir = new File(System.getProperty("user.home") + "/searchstat");
			if (!dir.exists() && dir.mkdirs())
				log.error("mkdir error:" + dir.getAbsolutePath());
			this.directory = NIOFSDirectory.getDirectory(dir);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public void put(String keyword, int hits) {
		if (StringUtils.isBlank(keyword) || hits == 0)
			return;
		keyword = keyword.toLowerCase();
		map.put(keyword, hits);
		runThread();
	}

	public Map<String, Integer> suggest(String keyword) {
		if (directory == null || StringUtils.isBlank(keyword))
			return Collections.EMPTY_MAP;
		try {
			IndexSearcher searcher = new IndexSearcher(directory);
			PrefixQuery query = new PrefixQuery(new Term("keyword", keyword));
			ScoreDoc[] hits = searcher.search(query, null, MAX_RESULT,
					new Sort(new SortField("hits", SortField.INT, true))).scoreDocs;
			if (hits.length == 0)
				return Collections.EMPTY_MAP;
			Map<String, Integer> result = new LinkedHashMap<String, Integer>();
			for (int i = 0; i < hits.length; i++) {
				int docId = hits[i].doc;
				Document d = searcher.doc(docId);
				result.put(d.get("keyword"), Integer.valueOf(d.get("hits")));
			}
			searcher.close();
			return result;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return Collections.EMPTY_MAP;
	}

	public void index() {
		try {
			boolean create = true;
			if (directory.list().length > 0)
				create = false;
			IndexWriter iw = new IndexWriter(directory, analyzer, create,
					IndexWriter.MaxFieldLength.LIMITED);
			for (Map.Entry<String, Integer> entry : map.entrySet())
				iw.deleteDocuments(new TermQuery(new Term(entry.getKey())));
			iw.commit();
			for (Map.Entry<String, Integer> entry : map.entrySet()) {
				Document doc = new Document();
				doc.add(new Field("keyword", entry.getKey(), Store.YES,
						Index.NOT_ANALYZED));
				doc.add(new Field("hits", String.valueOf(entry.getValue()),
						Store.YES, Index.NO));
				iw.addDocument(doc);
			}
			iw.close();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	private void runThread() {
		if (directory == null)
			return;
		if (thread != null) {
			if (thread.isAlive())
				return;
			try {
				thread.interrupt();
			} catch (Exception e) {
				log.error("interrupt write thread error", e);
			}
		}
		thread = new Thread(new Runnable() {
			public void run() {
				while (true) {
					try {
						timerLock.lock();
						if (condition.await(TIME_INTERVAL, TimeUnit.SECONDS))
							log.debug("await returns true");
					} catch (Exception e) {
						log.error("wait error", e);
					} finally {
						timerLock.unlock();
					}
					index();
				}
			}

		}, SEARCH_STAT_THREAD_NAME);
		thread.start();
	}

}
