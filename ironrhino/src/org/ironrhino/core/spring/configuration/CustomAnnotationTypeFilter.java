package org.ironrhino.core.spring.configuration;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Map;

import org.ironrhino.core.util.AppInfo;
import org.ironrhino.core.util.AppInfo.RunLevel;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.AnnotationTypeFilter;

public class CustomAnnotationTypeFilter extends AnnotationTypeFilter {

	public CustomAnnotationTypeFilter(Class<? extends Annotation> annotationType) {
		super(annotationType);
	}

	public CustomAnnotationTypeFilter(
			Class<? extends Annotation> annotationType,
			boolean considerMetaAnnotations) {
		super(annotationType, considerMetaAnnotations, false);
	}

	public CustomAnnotationTypeFilter(
			Class<? extends Annotation> annotationType,
			boolean considerMetaAnnotations, boolean considerInterfaces) {
		super(annotationType, considerMetaAnnotations, considerInterfaces);
	}

	@Override
	public boolean match(MetadataReader mr, MetadataReaderFactory mrf)
			throws IOException {
		if (!super.match(mr, mrf))
			return false;
		AnnotationMetadata metadata = mr.getAnnotationMetadata();
		Map<String, Object> attributes = metadata
				.getAnnotationAttributes(RunLevelConditional.class.getName());
		if (attributes != null
				&& AppInfo.getRunLevel().compareTo(
						(RunLevel) attributes.get("value")) < 0)
			return false;
		attributes = metadata.getAnnotationAttributes(StageConditional.class
				.getName());
		if (attributes != null && AppInfo.getStage() != attributes.get("value"))
			return false;
		return true;
	}

}
