package ink.andromeda.dataflow.server.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("HTTP请求返回结果")
public class HttpResult<R> {

    @ApiModelProperty("返回信息")
    private String msg;

    @ApiModelProperty("返回码")
    private String code;

    @ApiModelProperty("是否成功")
    private boolean success;

    @ApiModelProperty("返回数据")
    private R data;

    public static String DEFAULT_SUCCESS_CODE = "Y0000";

    public static String DEFAULT_FAILED_CODE = "E0000";

    public static HttpResult<?> FAILED = new HttpResult<>(null, DEFAULT_FAILED_CODE, false, null);

    public static <Z> HttpResult<Z> FAILED(String msg){
        return new HttpResult<>(msg, DEFAULT_FAILED_CODE, false, null);
    }
    
    public static <Z> HttpResult<Z> FAILED(String msg, Z data){
        return new HttpResult<>(msg, DEFAULT_FAILED_CODE, false, data);
    }

    public static HttpResult<?> SUCCESS = new HttpResult<>(null, DEFAULT_SUCCESS_CODE, true, null);

    public static <Z> HttpResult<Z> SUCCESS(Z data){
        return new HttpResult<>(null, DEFAULT_SUCCESS_CODE, true, data);
    }

    public static <Z> HttpResult<Z> SUCCESS(String msg){
        return new HttpResult<>(msg, DEFAULT_SUCCESS_CODE, true, null);
    }

    public static <Z> HttpResult<Z> SUCCESS(String msg, Z data){
        return new HttpResult<>(msg, DEFAULT_SUCCESS_CODE, true, data);
    }

}
