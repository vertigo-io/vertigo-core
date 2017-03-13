package io.vertigo.dynamo.domain.util;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.lang.Assertion;

/**
 * Collectors 'Lite' Java 8 pour vertigo
 *
 * @since 1.8
 */
public final class VCollectors {

	private static final Set<Collector.Characteristics> CH_ID = Collections
			.unmodifiableSet(EnumSet.of(Collector.Characteristics.IDENTITY_FINISH));

	private VCollectors() {
	}

	/**
	 * @author xdurand
	 * @param <T>
	 * @param <A>
	 * @param <R>
	 */
	private static class CollectorImpl<T, A, R> implements Collector<T, A, R> {

		private final Supplier<A> supplier;
		private final BiConsumer<A, T> accumulator;
		private final BinaryOperator<A> combiner;
		private final Function<A, R> finisher;
		private final Set<Characteristics> characteristics;

		/**
		 * Construct an instance of CollectorImpl.
		 *
		 * @param supplier
		 * @param accumulator
		 * @param combiner
		 * @param finisher
		 * @param characteristics
		 */
		CollectorImpl(final Supplier<A> supplier, final BiConsumer<A, T> accumulator, final BinaryOperator<A> combiner,
				final Function<A, R> finisher, final Set<Characteristics> characteristics) {
			this.supplier = supplier;
			this.accumulator = accumulator;
			this.combiner = combiner;
			this.finisher = finisher;
			this.characteristics = characteristics;
		}

		/**
		 * Construct an instance of CollectorImpl.
		 *
		 * @param supplier
		 * @param accumulator
		 * @param combiner
		 * @param characteristics
		 */
		CollectorImpl(final Supplier<A> supplier, final BiConsumer<A, T> accumulator, final BinaryOperator<A> combiner,
				final Set<Characteristics> characteristics) {
			this(supplier, accumulator, combiner, id(), characteristics);
		}

		@Override
		public BiConsumer<A, T> accumulator() {
			return accumulator;
		}

		@Override
		public Supplier<A> supplier() {
			return supplier;
		}

		@Override
		public BinaryOperator<A> combiner() {
			return combiner;
		}

		@Override
		public Function<A, R> finisher() {
			return finisher;
		}

		@Override
		public Set<Characteristics> characteristics() {
			return characteristics;
		}

		@SuppressWarnings("unchecked")
		private static <I, R> Function<I, R> id() {
			return i -> (R) i;
		}
	}

	/**
	 *
	 * @param dtClass
	 * @return A collector for DtList
	 */
	public static <T extends DtObject> Collector<T, ?, DtList<T>> toDtList(final Class<T> dtClass) {
		Assertion.checkNotNull(dtClass);
		//---
		final Supplier<DtList<T>> dtSupplier = () -> new DtList<>(dtClass);
		return new CollectorImpl<>(dtSupplier, List::add, (left, right) -> {
			left.addAll(right);
			return left;
		}, CH_ID);
	}
}
