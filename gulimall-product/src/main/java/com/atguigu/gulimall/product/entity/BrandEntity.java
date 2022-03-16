package com.atguigu.gulimall.product.entity;

import com.atguigu.common.valid.AddGroup;
import com.atguigu.common.valid.ListValue;
import com.atguigu.common.valid.UpdateGroup;
import com.atguigu.common.valid.UpdateStatusGroup;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Pattern;

/**
 * 品牌
 * 
 * @author lzp
 * @email lzp@gmail.com
 * @date 2022-03-09 09:38:07
 *
 * 分组校验的默认校验
 * 这里要是指定了分组，实体类上的注解就是指定了分组的注解才生效，
 * 没有指定分组的默认不生效，要是没有指定分组，就是对没有指定分组的注解生效，指定分组的注解就不生效了
 */
@Data
@TableName("pms_brand")
public class BrandEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 品牌id
	 */
	@TableId
	@NotNull(message = "修改必须定制品牌id", groups = {UpdateGroup.class,UpdateStatusGroup.class})
	@Null(message = "新增不能指定id", groups = {AddGroup.class})
	private Long brandId;
	/**
	 * 品牌名
	 * 添加JSR303校验注解，只是添加注解没用，
	 * 还需要让springmvc知道这个字段需要校验（在对应接口参数添加@Valid注解）
	 */
	@NotBlank(message = "品牌名必须提交", groups = {AddGroup.class, UpdateGroup.class})
	private String name;
	/**
	 * 品牌logo地址
	 */
	//@URL(message = "logo必须是一个合法的URL地址")
	private String logo;
	/**
	 * 介绍
	 */
	private String descript;
	/**
	 * 显示状态[0-不显示；1-显示]
	 * 也可以使用正则表达式校验@Pattern
	 */
	@NotNull(groups = {AddGroup.class, UpdateStatusGroup.class})
	@ListValue(vals = {0,1}, groups = {AddGroup.class, UpdateStatusGroup.class})
	private Integer showStatus;
	/**
	 * 检索首字母
	 */
	@NotBlank(message="检索首字母不能为空",groups = {AddGroup.class, UpdateGroup.class})
	@Pattern(regexp = "^[a-zA-Z]$", message = "检索首字母必须是一个字母",groups = {AddGroup.class, UpdateGroup.class})
	private String firstLetter;
	/**
	 * 排序
	 */
	private Integer sort;

}
