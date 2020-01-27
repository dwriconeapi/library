package org.ricone.library.client.oneroster.request;

import java.util.function.Consumer;

final class SortingBuilder {

	private Sorting sorting;

	public static Builder builder() {
		return new SortingBuilder.Builder();
	}

	private SortingBuilder() {
	}

	public Sorting getSorting() {
		return sorting;
	}

	public static class Builder {
		private SortingBuilder instance = new SortingBuilder();
		private WithBuilder.Builder parentBuilder;
		private Consumer<SortingBuilder> callback;

		public Builder() {
		}

		public Builder(WithBuilder.Builder b, Consumer<SortingBuilder> f) {
			this.parentBuilder = b;
			this.callback = f;
		}

		public Builder field(IField field) {
			instance.sorting.setField(field);
			return this;
		}

		public Builder orderBy(SortOrder sortOrder) {
			instance.sorting.setOrderBy(sortOrder);
			return this;
		}

		public WithBuilder.Builder end() {
			callback.accept(instance);
			return parentBuilder;
		}
	}		
}