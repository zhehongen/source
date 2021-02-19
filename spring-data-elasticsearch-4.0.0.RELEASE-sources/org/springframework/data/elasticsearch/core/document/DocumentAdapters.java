/*
 * Copyright 2019-2020 the original author or authors.
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
package org.springframework.data.elasticsearch.core.document;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.get.MultiGetResponse;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.document.DocumentField;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.get.GetResult;
import org.elasticsearch.search.SearchHit;
import org.springframework.data.elasticsearch.ElasticsearchException;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

/**
 * Utility class to adapt {@link org.elasticsearch.action.get.GetResponse},
 * {@link org.elasticsearch.index.get.GetResult}, {@link org.elasticsearch.action.get.MultiGetResponse}
 * {@link org.elasticsearch.search.SearchHit}, {@link org.elasticsearch.common.document.DocumentField} to
 * {@link Document}.
 * 实用程序类，用于将GetResponse，GetResult，MultiGetResponse SearchHit，DocumentField适配到Document。
 * @author Mark Paluch
 * @author Peter-Josef Meisch
 * @author Roman Puchkovskiy
 * @since 4.0
 */
public class DocumentAdapters {

	/**
	 * Create a {@link Document} from {@link GetResponse}.
	 * <p>
	 * Returns a {@link Document} using the source if available.
	 *
	 * @param source the source {@link GetResponse}.
	 * @return the adapted {@link Document}, null if source.isExists() returns false.
	 */
	@Nullable
	public static Document from(GetResponse source) {

		Assert.notNull(source, "GetResponse must not be null");

		if (!source.isExists()) {
			return null;
		}

		if (source.isSourceEmpty()) {
			return fromDocumentFields(source, source.getId(), source.getVersion(), source.getSeqNo(),
					source.getPrimaryTerm());
		}

		Document document = Document.from(source.getSourceAsMap());
		document.setId(source.getId());
		document.setVersion(source.getVersion());
		document.setSeqNo(source.getSeqNo());
		document.setPrimaryTerm(source.getPrimaryTerm());

		return document;
	}

	/**
	 * Create a {@link Document} from {@link GetResult}.
	 * <p>
	 * Returns a {@link Document} using the source if available.
	 *
	 * @param source the source {@link GetResult}.
	 * @return the adapted {@link Document}, null if source.isExists() returns false.
	 */
	@Nullable
	public static Document from(GetResult source) {

		Assert.notNull(source, "GetResult must not be null");

		if (!source.isExists()) {
			return null;
		}

		if (source.isSourceEmpty()) {
			return fromDocumentFields(source, source.getId(), source.getVersion(), source.getSeqNo(),
					source.getPrimaryTerm());
		}

		Document document = Document.from(source.getSource());
		document.setId(source.getId());
		document.setVersion(source.getVersion());
		document.setSeqNo(source.getSeqNo());
		document.setPrimaryTerm(source.getPrimaryTerm());

		return document;
	}

	/**
	 * Creates a List of {@link Document}s from {@link MultiGetResponse}.
	 *
	 * @param source the source {@link MultiGetResponse}, not {@literal null}.
	 * @return a list of Documents, contains null values for not found Documents.文档列表，包含未找到文档的空值。
	 */
	public static List<Document> from(MultiGetResponse source) {

		Assert.notNull(source, "MultiGetResponse must not be null");

		// noinspection ReturnOfNull
		return Arrays.stream(source.getResponses()) //
				.map(itemResponse -> itemResponse.isFailed() ? null : DocumentAdapters.from(itemResponse.getResponse())) //
				.collect(Collectors.toList());
	}

	/**
	 * Create a {@link SearchDocument} from {@link SearchHit}.
	 * <p>
	 * Returns a {@link SearchDocument} using the source if available.
	 *
	 * @param source the source {@link SearchHit}.
	 * @return the adapted {@link SearchDocument}.
	 */
	public static SearchDocument from(SearchHit source) {

		Assert.notNull(source, "SearchHit must not be null");

		Map<String, List<String>> highlightFields = new HashMap<>(source.getHighlightFields().entrySet().stream() //
				.collect(Collectors.toMap(Map.Entry::getKey,
						entry -> Arrays.stream(entry.getValue().getFragments()).map(Text::string).collect(Collectors.toList()))));

		BytesReference sourceRef = source.getSourceRef();

		if (sourceRef == null || sourceRef.length() == 0) {
			return new SearchDocumentAdapter(source.getScore(), source.getSortValues(), source.getFields(), highlightFields,
					fromDocumentFields(source, source.getId(), source.getVersion(), source.getSeqNo(), source.getPrimaryTerm()));
		}

		Document document = Document.from(source.getSourceAsMap());
		document.setId(source.getId());

		if (source.getVersion() >= 0) {
			document.setVersion(source.getVersion());
		}
		document.setSeqNo(source.getSeqNo());
		document.setPrimaryTerm(source.getPrimaryTerm());

		return new SearchDocumentAdapter(source.getScore(), source.getSortValues(), source.getFields(), highlightFields,
				document);
	}

	/**
	 * Create an unmodifiable {@link Document} from {@link Iterable} of {@link DocumentField}s.
	 *
	 * @param documentFields the {@link DocumentField}s backing the {@link Document}.
	 * @return the adapted {@link Document}.
	 */
	public static Document fromDocumentFields(Iterable<DocumentField> documentFields, String id, long version, long seqNo,
			long primaryTerm) {

		if (documentFields instanceof Collection) {
			return new DocumentFieldAdapter((Collection<DocumentField>) documentFields, id, version, seqNo, primaryTerm);
		}

		List<DocumentField> fields = new ArrayList<>();
		for (DocumentField documentField : documentFields) {
			fields.add(documentField);
		}

		return new DocumentFieldAdapter(fields, id, version, seqNo, primaryTerm);
	}

	// TODO: Performance regarding keys/values/entry-set 关于键/值/条目集的性能
	static class DocumentFieldAdapter implements Document {

		private final Collection<DocumentField> documentFields;
		private final String id;
		private final long version;
		private final long seqNo;
		private final long primaryTerm;

		DocumentFieldAdapter(Collection<DocumentField> documentFields, String id, long version, long seqNo,
				long primaryTerm) {
			this.documentFields = documentFields;
			this.id = id;
			this.version = version;
			this.seqNo = seqNo;
			this.primaryTerm = primaryTerm;
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.data.elasticsearch.core.document.Document#hasId()
		 */
		@Override
		public boolean hasId() {
			return StringUtils.hasLength(id);
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.data.elasticsearch.core.document.Document#getId()
		 */
		@Override
		public String getId() {
			return this.id;
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.data.elasticsearch.core.document.Document#hasVersion()
		 */
		@Override
		public boolean hasVersion() {
			return this.version >= 0;
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.data.elasticsearch.core.document.Document#getVersion()
		 */
		@Override
		public long getVersion() {

			if (!hasVersion()) {
				throw new IllegalStateException("No version associated with this Document");
			}

			return this.version;
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.data.elasticsearch.core.document.Document#hasSeqNo()
		 */
		@Override
		public boolean hasSeqNo() {
			return true;
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.data.elasticsearch.core.document.Document#getSeqNo()
		 */
		@Override
		public long getSeqNo() {

			if (!hasSeqNo()) {
				throw new IllegalStateException("No seq_no associated with this Document");
			}

			return this.seqNo;
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.data.elasticsearch.core.document.Document#hasPrimaryTerm()
		 */
		@Override
		public boolean hasPrimaryTerm() {
			return true;
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.data.elasticsearch.core.document.Document#getPrimaryTerm()
		 */
		@Override
		public long getPrimaryTerm() {

			if (!hasPrimaryTerm()) {
				throw new IllegalStateException("No primary_term associated with this Document");
			}

			return this.primaryTerm;
		}

		/*
		 * (non-Javadoc)
		 * @see java.util.Map#size()
		 */
		@Override
		public int size() {
			return documentFields.size();
		}

		/*
		 * (non-Javadoc)
		 * @see java.util.Map#isEmpty()
		 */
		@Override
		public boolean isEmpty() {
			return documentFields.isEmpty();
		}

		/*
		 * (non-Javadoc)
		 * @see java.util.Map#containsKey(java.lang.Object)
		 */
		@Override
		public boolean containsKey(Object key) {

			for (DocumentField documentField : documentFields) {
				if (documentField.getName().equals(key)) {
					return true;
				}
			}

			return false;
		}

		/*
		 * (non-Javadoc)
		 * @see java.util.Map#containsValue(java.lang.Object)
		 */
		@Override
		public boolean containsValue(Object value) {

			for (DocumentField documentField : documentFields) {

				Object fieldValue = getValue(documentField);
				if (fieldValue != null && fieldValue.equals(value) || value == fieldValue) {
					return true;
				}
			}

			return false;
		}

		/*
		 * (non-Javadoc)
		 * @see java.util.Map#get(java.lang.Object)
		 */
		@Override
		@Nullable
		public Object get(Object key) {
			return documentFields.stream() //
					.filter(documentField -> documentField.getName().equals(key)) //
					.map(DocumentField::getValue).findFirst() //
					.orElse(null); //

		}

		/*
		 * (non-Javadoc)
		 * @see java.util.Map#put(java.lang.Object, java.lang.Object)
		 */
		@Override
		public Object put(String key, Object value) {
			throw new UnsupportedOperationException();
		}

		/*
		 * (non-Javadoc)
		 * @see java.util.Map#remove(java.lang.Object)
		 */
		@Override
		public Object remove(Object key) {
			throw new UnsupportedOperationException();
		}

		/*
		 * (non-Javadoc)
		 * @see java.util.Map#putAll(Map)
		 */
		@Override
		public void putAll(Map<? extends String, ?> m) {
			throw new UnsupportedOperationException();
		}

		/*
		 * (non-Javadoc)
		 * @see java.util.Map#clear()
		 */
		@Override
		public void clear() {
			throw new UnsupportedOperationException();
		}

		/*
		 * (non-Javadoc)
		 * @see java.util.Map#keySet()
		 */
		@Override
		public Set<String> keySet() {
			return documentFields.stream().map(DocumentField::getName).collect(Collectors.toCollection(LinkedHashSet::new));
		}

		/*
		 * (non-Javadoc)
		 * @see java.util.Map#values()
		 */
		@Override
		public Collection<Object> values() {
			return documentFields.stream().map(DocumentFieldAdapter::getValue).collect(Collectors.toList());
		}

		/*
		 * (non-Javadoc)
		 * @see java.util.Map#entrySet()
		 */
		@Override
		public Set<Entry<String, Object>> entrySet() {
			return documentFields.stream().collect(Collectors.toMap(DocumentField::getName, DocumentFieldAdapter::getValue))
					.entrySet();
		}

		/*
		 * (non-Javadoc)
		 * @see java.util.Map#forEach(java.util.function.BiConsumer)
		 */
		@Override
		public void forEach(BiConsumer<? super String, ? super Object> action) {

			Objects.requireNonNull(action);

			documentFields.forEach(field -> action.accept(field.getName(), getValue(field)));
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.data.elasticsearch.core.document.Document#toJson()
		 */
		@Override
		public String toJson() {

			JsonFactory nodeFactory = new JsonFactory();
			try {
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				JsonGenerator generator = nodeFactory.createGenerator(stream, JsonEncoding.UTF8);
				generator.writeStartObject();
				for (DocumentField value : documentFields) {
					if (value.getValues().size() > 1) {
						generator.writeArrayFieldStart(value.getName());
						for (Object val : value.getValues()) {
							generator.writeObject(val);
						}
						generator.writeEndArray();
					} else {
						generator.writeObjectField(value.getName(), value.getValue());
					}
				}
				generator.writeEndObject();
				generator.flush();
				return new String(stream.toByteArray(), StandardCharsets.UTF_8);
			} catch (IOException e) {
				throw new ElasticsearchException("Cannot render JSON", e);
			}
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return getClass().getSimpleName() + '@' + this.id + '#' + this.version + ' ' + toJson();
		}

		@Nullable
		private static Object getValue(DocumentField documentField) {

			if (documentField.getValues().isEmpty()) {
				return null;
			}

			if (documentField.getValues().size() == 1) {
				return documentField.getValue();
			}

			return documentField.getValues();
		}
	}

	/**
	 * Adapter for a {@link SearchDocument}.
	 */
	static class SearchDocumentAdapter implements SearchDocument {

		private final float score;
		private final Object[] sortValues;
		private final Map<String, List<Object>> fields = new HashMap<>();
		private final Document delegate;
		private final Map<String, List<String>> highlightFields = new HashMap<>();

		SearchDocumentAdapter(float score, Object[] sortValues, Map<String, DocumentField> fields,
				Map<String, List<String>> highlightFields, Document delegate) {

			this.score = score;
			this.sortValues = sortValues;
			this.delegate = delegate;
			fields.forEach((name, documentField) -> this.fields.put(name, documentField.getValues()));
			this.highlightFields.putAll(highlightFields);
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.data.elasticsearch.core.document.Document#append(java.lang.String, java.lang.Object)
		 */
		@Override
		public SearchDocument append(String key, Object value) {
			delegate.append(key, value);

			return this;
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.data.elasticsearch.core.document.SearchDocument#getScore()
		 */
		@Override
		public float getScore() {
			return score;
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.data.elasticsearch.core.document.SearchDocument#getFields()
		 */
		@Override
		public Map<String, List<Object>> getFields() {
			return fields;
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.data.elasticsearch.core.document.SearchDocument#getSortValues()
		 */
		@Override
		public Object[] getSortValues() {
			return sortValues;
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.data.elasticsearch.core.document.SearchDocument#getHighlightFields()
		 */
		@Override
		public Map<String, List<String>> getHighlightFields() {
			return highlightFields;
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.data.elasticsearch.core.document.Document#hasId()
		 */
		@Override
		public boolean hasId() {
			return delegate.hasId();
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.data.elasticsearch.core.document.Document#getId()
		 */
		@Override
		public String getId() {
			return delegate.getId();
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.data.elasticsearch.core.document.Document#setId(java.lang.String)
		 */
		@Override
		public void setId(String id) {
			delegate.setId(id);
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.data.elasticsearch.core.document.Document#hasVersion()
		 */
		@Override
		public boolean hasVersion() {
			return delegate.hasVersion();
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.data.elasticsearch.core.document.Document#getVersion()
		 */
		@Override
		public long getVersion() {
			return delegate.getVersion();
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.data.elasticsearch.core.document.Document#setVersion(long)
		 */
		@Override
		public void setVersion(long version) {
			delegate.setVersion(version);
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.data.elasticsearch.core.document.Document#hasSeqNo()
		 */
		@Override
		public boolean hasSeqNo() {
			return delegate.hasSeqNo();
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.data.elasticsearch.core.document.Document#getSeqNo()
		 */
		@Override
		public long getSeqNo() {
			return delegate.getSeqNo();
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.data.elasticsearch.core.document.Document#setSeqNo(long)
		 */
		@Override
		public void setSeqNo(long seqNo) {
			delegate.setSeqNo(seqNo);
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.data.elasticsearch.core.document.Document#hasPrimaryTerm()
		 */
		@Override
		public boolean hasPrimaryTerm() {
			return delegate.hasPrimaryTerm();
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.data.elasticsearch.core.document.Document#getPrimaryTerm()
		 */
		@Override
		public long getPrimaryTerm() {
			return delegate.getPrimaryTerm();
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.data.elasticsearch.core.document.Document#setPrimaryTerm(long)
		 */
		@Override
		public void setPrimaryTerm(long primaryTerm) {
			delegate.setPrimaryTerm(primaryTerm);
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.data.elasticsearch.core.document.Document#get(java.lang.Object, java.lang.Class)
		 */
		@Override
		@Nullable
		public <T> T get(Object key, Class<T> type) {
			return delegate.get(key, type);
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.data.elasticsearch.core.document.Document#toJson()
		 */
		@Override
		public String toJson() {
			return delegate.toJson();
		}

		/*
		 * (non-Javadoc)
		 * @see java.util.Map#size()
		 */
		@Override
		public int size() {
			return delegate.size();
		}

		/*
		 * (non-Javadoc)
		 * @see java.util.Map#isEmpty()
		 */
		@Override
		public boolean isEmpty() {
			return delegate.isEmpty();
		}

		/*
		 * (non-Javadoc)
		 * @see java.util.Map#containsKey(java.lang.Object)
		 */
		@Override
		public boolean containsKey(Object key) {
			return delegate.containsKey(key);
		}

		/*
		 * (non-Javadoc)
		 * @see java.util.Map#containsValue(java.lang.Object)
		 */
		@Override
		public boolean containsValue(Object value) {
			return delegate.containsValue(value);
		}

		/*
		 * (non-Javadoc)
		 * @see java.util.Map#get(java.lang.Object)
		 */
		@Override
		public Object get(Object key) {
			return delegate.get(key);
		}

		/*
		 * (non-Javadoc)
		 * @see java.util.Map#put(java.lang.Object, java.lang.Object)
		 */
		@Override
		public Object put(String key, Object value) {
			return delegate.put(key, value);
		}

		/*
		 * (non-Javadoc)
		 * @see java.util.Map#remove(java.lang.Object)
		 */
		@Override
		public Object remove(Object key) {
			return delegate.remove(key);
		}

		/*
		 * (non-Javadoc)
		 * @see java.util.Map#putAll(Map)
		 */
		@Override
		public void putAll(Map<? extends String, ?> m) {
			delegate.putAll(m);
		}

		/*
		 * (non-Javadoc)
		 * @see java.util.Map#clear()
		 */
		@Override
		public void clear() {
			delegate.clear();
		}

		/*
		 * (non-Javadoc)
		 * @see java.util.Map#keySet()
		 */
		@Override
		public Set<String> keySet() {
			return delegate.keySet();
		}

		/*
		 * (non-Javadoc)
		 * @see java.util.Map#values()
		 */
		@Override
		public Collection<Object> values() {
			return delegate.values();
		}

		/*
		 * (non-Javadoc)
		 * @see java.util.Map#entrySet()
		 */
		@Override
		public Set<Entry<String, Object>> entrySet() {
			return delegate.entrySet();
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (!(o instanceof SearchDocumentAdapter)) {
				return false;
			}
			SearchDocumentAdapter that = (SearchDocumentAdapter) o;
			return Float.compare(that.score, score) == 0 && delegate.equals(that.delegate);
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			return delegate.hashCode();
		}

		/*
		 * (non-Javadoc)
		 * @see java.util.Map#forEach(java.util.function.BiConsumer)
		 */
		@Override
		public void forEach(BiConsumer<? super String, ? super Object> action) {
			delegate.forEach(action);
		}

		/*
		 * (non-Javadoc)
		 * @see java.util.Map#remove(java.lang.Object, java.lang.Object)
		 */
		@Override
		public boolean remove(Object key, Object value) {
			return delegate.remove(key, value);
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {

			String id = hasId() ? getId() : "?";
			String version = hasVersion() ? Long.toString(getVersion()) : "?";

			return getClass().getSimpleName() + '@' + id + '#' + version + ' ' + toJson();
		}
	}
}
