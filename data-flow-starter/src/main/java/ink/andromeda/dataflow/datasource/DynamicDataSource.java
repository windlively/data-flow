package ink.andromeda.dataflow.datasource;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.datasource.AbstractDataSource;
import org.springframework.jdbc.datasource.lookup.DataSourceLookup;
import org.springframework.jdbc.datasource.lookup.JndiDataSourceLookup;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 动态数据源
 * <p>{@link org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource}继承方式不好满足需求, 因此直接基于源码改造
 */
@Slf4j
public class DynamicDataSource extends AbstractDataSource implements DisposableBean, InitializingBean {

    private final ThreadLocal<String> currentLookupKey = ThreadLocal.withInitial(() -> "master");

    @Getter
    private final Map<Object, DataSource> includedDataSource = new ConcurrentHashMap<>();

    public DynamicDataSource(DataSource masterDataSource, Map<Object, DataSource> includedDataSource){
        addIncludedDataSource(includedDataSource);
        setMasterDataSource(masterDataSource);
    }

    public DynamicDataSource(){}

    public DynamicDataSource(DataSource masterDataSource){
        setMasterDataSource(masterDataSource);
    }

    protected Object determineCurrentLookupKey() {
        return getLookupKey();
    }

    public void changeLookupKey(String key){
        currentLookupKey.set(key);
    }

    public String getLookupKey(){
        return currentLookupKey.get();
    }

    public void resetToDefault(){
        currentLookupKey.remove();
    }

    public void appendDataSource(Object lookupKey, DataSource dataSource) {
        if (includedDataSource.get(lookupKey) != null)
            throw new IllegalArgumentException("datasource lookup key [" + lookupKey + "] has existed!");
        includedDataSource.put(lookupKey, dataSource);
    }

    public DataSource getDataSource(Object lookupKey){
        return includedDataSource.get(lookupKey);
    }

    public DataSource removeDataSource(Object lookupKey){
        if(Objects.equals("master", lookupKey))
            throw new IllegalArgumentException("master datasource could not be removed!");
        return includedDataSource.remove(lookupKey);
    }

    @Override
    public void destroy() {

    }

    @Nullable
    @Getter
    private DataSource masterDataSource;

    private boolean lenientFallback = true;

    private DataSourceLookup dataSourceLookup = new JndiDataSourceLookup();

    public void addIncludedDataSource(Map<Object, DataSource> includedDataSource) {
        includedDataSource.forEach(this::appendDataSource);
    }

    public void setMasterDataSource(DataSource masterDataSource) {
        includedDataSource.put("master", masterDataSource);
        this.masterDataSource = masterDataSource;
    }

    public void setLenientFallback(boolean lenientFallback) {
        this.lenientFallback = lenientFallback;
    }

    public void setDataSourceLookup(@Nullable DataSourceLookup dataSourceLookup) {
        this.dataSourceLookup = (dataSourceLookup != null ? dataSourceLookup : new JndiDataSourceLookup());
    }


    protected Object resolveSpecifiedLookupKey(Object lookupKey) {
        return lookupKey;
    }

    protected DataSource resolveSpecifiedDataSource(Object dataSource) throws IllegalArgumentException {
        if (dataSource instanceof DataSource) {
            return (DataSource) dataSource;
        }
        else if (dataSource instanceof String) {
            return this.dataSourceLookup.getDataSource((String) dataSource);
        }
        else {
            throw new IllegalArgumentException(
                    "Illegal data source value - only [javax.sql.DataSource] and String supported: " + dataSource);
        }
    }


    @Override
    public Connection getConnection() throws SQLException {
        return determineTargetDataSource().getConnection();
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return determineTargetDataSource().getConnection(username, password);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T unwrap(Class<T> iface) throws SQLException {
        if (iface.isInstance(this)) {
            return (T) this;
        }
        return determineTargetDataSource().unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return (iface.isInstance(this) || determineTargetDataSource().isWrapperFor(iface));
    }

    protected DataSource determineTargetDataSource() {
        Assert.notNull(this.includedDataSource, "DataSource router not initialized");
        Object lookupKey = determineCurrentLookupKey();
        DataSource dataSource = this.includedDataSource.get(lookupKey);
        if (dataSource == null && (this.lenientFallback || lookupKey == null)) {
            dataSource = this.masterDataSource;
        }
        if (dataSource == null) {
            throw new IllegalStateException("Cannot determine target DataSource for lookup key [" + lookupKey + "]");
        }
        return dataSource;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // this.resolvedDataSources = new HashMap<>(this.targetDataSources.size());
        // assert resolvedDataSources != null;
        // this.includedDataSource.forEach((key, value) -> {
        //     Object lookupKey = resolveSpecifiedLookupKey(key);
        //     DataSource dataSource = resolveSpecifiedDataSource(value);
        // });
        // if (this.defaultDataSource != null) {
        //     this.defaultDataSource = resolveSpecifiedDataSource(this.defaultDataSource);
        // }
    }
}
