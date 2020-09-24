package ink.andromeda.dataflow.datasource.mapper;

import ink.andromeda.dataflow.datasource.Sources;
import ink.andromeda.dataflow.datasource.SwitchSource;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
@Mapper()
public interface HaierOracleMapper {

    // ========================= lm_loan

    @SwitchSource(Sources.SLAVE)
    @Select(value = {"SELECT * FROM (\n" +
            "    SELECT ROWNUM row_num, t.* FROM (\n" +
            "        SELECT * FROM GLLOANS_HAIERDB.V_LM_LOAN \n" +
            "    ) t WHERE ROWNUM <= #{page} * #{size}\n" +
            ") tt WHERE tt.row_num > (#{page} - 1) * #{size}"})
    @Options(flushCache = Options.FlushCachePolicy.TRUE, useCache = false)
    List<Map<String, Object>> selectLmLoanWithPageable(@Param("page") int page, @Param("size") int size);

    @SwitchSource(Sources.SLAVE)
    @Options(flushCache = Options.FlushCachePolicy.TRUE, useCache = false)
    @Select("SELECT COUNT(1) FROM GLLOANS_HAIERDB.V_LM_LOAN")
    long selectLmLoanTotalCount();

    // ========================= lm_pm_shd

    @SwitchSource(Sources.SLAVE)
    @Select("SELECT * FROM (\n" +
            "    SELECT ROWNUM row_num, t.* FROM (\n" +
            "        SELECT * FROM GLLOANS_HAIERDB.lm_pm_shd \n" +
            "    ) t WHERE ROWNUM <= #{page} * #{size}\n" +
            ") tt WHERE tt.row_num > (#{page} - 1) * #{size}")
    @Options(flushCache = Options.FlushCachePolicy.TRUE, useCache = false)
    List<Map<String, Object>> selectLmPmShdWithPageable(@Param("page") int page, @Param("size") int size);

    @SwitchSource(Sources.SLAVE)
    @Select("SELECT * FROM GLLOANS_HAIERDB.lm_pm_shd WHERE LOAN_NO = #{loanNo}")
    @Options(flushCache = Options.FlushCachePolicy.TRUE, useCache = false)
    List<Map<String, Object>> selectLmPmShdByLoanNo(String loanNo);

    @SwitchSource(Sources.SLAVE)
    @Select("SELECT COUNT(1) FROM GLLOANS_HAIERDB.lm_pm_shd")
    @Options(flushCache = Options.FlushCachePolicy.TRUE, useCache = false)
    long selectLmPmShdTotalCount();

    // ========================= lm_setlmt_log

    @SwitchSource(Sources.SLAVE)
    @Select("SELECT * FROM (\n" +
            "    SELECT ROWNUM row_num, t.* FROM (\n" +
            "        SELECT * FROM GLLOANS_HAIERDB.lm_setlmt_log \n" +
            "    ) t WHERE ROWNUM <= #{page} * #{size}\n" +
            ") tt WHERE tt.row_num > (#{page} - 1) * #{size}")
    @Options(flushCache = Options.FlushCachePolicy.TRUE, useCache = false)
    List<Map<String, Object>> selectLmSetlmtLogWithPageable(@Param("page") int page, @Param("size") int size);

    @SwitchSource(Sources.SLAVE)
    @Select("SELECT COUNT(1) FROM GLLOANS_HAIERDB.lm_setlmt_log")
    @Options(flushCache = Options.FlushCachePolicy.TRUE, useCache = false)
    long selectLmSetlmtLogTotalCount();

    @SwitchSource(Sources.SLAVE)
    @Select("SELECT * FROM GLLOANS_HAIERDB.lm_setlmt_log WHERE LOAN_NO = #{loanNo}")
    @Options(flushCache = Options.FlushCachePolicy.TRUE, useCache = false)
    List<Map<String, Object>> selectLmSetlmtLogByLoanNo(String loanNo);

    // ========================= lm_pm_log

    @SwitchSource(Sources.SLAVE)
    @Select("SELECT * FROM (\n" +
            "    SELECT ROWNUM row_num, t.* FROM (\n" +
            "        SELECT * FROM GLLOANS_HAIERDB.lm_pm_log \n" +
            "    ) t WHERE ROWNUM <= #{page} * #{size}\n" +
            ") tt WHERE tt.row_num > (#{page} - 1) * #{size}")
    @Options(flushCache = Options.FlushCachePolicy.TRUE, useCache = false)
    List<Map<String, Object>> selectLmPmLogWithPageable(@Param("page") int page, @Param("size") int size);

    @SwitchSource(Sources.SLAVE)
    @Select("SELECT COUNT(1) FROM GLLOANS_HAIERDB.lm_pm_log")
    @Options(flushCache = Options.FlushCachePolicy.TRUE, useCache = false)
    long selectLmPmLogTotalCount();

    @SwitchSource(Sources.SLAVE)
    @Select("SELECT * FROM GLLOANS_HAIERDB.lm_pm_log WHERE LOAN_NO = #{loanNo}")
    @Options(flushCache = Options.FlushCachePolicy.TRUE, useCache = false)
    List<Map<String, Object>> selectLmPmLogByLoanNo(String loanNo);

}
