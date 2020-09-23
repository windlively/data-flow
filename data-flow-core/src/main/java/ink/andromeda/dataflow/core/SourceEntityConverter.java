package ink.andromeda.dataflow.core;

import org.springframework.core.convert.converter.Converter;

public interface SourceEntityConverter<T> extends Converter<T, SourceEntity> {


}
