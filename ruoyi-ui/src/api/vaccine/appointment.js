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
    url: "/vaccine/information/" + id, // ⭐️ 注意：这里是/vaccine/information/
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
export function getPendingCount() {
  return request({
    url: "/vaccine/appointment/pendingCount",
    method: "get",
  });
}
