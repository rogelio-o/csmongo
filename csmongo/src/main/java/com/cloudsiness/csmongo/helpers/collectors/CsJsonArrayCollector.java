package com.cloudsiness.csmongo.helpers.collectors;

import java.util.EnumSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import io.vertx.core.json.JsonArray;

public class CsJsonArrayCollector implements Collector<Object, JsonArray, JsonArray> {

	@Override
	public Supplier<JsonArray> supplier() {
		return () -> new JsonArray();
	}

	@Override
	public BiConsumer<JsonArray, Object> accumulator() {
		return (builder, t) -> builder.add(t);
	}

	@Override
	public BinaryOperator<JsonArray> combiner() {
		return (left, right) -> {
            left.addAll(right);
            return left;
        };
	}

	@Override
	public Function<JsonArray, JsonArray> finisher() {
		return builder -> builder;
	}

	@Override
	public Set<java.util.stream.Collector.Characteristics> characteristics() {
		return EnumSet.of(Characteristics.UNORDERED);
	}


}