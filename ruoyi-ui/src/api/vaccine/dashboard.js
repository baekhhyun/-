import request from "@/utils/request";

/**
 * 首页API接口
 * 包含：统计数据、趋势图、排行榜等
 */

// 1. 获取首页统计数据
export function getDashboardStats() {
  return request({
    url: "/vaccine/dashboard/stats",
    method: "get",
  });
}

// 2. 获取预约趋势数据
export function getTrendData(params) {
  return request({
    url: "/vaccine/dashboard/trend",
    method: "get",
    params: params,
  });
}

// 3. 获取疫苗预约排行榜
export function getVaccineRanking() {
  return request({
    url: "/vaccine/dashboard/ranking",
    method: "get",
  });
}

// 4. 获取待处理预约列表（用于管理员首页）
export function getPendingList(params) {
  return request({
    url: "/vaccine/appointment/list",
    method: "get",
    params: {
      ...params,
      status: "0", // 只查待确认的
    },
  });
}

// 5. 获取我的预约（用于用户首页）
export function getMyAppointments(params) {
  return request({
    url: "/vaccine/appointment/myList",
    method: "get",
    params: {
      ...params,
      pageNum: 1,
      pageSize: 3, // 只取3条
    },
  });
}

// 6. 获取系统公告
export function getNotices(params) {
  return request({
    url: "/system/notice/list",
    method: "get",
    params: params,
  });
}
