import request from '@/utils/request'

// 查询疫苗信息列表
export function listVaccine(query) {
  return request({
    url: '/vaccine/vaccine/list',
    method: 'get',
    params: query
  })
}

// 查询疫苗信息详细
export function getVaccine(id) {
  return request({
    url: '/vaccine/vaccine/' + id,
    method: 'get'
  })
}

// 新增疫苗信息
export function addVaccine(data) {
  return request({
    url: '/vaccine/vaccine',
    method: 'post',
    data: data
  })
}

// 修改疫苗信息
export function updateVaccine(data) {
  return request({
    url: '/vaccine/vaccine',
    method: 'put',
    data: data
  })
}

// 删除疫苗信息
export function delVaccine(id) {
  return request({
    url: '/vaccine/vaccine/' + id,
    method: 'delete'
  })
}
