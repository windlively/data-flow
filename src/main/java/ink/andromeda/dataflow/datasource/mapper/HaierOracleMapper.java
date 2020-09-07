package ink.andromeda.dataflow.datasource.mapper;

import com.alibaba.fastjson.JSONObject;
import net.abakus.coresystem.dynamicdatasource.Sources;
import net.abakus.coresystem.dynamicdatasource.SwitchSource;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Mapper()
public interface HaierOracleMapper {

    // ========================= lm_loan

    @SwitchSource(Sources.HAIER)
    @Select(value = {"SELECT * FROM (\n" +
            "    SELECT ROWNUM row_num, t.* FROM (\n" +
            "        SELECT * FROM GLLOANS_HAIERDB.V_LM_LOAN \n" +
            "    ) t WHERE ROWNUM <= #{page} * #{size}\n" +
            ") tt WHERE tt.row_num > (#{page} - 1) * #{size}"})
    @Options(flushCache = Options.FlushCachePolicy.TRUE, useCache = false)
    List<JSONObject> selectLmLoanWithPageable(@Param("page") int page, @Param("size") int size);

    @SwitchSource(Sources.HAIER)
    @Options(flushCache = Options.FlushCachePolicy.TRUE, useCache = false)
    @Select("SELECT COUNT(1) FROM GLLOANS_HAIERDB.V_LM_LOAN")
    long selectLmLoanTotalCount();

    // ========================= lm_pm_shd

    @SwitchSource(Sources.HAIER)
    @Select("SELECT * FROM (\n" +
            "    SELECT ROWNUM row_num, t.* FROM (\n" +
            "        SELECT * FROM GLLOANS_HAIERDB.lm_pm_shd \n" +
            "    ) t WHERE ROWNUM <= #{page} * #{size}\n" +
            ") tt WHERE tt.row_num > (#{page} - 1) * #{size}")
    @Options(flushCache = Options.FlushCachePolicy.TRUE, useCache = false)
    List<JSONObject> selectLmPmShdWithPageable(@Param("page") int page, @Param("size") int size);

    @SwitchSource(Sources.HAIER)
    @Select("SELECT * FROM GLLOANS_HAIERDB.lm_pm_shd WHERE LOAN_NO = #{loanNo}")
    @Options(flushCache = Options.FlushCachePolicy.TRUE, useCache = false)
    List<JSONObject> selectLmPmShdByLoanNo(String loanNo);

    @SwitchSource(Sources.HAIER)
    @Select("SELECT COUNT(1) FROM GLLOANS_HAIERDB.lm_pm_shd")
    @Options(flushCache = Options.FlushCachePolicy.TRUE, useCache = false)
    long selectLmPmShdTotalCount();

    // ========================= lm_setlmt_log

    @SwitchSource(Sources.HAIER)
    @Select("SELECT * FROM (\n" +
            "    SELECT ROWNUM row_num, t.* FROM (\n" +
            "        SELECT * FROM GLLOANS_HAIERDB.lm_setlmt_log \n" +
            "    ) t WHERE ROWNUM <= #{page} * #{size}\n" +
            ") tt WHERE tt.row_num > (#{page} - 1) * #{size}")
    @Options(flushCache = Options.FlushCachePolicy.TRUE, useCache = false)
    List<JSONObject> selectLmSetlmtLogWithPageable(@Param("page") int page, @Param("size") int size);

    @SwitchSource(Sources.HAIER)
    @Select("SELECT COUNT(1) FROM GLLOANS_HAIERDB.lm_setlmt_log")
    @Options(flushCache = Options.FlushCachePolicy.TRUE, useCache = false)
    long selectLmSetlmtLogTotalCount();

    @SwitchSource(Sources.HAIER)
    @Select("SELECT * FROM GLLOANS_HAIERDB.lm_setlmt_log WHERE LOAN_NO = #{loanNo}")
    @Options(flushCache = Options.FlushCachePolicy.TRUE, useCache = false)
    List<JSONObject> selectLmSetlmtLogByLoanNo(String loanNo);

    // ========================= lm_pm_log

    @SwitchSource(Sources.HAIER)
    @Select("SELECT * FROM (\n" +
            "    SELECT ROWNUM row_num, t.* FROM (\n" +
            "        SELECT * FROM GLLOANS_HAIERDB.lm_pm_log \n" +
            "    ) t WHERE ROWNUM <= #{page} * #{size}\n" +
            ") tt WHERE tt.row_num > (#{page} - 1) * #{size}")
    @Options(flushCache = Options.FlushCachePolicy.TRUE, useCache = false)
    List<JSONObject> selectLmPmLogWithPageable(@Param("page") int page, @Param("size") int size);

    @SwitchSource(Sources.HAIER)
    @Select("SELECT COUNT(1) FROM GLLOANS_HAIERDB.lm_pm_log")
    @Options(flushCache = Options.FlushCachePolicy.TRUE, useCache = false)
    long selectLmPmLogTotalCount();

    @SwitchSource(Sources.HAIER)
    @Select("SELECT * FROM GLLOANS_HAIERDB.lm_pm_log WHERE LOAN_NO = #{loanNo}")
    @Options(flushCache = Options.FlushCachePolicy.TRUE, useCache = false)
    List<JSONObject> selectLmPmLogByLoanNo(String loanNo);

}
