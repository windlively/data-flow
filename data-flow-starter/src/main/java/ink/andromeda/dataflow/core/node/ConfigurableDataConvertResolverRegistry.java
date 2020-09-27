package ink.andromeda.dataflow.core.node;

import ink.andromeda.dataflow.core.node.resolver.DefaultConfigurationResolver;
import lombok.Getter;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class ConfigurableDataConvertResolverRegistry {

    private final LinkedList<DefaultConfigurationResolver> internalConvertConfigResolver = new LinkedList<>();

    private final LinkedList<DefaultConfigurationResolver> internalExportConfigResolver = new LinkedList<>();
    
    @Getter
    private List<DefaultConfigurationResolver> convertConfigResolver = Collections.emptyList();

    @Getter
    private List<DefaultConfigurationResolver> exportConfigResolver = Collections.emptyList();

    public synchronized void addConvertResolver(DefaultConfigurationResolver resolver){
        addResolver(resolver, internalConvertConfigResolver);
    }
    
    public synchronized void addConvertResolverFirst(DefaultConfigurationResolver resolver){
        addResolverFirst(resolver, internalConvertConfigResolver);
    }
    
    public synchronized void addConvertResolverAfter(String prevName, DefaultConfigurationResolver resolver){
        addResolverAfter(prevName, resolver, internalConvertConfigResolver);
    }
    
    public synchronized void addExportResolver(DefaultConfigurationResolver resolver){
        addResolver(resolver, internalExportConfigResolver);
    }

    public synchronized void addExportResolverFirst(DefaultConfigurationResolver resolver){
        addResolverFirst(resolver, internalExportConfigResolver);
    }

    public synchronized void addExportResolverAfter(String prevName, DefaultConfigurationResolver resolver){
        addResolverAfter(prevName, resolver, internalExportConfigResolver);
    }
    
    public void updateResolverList(){
        convertConfigResolver = Collections.unmodifiableList(internalConvertConfigResolver);
        exportConfigResolver = Collections.unmodifiableList(internalExportConfigResolver);
    }
    
    private void addResolver(DefaultConfigurationResolver resolver, LinkedList<DefaultConfigurationResolver> list){
        list.add(resolver);
    }

    private void addResolverFirst(DefaultConfigurationResolver resolver, LinkedList<DefaultConfigurationResolver> list){
        list.addFirst(resolver);
    }

    private void addResolverAfter(String prevName, DefaultConfigurationResolver resolver, LinkedList<DefaultConfigurationResolver> list){
        for (int i = 0; i < list.size(); i ++) {
            if(list.get(i).getName().equals(prevName)){
                list.add(i + 1, resolver);
                return;
            }
        }
        throw new IllegalArgumentException("could not found configuration resolver '" + prevName +"'");
    }
    
}
