package cn.odboy.kubernetes.dal.dataobject;

import cn.odboy.base.KitBaseUserTimeTb;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


/**
 * <p>
 * K8s Service 负载均衡与服务发现
 * </p>
 *
 * @author codegen
 * @since 2026-03-27
 */
@Getter
@Setter
@ToString
@TableName("k8s_service")
@ApiModel(value = "K8sServiceTb对象", description = "K8s Service 负载均衡与服务发现")
public class K8sServiceTb extends KitBaseUserTimeTb {

    /**
     * id
     */
    @ApiModelProperty("id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * Service名称。命名规则：{appName}-svc
     */
    @TableField("`name`")
    @ApiModelProperty("Service名称。命名规则：{appName}-svc")
    private String name;

    /**
     * 命名空间。上下文名称
     */
    @TableField("namespace")
    @ApiModelProperty("命名空间。上下文名称")
    private String namespace;

    /**
     * 适用环境。daily、stage、production
     */
    @TableField("env_code")
    @ApiModelProperty("适用环境。daily、stage、production")
    private String envCode;

    /**
     * Service类型。ClusterIP、NodePort
     */
    @TableField("service_type")
    @ApiModelProperty("Service类型。ClusterIP、NodePort")
    private String serviceType;

    /**
     * 应用服务端口
     */
    @ApiModelProperty("应用服务端口")
    @TableField("service_port")
    private Integer servicePort;
}
