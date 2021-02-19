/*
 * Copyright 2013-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.data.elasticsearch.core.mapping;

import static org.springframework.util.StringUtils.*;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import org.elasticsearch.index.VersionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.expression.BeanFactoryAccessor;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Parent;
import org.springframework.data.elasticsearch.annotations.Setting;
import org.springframework.data.mapping.MappingException;
import org.springframework.data.mapping.PropertyHandler;
import org.springframework.data.mapping.model.BasicPersistentEntity;
import org.springframework.data.mapping.model.PersistentPropertyAccessorFactory;
import org.springframework.data.util.TypeInformation;
import org.springframework.expression.Expression;
import org.springframework.expression.ParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * Elasticsearch specific {@link org.springframework.data.mapping.PersistentEntity} implementation holding
 *
 * @param <T>
 * @author Rizwan Idrees
 * @author Mohsin Husen
 * @author Mark Paluch
 * @author Sascha Woo
 * @author Ivan Greene
 * @author Peter-Josef Meisch
 * @author Roman Puchkovskiy
 */
public class SimpleElasticsearchPersistentEntity<T> extends BasicPersistentEntity<T, ElasticsearchPersistentProperty>
		implements ElasticsearchPersistentEntity<T>, ApplicationContextAware {

	private static final Logger LOGGER = LoggerFactory.getLogger(SimpleElasticsearchPersistentEntity.class);

	private final StandardEvaluationContext context;
	private final SpelExpressionParser parser;

	private @Nullable String indexName;
	private @Nullable String indexType;
	private boolean useServerConfiguration;
	private short shards;
	private short replicas;
	private @Nullable String refreshInterval;
	private @Nullable String indexStoreType;
	private @Nullable String parentType;
	private @Nullable ElasticsearchPersistentProperty parentIdProperty;
	private @Nullable ElasticsearchPersistentProperty scoreProperty;
	private @Nullable ElasticsearchPersistentProperty seqNoPrimaryTermProperty;
	private @Nullable String settingPath;
	private @Nullable VersionType versionType;
	private boolean createIndexAndMapping;
	private final Map<String, ElasticsearchPersistentProperty> fieldNamePropertyCache = new ConcurrentHashMap<>();

	public SimpleElasticsearchPersistentEntity(TypeInformation<T> typeInformation) {

		super(typeInformation);
		this.context = new StandardEvaluationContext();
		this.parser = new SpelExpressionParser();

		Class<T> clazz = typeInformation.getType();
		if (clazz.isAnnotationPresent(Document.class)) {
			Document document = clazz.getAnnotation(Document.class);
			Assert.hasText(document.indexName(),
					" Unknown indexName. Make sure the indexName is defined. e.g @Document(indexName=\"foo\")");
			this.indexName = document.indexName();
			this.indexType = hasText(document.type()) ? document.type() : clazz.getSimpleName().toLowerCase(Locale.ENGLISH);
			this.useServerConfiguration = document.useServerConfiguration();
			this.shards = document.shards();
			this.replicas = document.replicas();
			this.refreshInterval = document.refreshInterval();
			this.indexStoreType = document.indexStoreType();
			this.versionType = document.versionType();
			this.createIndexAndMapping = document.createIndex();
		}
		if (clazz.isAnnotationPresent(Setting.class)) {
			this.settingPath = typeInformation.getType().getAnnotation(Setting.class).settingPath();
		}
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		context.addPropertyAccessor(new BeanFactoryAccessor());
		context.setBeanResolver(new BeanFactoryResolver(applicationContext));
		context.setRootObject(applicationContext);
	}

	private String getIndexName() {

		if (indexName != null) {
			Expression expression = parser.parseExpression(indexName, ParserContext.TEMPLATE_EXPRESSION);
			return expression.getValue(context, String.class);
		}

		return getTypeInformation().getType().getSimpleName();
	}

	private String getIndexType() {

		if (indexType != null) {
			Expression expression = parser.parseExpression(indexType, ParserContext.TEMPLATE_EXPRESSION);
			return expression.getValue(context, String.class);
		}

		return "";
	}

	@Override
	public IndexCoordinates getIndexCoordinates() {
		return IndexCoordinates.of(getIndexName()).withTypes(getIndexType());
	}

	@Nullable
	@Override
	public String getIndexStoreType() {
		return indexStoreType;
	}

	@Override
	public short getShards() {
		return shards;
	}

	@Override
	public short getReplicas() {
		return replicas;
	}

	@Override
	public boolean isUseServerConfiguration() {
		return useServerConfiguration;
	}

	@Nullable
	@Override
	public String getRefreshInterval() {
		return refreshInterval;
	}

	@Nullable
	@Override
	public String getParentType() {
		return parentType;
	}

	@Nullable
	@Override
	public ElasticsearchPersistentProperty getParentIdProperty() {
		return parentIdProperty;
	}

	@Nullable
	@Override
	public VersionType getVersionType() {
		return versionType;
	}

	@Override
	public String settingPath() {
		return settingPath;
	}

	@Override
	public boolean isCreateIndexAndMapping() {
		return createIndexAndMapping;
	}

	@Override
	public boolean hasScoreProperty() {
		return scoreProperty != null;
	}

	@Nullable
	@Override
	public ElasticsearchPersistentProperty getScoreProperty() {
		return scoreProperty;
	}

	@Override
	public void addPersistentProperty(ElasticsearchPersistentProperty property) {
		super.addPersistentProperty(property);

		if (property.isParentProperty()) {
			ElasticsearchPersistentProperty parentProperty = this.parentIdProperty;

			if (parentProperty != null) {
				throw new MappingException(String.format(
						"Attempt to add parent property %s but already have property %s registered "
								+ "as parent property. Check your mapping configuration!",
						property.getField(), parentProperty.getField()));
			}

			Parent parentAnnotation = property.findAnnotation(Parent.class);
			this.parentIdProperty = property;
			this.parentType = parentAnnotation.type();
		}

		if (property.isScoreProperty()) {

			ElasticsearchPersistentProperty scoreProperty = this.scoreProperty;

			if (scoreProperty != null) {
				throw new MappingException(String.format(
						"Attempt to add score property %s but already have property %s registered "
								+ "as score property. Check your mapping configuration!",
						property.getField(), scoreProperty.getField()));
			}

			this.scoreProperty = property;
		}

		if (property.isSeqNoPrimaryTermProperty()) {

			ElasticsearchPersistentProperty seqNoPrimaryTermProperty = this.seqNoPrimaryTermProperty;

			if (seqNoPrimaryTermProperty != null) {
				throw new MappingException(String.format(
						"Attempt to add SeqNoPrimaryTerm property %s but already have property %s registered "
								+ "as SeqNoPrimaryTerm property. Check your entity configuration!",
						property.getField(), seqNoPrimaryTermProperty.getField()));
			}

			this.seqNoPrimaryTermProperty = property;

			if (hasVersionProperty()) {
				warnAboutBothSeqNoPrimaryTermAndVersionProperties();
			}
		}

		if (property.isVersionProperty()) {
			if (hasSeqNoPrimaryTermProperty()) {
				warnAboutBothSeqNoPrimaryTermAndVersionProperties();
			}
		}
	}

	private void warnAboutBothSeqNoPrimaryTermAndVersionProperties() {
		LOGGER.warn(
				"Both SeqNoPrimaryTerm and @Version properties are defined on {}. Version will not be sent in index requests when seq_no is sent!",
				getType());
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.mapping.model.BasicPersistentEntity#setPersistentPropertyAccessorFactory(org.springframework.data.mapping.model.PersistentPropertyAccessorFactory)
	 */
	@Override
	public void setPersistentPropertyAccessorFactory(PersistentPropertyAccessorFactory factory) {

		// Do nothing to avoid the usage of ClassGeneratingPropertyAccessorFactory for now
		// DATACMNS-1322 switches to proper immutability behavior which Spring Data Elasticsearch
		// cannot yet implement
	}

	@Nullable
	@Override
	public ElasticsearchPersistentProperty getPersistentPropertyWithFieldName(String fieldName) {

		Assert.notNull(fieldName, "fieldName must not be null");

		return fieldNamePropertyCache.computeIfAbsent(fieldName, key -> {
			AtomicReference<ElasticsearchPersistentProperty> propertyRef = new AtomicReference<>();
			doWithProperties((PropertyHandler<ElasticsearchPersistentProperty>) property -> {
				if (key.equals(property.getFieldName())) {
					propertyRef.set(property);
				}
			});

			return propertyRef.get();
		});
	}

	@Override
	public boolean hasSeqNoPrimaryTermProperty() {
		return seqNoPrimaryTermProperty != null;
	}

	@Override
	@Nullable
	public ElasticsearchPersistentProperty getSeqNoPrimaryTermProperty() {
		return seqNoPrimaryTermProperty;
	}
}
