package com.qz.sns.sv.mapper;
import com.qz.sns.model.entity.Group;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import io.lettuce.core.dynamic.annotation.Param;
import java.util.List;

/**
 * <p>
 * 群 Mapper 接口
 * </p>
 *
 * @author 李彬
 * @since 2025-02-18
 */
@Mapper
public interface GroupMapper extends BaseMapper<Group> {

}
