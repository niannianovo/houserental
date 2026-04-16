package com.example.mapper;

import com.example.vo.AreaDistributionVO;
import com.example.vo.TrendVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DashboardMapper {

    @Select("SELECT COUNT(*) FROM user WHERE is_admin = 0")
    Integer countUsers();

    @Select("SELECT COUNT(*) FROM house")
    Integer countHouses();

    @Select("SELECT COUNT(*) FROM house WHERE verify_status = 0")
    Integer countPendingVerify();

    @Select("SELECT COUNT(*) FROM rental_order WHERE DATE_FORMAT(create_time, '%Y-%m') = #{month}")
    Integer countMonthlyOrders(@Param("month") String month);
}
