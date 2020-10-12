package ink.andromeda.dataflow.core;

import org.springframework.core.convert.converter.Converter;

@FunctionalInterface
public interface SourceEntityConverter<T> extends Converter<T, SourceEntity> {


}
