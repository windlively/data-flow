package ink.andromeda.dataflow.core.converter;

public interface DataConverterFactory<I> {

    DataConverter buildConverter(I buildSource);

    default DataConverter buildConverter(){
        throw new UnsupportedOperationException();
    };
}
