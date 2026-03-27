import request from "@/utils/request";

// 查询疫苗预约列表（管理员用）
export function listAppointment(query) {
  return request({
    url: "/vaccine/appointment/list",
    method: "get",
    params: query,
  });
}

// 查询疫苗预约详细
export function getAppointment(id) {
  return request({
    url: "/vaccine/appointment/" + id,
    method: "get",
  });
}

// 新增疫苗预约（用户提交）
export function addAppointment(data) {
  return request({
    url: "/vaccine/appointment",
    method: "post",
    data: data,
  });
}

// 修改疫苗预约
export function updateAppointment(data) {
  return request({
    url: "/vaccine/appointment",
    method: "put",
    data: data,
  });
}

// 删除疫苗预约
export function delAppointment(ids) {
  // 如果后端接受数组
  return request({
    url: "/vaccine/appointment/" + ids, // 如果后端是 @DeleteMapping("/{ids}")
    method: "delete",
  });
}

// ============ 用户端API ============

// 获取我的预约列表
export function listMyAppointment(query) {
  return request({
    url: "/vaccine/appointment/myList",
    method: "get",
    params: query,
  });
}

// 取消预约
export function cancelAppointment(id) {
  return request({
    url: "/vaccine/appointment/cancel/" + id,
    method: "put",
  });
}

// 检查时间段可用性
export function checkTimeSlot(query) {
  return request({
    url: "/vaccine/appointment/checkTimeSlot",
    method: "get",
    params: query,
  });
}

// 获取疫苗信息（使用疫苗模块的API）
export function getVaccineInfo(id) {
  return request({
    url: "/vaccine/information/" + id,
    method: "get",
  });
}

// 获取时间段剩余名额
export function getTimeSlotRemaining(appointmentDate) {
  return request({
    url: "/vaccine/appointment/getTimeSlotRemaining",
    method: "get",
    params: { appointmentDate: appointmentDate },
  });
}

// 获取待处理预约数量（管理员端小红点）
export function getPendingCount() {
  return request({
    url: "/vaccine/appointment/pendingCount",
    method: "get",
  });
}

// ============  多剂次疫苗新增接口 ============

/**
 * 确认预约（待确认 → 已确认）
 * @param {Long} id 预约ID
 */
export function confirmAppointment(id) {
  return request({
    url: "/vaccine/appointment/confirm/" + id,
    method: "put",
  });
}

/**
 * 完成接种（已确认 → 已完成）
 * @param {Long} id 预约ID
 */
export function completeAppointment(id) {
  return request({
    url: "/vaccine/appointment/complete/" + id,
    method: "put",
  });
}

/**
 * 获取用户某疫苗的接种进度
 * @param {Long} vaccineId 疫苗ID
 */
export function getMyVaccineProgress(vaccineId) {
  return request({
    url: "/vaccine/appointment/myProgress",
    method: "get",
    params: { vaccineId: vaccineId },
  });
}

/**
 * 获取用户待接种提醒列表
 */
export function getMyPendingDoses() {
  return request({
    url: "/vaccine/appointment/myPendingDoses",
    method: "get",
  });
}

/**
 * 获取今日待接种提醒（管理员用）
 */
export function getTodayReminders() {
  return request({
    url: "/vaccine/appointment/todayReminders",
    method: "get",
  });
}

/**
 * 获取疫苗消耗统计（用于仪表盘图表）
 */
export function getConsumptionStats() {
  return request({
    url: "/vaccine/appointment/consumptionStats",
    method: "get",
  });
}

/**
 * 获取接种覆盖率统计
 */
export function getCoverageStats() {
  return request({
    url: "/vaccine/appointment/coverageStats",
    method: "get",
  });
}

/**
 * 获取各剂次完成情况统计
 * @param {Long} vaccineId 疫苗ID
 */
export function getDoseStats(vaccineId) {
  return request({
    url: "/vaccine/appointment/doseStats/" + vaccineId,
    method: "get",
  });
}

/**
 * 获取预约趋势（按日期统计，支持多剂次区分）
 * @param {String} beginDate 开始日期
 * @param {String} endDate 结束日期
 */
export function getAppointmentTrend(beginDate, endDate) {
  return request({
    url: "/vaccine/appointment/trend",
    method: "get",
    params: { beginDate: beginDate, endDate: endDate },
  });
}

/**
 * 获取疫苗总预约数
 * @param {Long} vaccineId 疫苗ID
 */
export function getTotalBooked(vaccineId) {
  return request({
    url: "/vaccine/appointment/totalBooked/" + vaccineId,
    method: "get",
  });
}
