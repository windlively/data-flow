package ink.andromeda.dataflow.core.converter;

import ink.andromeda.dataflow.core.converter.configuarion.SpringELConfigurationResolver;
import lombok.Getter;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class ConfigurableDataConvertResolverRegistry {

    private final LinkedList<SpringELConfigurationResolver> internalConvertConfigResolver = new LinkedList<>();

    private final LinkedList<SpringELConfigurationResolver> internalExportConfigResolver = new LinkedList<>();
    
    @Getter
    private List<SpringELConfigurationResolver> convertConfigResolver = Collections.emptyList();

    @Getter
    private List<SpringELConfigurationResolver> exportConfigResolver = Collections.emptyList();

    public synchronized void addConvertResolver(SpringELConfigurationResolver resolver){
        addResolver(resolver, internalConvertConfigResolver);
    }
    
    public synchronized void addConvertResolverFirst(SpringELConfigurationResolver resolver){
        addResolverFirst(resolver, internalConvertConfigResolver);
    }
    
    public synchronized void addConvertResolverAfter(String prevName, SpringELConfigurationResolver resolver){
        addResolverAfter(prevName, resolver, internalConvertConfigResolver);
    }
    
    public synchronized void addExportResolver(SpringELConfigurationResolver resolver){
        addResolver(resolver, internalExportConfigResolver);
    }

    public synchronized void addExportResolverFirst(SpringELConfigurationResolver resolver){
        addResolverFirst(resolver, internalExportConfigResolver);
    }

    public synchronized void addExportResolverAfter(String prevName, SpringELConfigurationResolver resolver){
        addResolverAfter(prevName, resolver, internalExportConfigResolver);
    }
    
    public void updateResolverList(){
        convertConfigResolver = Collections.unmodifiableList(internalConvertConfigResolver);
        exportConfigResolver = Collections.unmodifiableList(internalExportConfigResolver);
    }
    
    private void addResolver(SpringELConfigurationResolver resolver, LinkedList<SpringELConfigurationResolver> list){
        list.add(resolver);
    }

    private void addResolverFirst(SpringELConfigurationResolver resolver, LinkedList<SpringELConfigurationResolver> list){
        list.addFirst(resolver);
    }

    private void addResolverAfter(String prevName, SpringELConfigurationResolver resolver, LinkedList<SpringELConfigurationResolver> list){
        for (int i = 0; i < list.size(); i ++) {
            if(list.get(i).getName().equals(prevName)){
                list.add(i + 1, resolver);
                return;
            }
        }
        throw new IllegalArgumentException("could not found configuration resolver '" + prevName +"'");
    }
    
}
