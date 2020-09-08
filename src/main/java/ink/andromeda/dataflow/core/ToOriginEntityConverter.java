package ink.andromeda.dataflow.core;

import org.springframework.core.convert.converter.Converter;

public interface ToOriginEntityConverter<T> extends Converter<T, OriginEntity> {


}
