package ink.andromeda.dataflow.core;

import ink.andromeda.dataflow.entity.SourceEntity;
import org.springframework.core.convert.converter.Converter;

public interface ToSourceEntityConverter<T> extends Converter<T, SourceEntity> {


}
