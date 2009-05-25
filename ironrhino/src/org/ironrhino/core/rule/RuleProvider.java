package org.ironrhino.core.rule;

import java.io.InputStreamReader;
import java.io.Reader;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.drools.FactHandle;
import org.drools.RuleBase;
import org.drools.RuleBaseFactory;
import org.drools.StatefulSession;
import org.drools.compiler.DrlParser;
import org.drools.compiler.PackageBuilder;
import org.drools.lang.descr.PackageDescr;
import org.springframework.core.io.Resource;

public class RuleProvider {

	private static final Log log = LogFactory.getLog(RuleProvider.class);

	private StatefulSession currentStatefulSession;

	private Resource dslFile;

	private RuleBase ruleBase;

	private Resource[] ruleFiles;

	public StatefulSession getStatefulSession(Object... assertObjects) {
		for (Object object : assertObjects) {
			insertOrUpdate(currentStatefulSession, object);
		}
		return currentStatefulSession;
	}

	public StatefulSession getNewStatefulSession(Object... assertObjects) {
		StatefulSession newStatefulSession = ruleBase.newStatefulSession();
		for (Object object : assertObjects) {
			insertOrUpdate(newStatefulSession, object);
		}
		return newStatefulSession;
	}

	public void insert(StatefulSession statefulSession, Object element) {
		statefulSession.asyncInsert(element);
	}

	public void insertOrUpdate(StatefulSession statefulSession, Object element) {
		if (element == null)
			return;
		FactHandle fact = statefulSession.getFactHandle(element);
		if (fact == null) {
			statefulSession.asyncInsert(element);
		} else {
			statefulSession.asyncUpdate(fact, element);
		}
	}

	public void retract(StatefulSession statefulSession, Object element) {
		if (element == null)
			return;
		FactHandle fact = statefulSession.getFactHandle(element);
		if (fact != null) {
			statefulSession.asyncRetract(fact);
		}
	}

	public synchronized void compileRuleBase() throws Exception {
		PackageBuilder builder = new PackageBuilder();
		ruleBase = RuleBaseFactory.newRuleBase();
		if (ruleFiles != null) {
			Reader dslReader = null;
			if (dslFile != null)
				dslReader = new InputStreamReader(dslFile.getInputStream(),
						"UTF-8");
			for (Resource ruleFile : ruleFiles) {
				PackageDescr packageDescr;
				Reader drlReader = new InputStreamReader(ruleFile
						.getInputStream(), "UTF-8");
				if (dslFile != null)
					packageDescr = new DrlParser().parse(drlReader, dslReader);
				else
					packageDescr = new DrlParser().parse(drlReader);
				builder.addPackage(packageDescr);
			}
			ruleBase.addPackage(builder.getPackage());
			log.info("compiled rule base");
			currentStatefulSession = ruleBase.newStatefulSession();
		} else
			log.warn("didn't set the rule files");
	}

	public void setDslFile(Resource dslFile) {
		this.dslFile = dslFile;
	}

	public void setRuleFiles(Resource[] ruleFiles) {
		this.ruleFiles = ruleFiles;
	}

	@PostConstruct
	public void afterPropertiesSet() throws Exception {
		compileRuleBase();
	}

}
