package org.ironrhino.core.search;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.apache.lucene.store.FSDirectory;
import org.ironrhino.core.util.AppInfo;
import org.springframework.scheduling.annotation.Scheduled;

public class SearchStat {

	public static final String INDEX_DIRECTORY = "/searchstat";

	public static final long TIME_INTERVAL = 3600;

	public static final int MAX_LIMIT = 10;

	private Logger log = LoggerFactory.getLogger(getClass());

	private Directory directory;

	private Analyzer analyzer = new SimpleAnalyzer();

	private Map<String, Integer> map = new HashMap<String, Integer>(1000);

	@PostConstruct
	public void afterPropertiesSet() {
		try {
			File dir = new File(AppInfo.getAppHome() + INDEX_DIRECTORY);
			if (!dir.exists() && dir.mkdirs())
				log.error("mkdir error:" + dir.getAbsolutePath());
			this.directory = FSDirectory.getDirectory(dir);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public void put(String keyword, int hits) {
		if (StringUtils.isBlank(keyword) || hits == 0)
			return;
		keyword = keyword.toLowerCase();
		map.put(keyword, hits);
	}

	@SuppressWarnings("unchecked")
	public Map<String, Integer> suggest(String keyword, int limit) {
		if (directory == null || StringUtils.isBlank(keyword))
			return Collections.EMPTY_MAP;
		if (limit > MAX_LIMIT)
			limit = MAX_LIMIT;
		try {
			IndexSearcher searcher = new IndexSearcher(directory);
			PrefixQuery query = new PrefixQuery(new Term("keyword", keyword));
			ScoreDoc[] hits = searcher.search(query, null, limit, new Sort(
					new SortField("hits", SortField.INT, true))).scoreDocs;
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

	@Scheduled(fixedDelay = 3600000)
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

}
