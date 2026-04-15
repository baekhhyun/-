import request from "@/utils/request";

export function listConsultation(query) {
  return request({
    url: "/vaccine/consultation/list",
    method: "get",
    params: query,
  });
}

export function listMyConsultation(query) {
  return request({
    url: "/vaccine/consultation/myList",
    method: "get",
    params: query,
  });
}

export function addConsultation(data) {
  return request({
    url: "/vaccine/consultation",
    method: "post",
    data,
  });
}

export function replyConsultation(data) {
  return request({
    url: "/vaccine/consultation/reply",
    method: "put",
    data,
  });
}

export function delConsultation(ids) {
  return request({
    url: "/vaccine/consultation/" + ids,
    method: "delete",
  });
}
