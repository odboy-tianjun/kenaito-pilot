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

import java.math.BigDecimal;

/**
 * <p>
 * K8s Pod 规格
 * </p>
 *
 * @author codegen
 * @since 2026-03-27
 */
@Getter
@Setter
@ToString
@TableName("k8s_pod_specs")
@ApiModel(value = "K8sPodSpecsTb对象", description = "K8s Pod 规格")
public class K8sPodSpecsTb extends KitBaseUserTimeTb {

    /**
     * id
     */
    @ApiModelProperty("id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 规格名称
     */
    @TableField("`name`")
    @ApiModelProperty("规格名称")
    private String name;

    /**
     * CPU核心数。毫核m
     */
    @TableField("cpu_size")
    @ApiModelProperty("CPU核心数。毫核m")
    private BigDecimal cpuSize;

    /**
     * 内存数。单位Mi
     */
    @TableField("memory_size")
    @ApiModelProperty("内存数。单位Mi")
    private Long memorySize;

    /**
     * 磁盘容量。单位Mi
     */
    @TableField("disk_size")
    @ApiModelProperty("磁盘容量。单位Mi")
    private Long diskSize;

    /**
     * 是否启用
     */
    @TableField("`status`")
    @ApiModelProperty("是否启用")
    private Boolean status;
}
