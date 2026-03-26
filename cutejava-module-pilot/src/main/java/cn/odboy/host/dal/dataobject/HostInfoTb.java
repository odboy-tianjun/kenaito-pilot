package cn.odboy.host.dal.dataobject;

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

import java.math.BigDecimal;

/**
 * <p>
 * 主机信息
 * </p>
 *
 * @author codegen
 * @since 2026-03-27
 */
@Getter
@Setter
@ToString
@TableName("host_info")
@ApiModel(value = "HostInfoTb对象", description = "主机信息")
public class HostInfoTb extends KitBaseUserTimeTb {

    /**
     * id
     */
    @ApiModelProperty("id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 主机名
     */
    @TableField("hostname")
    @ApiModelProperty("主机名")
    private String hostname;

    /**
     * 主机ip
     */
    @TableField("host_ip")
    @ApiModelProperty("主机ip")
    private String hostIp;

    /**
     * 集群编码
     */
    @ApiModelProperty("集群编码")
    @TableField("cluster_code")
    private String clusterCode;

    /**
     * 部署应用类型。比如：java
     */
    @TableField("deploy_app_type")
    @ApiModelProperty("部署应用类型。比如：java")
    private String deployAppType;

    /**
     * cpu大小(auto)
     */
    @TableField("cpu_size")
    @ApiModelProperty("cpu大小(auto)")
    private Long cpuSize;

    /**
     * 内存大小(auto)
     */
    @TableField("memory_size")
    @ApiModelProperty("内存大小(auto)")
    private Long memorySize;

    /**
     * 磁盘容量(auto)
     */
    @TableField("disk_size")
    @ApiModelProperty("磁盘容量(auto)")
    private BigDecimal diskSize;

    /**
     * 处理器类型(auto)。uname -m
     */
    @TableField("machine")
    @ApiModelProperty("处理器类型(auto)。uname -m")
    private String machine;

    /**
     * 内核版本号(auto)。uname -r
     */
    @TableField("`release`")
    @ApiModelProperty("内核版本号(auto)。uname -r")
    private String release;

    /**
     * 操作系统(auto)。cat /etc/redhat-release
     */
    @TableField("`system`")
    @ApiModelProperty("操作系统(auto)。cat /etc/redhat-release")
    private String system;

    /**
     * 是否启用
     */
    @TableField("`status`")
    @ApiModelProperty("是否启用")
    private Boolean status;
}
