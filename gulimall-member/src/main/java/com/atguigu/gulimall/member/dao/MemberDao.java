package com.atguigu.gulimall.member.dao;

import com.atguigu.gulimall.member.entity.MemberEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员
 * 
 * @author lzp
 * @email lzp@gmail.com
 * @date 2022-03-09 11:01:56
 */
@Mapper
public interface MemberDao extends BaseMapper<MemberEntity> {
	
}
