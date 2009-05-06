/*
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.gson;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashSet;

/**
 * Exclude fields and getter based on particular annotation.
 * 
 * @author zhouyanming
 */
final class AnnotationBasedExclusionStrategy implements ExclusionStrategy {
	private final boolean skipSyntheticField;
	private final Collection<Class<? extends Annotation>> annotationclasses;

	public AnnotationBasedExclusionStrategy(boolean skipSyntheticFields,
			Class<? extends Annotation>... annotationclasses) {
		this.skipSyntheticField = skipSyntheticFields;
		this.annotationclasses = new HashSet<Class<? extends Annotation>>();
		if (annotationclasses != null) {
			for (Class<? extends Annotation> clazz : annotationclasses) {
				this.annotationclasses.add(clazz);
			}
		}
	}

	public boolean shouldSkipField(Field f) {
		if (skipSyntheticField && f.isSynthetic()) {
			return true;
		}
		for (Class<? extends Annotation> annotationClass : annotationclasses)
			if (f.getAnnotation(annotationClass) != null)
				return true;
		return false;
	}

	public boolean shouldSkipClass(Class<?> clazz) {
		for (Class<? extends Annotation> annotationClass : annotationclasses)
			if (clazz.getAnnotation(annotationClass) != null)
				return true;
		return false;
	}
}
