package ink.andromeda.dataflow.core.converter;

public interface DataConverterFactory<I> {

    FlowNode buildConverter(I buildSource);

    default FlowNode buildConverter(){
        throw new UnsupportedOperationException();
    };
}
