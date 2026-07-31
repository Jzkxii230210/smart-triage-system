package com.zjsru.mapper;
import com.zjsru.entity.vo.DashboardVO;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface DashboardMapper {
    // 科室信息查询
    List<DashboardVO.DeptVO> selectDepts();

    // 公告信息查询
    List<DashboardVO.NoticeVO> selectNotices();

    // 推荐医生信息查询
    List<DashboardVO.DoctorVO> selectRecommendDoctors();
}
