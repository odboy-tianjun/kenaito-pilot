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
 * K8s Ingress 反向代理
 * </p>
 *
 * @author codegen
 * @since 2026-03-27
 */
@Getter
@Setter
@ToString
@TableName("k8s_ingress")
@ApiModel(value = "K8sIngressTb对象", description = "K8s Ingress 反向代理")
public class K8sIngressTb extends KitBaseUserTimeTb {

    /**
     * id
     */
    @ApiModelProperty("id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * Ingress名称。命名规则：{appName}-ing
     */
    @TableField("`name`")
    @ApiModelProperty("Ingress名称。命名规则：{appName}-ing")
    private String name;

    /**
     * 域名
     */
    @TableField("`host`")
    @ApiModelProperty("域名")
    private String host;

    /**
     * 匹配路径。默认为'/'
     */
    @TableField("`path`")
    @ApiModelProperty("匹配路径。默认为'/'")
    private String path;

    /**
     * Ingress路由规则中的路径类型(Exact、Prefix、ImplementationSpecific
     */
    @TableField("path_type")
    @ApiModelProperty("Ingress路由规则中的路径类型(Exact、Prefix、ImplementationSpecific")
    private String pathType;

    /**
     * 目标Service
     */
    @TableField("service_name")
    @ApiModelProperty("目标Service")
    private String serviceName;

    /**
     * 服务端口。指向Service的应用服务端口
     */
    @TableField("service_port")
    @ApiModelProperty("服务端口。指向Service的应用服务端口")
    private Integer servicePort;
}
