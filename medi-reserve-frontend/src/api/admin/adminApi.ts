/* eslint-disable */
/* tslint:disable */
// @ts-nocheck
/*
 * ---------------------------------------------------------------
 * ## THIS FILE WAS GENERATED VIA SWAGGER-TYPESCRIPT-API        ##
 * ##                                                           ##
 * ## AUTHOR: acacode                                           ##
 * ## SOURCE: https://github.com/acacode/swagger-typescript-api ##
 * ---------------------------------------------------------------
 */

export interface ResultVoid {
  /** @format int32 */
  code?: number;
  msg?: string;
  data?: object;
}

/** 角色权限更新请求 DTO */
export interface RolePermissionUpdateDTO {
  /** 权限ID列表 */
  permissionIds: number[];
}

/** 密码修改请求 DTO */
export interface PasswordUpdateDTO {
  /** 旧密码 */
  oldPassword: string;
  /**
   * 新密码（6-20位字母和数字组合）
   * @pattern ^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d]{6,20}$
   */
  newPassword: string;
  /** 确认密码 */
  confirmPassword: string;
}

/** 管理员注册请求 DTO */
export interface AdminRegisterDTO {
  /**
   * 用户名（4-20位字母数字下划线）
   * @pattern ^[a-zA-Z0-9_]{4,20}$
   */
  username: string;
  /**
   * 密码（6-20位字母和数字组合）
   * @pattern ^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d]{6,20}$
   */
  password: string;
  /** 真实姓名 */
  name: string;
  /**
   * 手机号（11位）
   * @pattern ^1[3-9]\d{9}$
   * @example "13800138000"
   */
  phone?: string;
  /**
   * 邮箱
   * @pattern ^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$
   * @example "admin@example.com"
   */
  email?: string;
  /**
   * 角色（1=超级管理员，2=普通管理员），默认普通管理员
   * @format int32
   */
  role?: number;
}

export interface ResultMapStringObject {
  /** @format int32 */
  code?: number;
  msg?: string;
  data?: Record<string, object>;
}

/** 通用登录请求 DTO（患者/医生使用手机号，管理员使用用户名） */
export interface LoginDTO {
  /** 登录账号（手机号或用户名） */
  username: string;
  /** 密码 */
  password: string;
}

export interface ResultString {
  /** @format int32 */
  code?: number;
  msg?: string;
  data?: string;
}

/** 证件审核请求 DTO（管理端） */
export interface CertificateAuditDTO {
  /**
   * 审核结果：1-通过，2-驳回
   * @format int32
   */
  result: 1 | 2;
  /** 驳回原因（驳回时必填） */
  remark?: string;
}

/** 审核驳回请求 DTO */
export interface AuditRejectDTO {
  /** 驳回原因 */
  rejectReason: string;
}

/** 待审核医生证件信息 VO（管理端） */
export interface PendingCertAuditVO {
  /**
   * 医生ID
   * @format int64
   */
  doctorId?: number;
  /** 医生姓名 */
  doctorName?: string;
  /** 科室名称 */
  departmentName?: string;
  /** 职称名称 */
  titleName?: string;
  /** 当前生效的执业证书URL */
  currentCertificateUrl?: string;
  /** 当前生效的资格证URL */
  currentQualificationUrl?: string;
  /** 待审核的执业证书URL（新上传） */
  pendingCertificateUrl?: string;
  /** 待审核的资格证URL（新上传） */
  pendingQualificationUrl?: string;
  /**
   * 提交时间
   * @format date-time
   */
  submittedAt?: string;
}

export interface ResultPendingCertAuditVO {
  /** @format int32 */
  code?: number;
  msg?: string;
  /** 待审核医生证件信息 VO（管理端） */
  data?: PendingCertAuditVO;
}

/** 权限树节点 VO */
export interface PermissionNodeVO {
  /**
   * 权限ID
   * @format int64
   */
  id?: number;
  /** 权限代码（如 admin:audit:view） */
  code?: string;
  /** 权限名称 */
  name?: string;
  /**
   * 类型：1-菜单，2-按钮，3-接口
   * @format int32
   */
  type?: number;
  /**
   * 排序
   * @format int32
   */
  sortOrder?: number;
  /** 子权限列表 */
  children?: PermissionNodeVO[];
}

export interface ResultListPermissionNodeVO {
  /** @format int32 */
  code?: number;
  msg?: string;
  data?: PermissionNodeVO[];
}

export interface ResultListRoleVO {
  /** @format int32 */
  code?: number;
  msg?: string;
  data?: RoleVO[];
}

/** 角色信息 VO */
export interface RoleVO {
  /**
   * 角色ID
   * @format int32
   */
  id?: number;
  /** 角色名称 */
  name?: string;
  /** 角色描述 */
  description?: string;
  /** 拥有的权限ID列表 */
  permissionIds?: number[];
}

export interface ResultListLong {
  /** @format int32 */
  code?: number;
  msg?: string;
  data?: number[];
}

/** 操作日志查询条件 DTO */
export interface OperationLogQueryDTO {
  /**
   * 操作管理员ID（精确查询）
   * @format int64
   */
  adminId?: number;
  /** 操作模块（如：审核管理） */
  module?: string;
  /**
   * 操作结果：1-成功，0-失败
   * @format int32
   */
  result?: number;
  /**
   * 开始日期（yyyy-MM-dd）
   * @format date
   */
  startDate?: string;
  /**
   * 结束日期（yyyy-MM-dd）
   * @format date
   */
  endDate?: string;
  /**
   * 页码（从1开始）
   * @format int32
   */
  pageNum?: number;
  /**
   * 每页大小（默认10）
   * @format int32
   */
  pageSize?: number;
}

/** 操作日志响应 VO */
export interface OperationLogVO {
  /**
   * 日志ID
   * @format int64
   */
  id?: number;
  /**
   * 管理员ID
   * @format int64
   */
  adminId?: number;
  /** 管理员姓名 */
  adminName?: string;
  /** 操作模块 */
  module?: string;
  /** 操作描述 */
  operation?: string;
  /** 请求方法 */
  method?: string;
  /** 请求路径 */
  path?: string;
  /** 请求参数（JSON） */
  params?: string;
  /** 客户端IP */
  ip?: string;
  /**
   * 操作结果：1-成功，0-失败
   * @format int32
   */
  result?: number;
  /**
   * HTTP状态码
   * @format int32
   */
  statusCode?: number;
  /** 错误信息（失败时） */
  errorMsg?: string;
  /**
   * 操作耗时（毫秒）
   * @format int32
   */
  durationMs?: number;
  /**
   * 操作时间
   * @format date-time
   */
  createdAt?: string;
}

export interface PageInfoOperationLogVO {
  /** @format int64 */
  total?: number;
  list?: OperationLogVO[];
  /** @format int32 */
  pageNum?: number;
  /** @format int32 */
  pageSize?: number;
  /** @format int32 */
  size?: number;
  /** @format int64 */
  startRow?: number;
  /** @format int64 */
  endRow?: number;
  /** @format int32 */
  pages?: number;
  /** @format int32 */
  prePage?: number;
  /** @format int32 */
  nextPage?: number;
  isFirstPage?: boolean;
  isLastPage?: boolean;
  hasPreviousPage?: boolean;
  hasNextPage?: boolean;
  /** @format int32 */
  navigatePages?: number;
  navigatepageNums?: number[];
  /** @format int32 */
  navigateFirstPage?: number;
  /** @format int32 */
  navigateLastPage?: number;
}

export interface ResultPageInfoOperationLogVO {
  /** @format int32 */
  code?: number;
  msg?: string;
  data?: PageInfoOperationLogVO;
}

export interface ResultOperationLogVO {
  /** @format int32 */
  code?: number;
  msg?: string;
  /** 操作日志响应 VO */
  data?: OperationLogVO;
}

export interface Admin {
  /** @format int64 */
  id?: number;
  username?: string;
  name?: string;
  phone?: string;
  email?: string;
  /** @format int32 */
  role?: number;
  /** @format int32 */
  status?: number;
  lastLoginIp?: string;
  /** @format date-time */
  lastLoginTime?: string;
  /** @format date-time */
  createdAt?: string;
  /** @format date-time */
  updatedAt?: string;
}

export interface PageInfoAdmin {
  /** @format int64 */
  total?: number;
  list?: Admin[];
  /** @format int32 */
  pageNum?: number;
  /** @format int32 */
  pageSize?: number;
  /** @format int32 */
  size?: number;
  /** @format int64 */
  startRow?: number;
  /** @format int64 */
  endRow?: number;
  /** @format int32 */
  pages?: number;
  /** @format int32 */
  prePage?: number;
  /** @format int32 */
  nextPage?: number;
  isFirstPage?: boolean;
  isLastPage?: boolean;
  hasPreviousPage?: boolean;
  hasNextPage?: boolean;
  /** @format int32 */
  navigatePages?: number;
  navigatepageNums?: number[];
  /** @format int32 */
  navigateFirstPage?: number;
  /** @format int32 */
  navigateLastPage?: number;
}

export interface ResultPageInfoAdmin {
  /** @format int32 */
  code?: number;
  msg?: string;
  data?: PageInfoAdmin;
}

export interface DoctorAudit {
  /** @format int64 */
  id?: number;
  /** @format int64 */
  doctorId?: number;
  certificateUrl?: string;
  qualificationUrl?: string;
  pendingCertificateUrl?: string;
  pendingQualificationUrl?: string;
  /** @format int32 */
  certAuditStatus?: number;
  certAuditRemark?: string;
  /** @format date-time */
  certAuditTime?: string;
  /** @format int64 */
  certAuditorId?: number;
  specialty?: string;
  introduction?: string;
  avatar?: string;
  /** @format int32 */
  auditStatus?: number;
  auditRemark?: string;
  /** @format date-time */
  auditTime?: string;
  /** @format int64 */
  auditorId?: number;
  /** @format date-time */
  createdAt?: string;
  /** @format date-time */
  updatedAt?: string;
}

export interface ResultDoctorAudit {
  /** @format int32 */
  code?: number;
  msg?: string;
  data?: DoctorAudit;
}

/** 待审核医生列表返回 VO（管理端） */
export interface DoctorPendingVO {
  /**
   * 医生ID
   * @format int64
   */
  doctorId?: number;
  /** 姓名 */
  name?: string;
  /** 手机号 */
  phone?: string;
  /** 科室名称 */
  departmentName?: string;
  /** 职称名称 */
  titleName?: string;
  /**
   * 注册时间
   * @format date-time
   */
  createdAt?: string;
  /** 擅长领域 */
  specialty?: string;
  /** 个人简介 */
  introduction?: string;
  /** 执业证书URL */
  certificateUrl?: string;
  /** 资格证URL */
  qualificationUrl?: string;
}

export interface PageInfoDoctorPendingVO {
  /** @format int64 */
  total?: number;
  list?: DoctorPendingVO[];
  /** @format int32 */
  pageNum?: number;
  /** @format int32 */
  pageSize?: number;
  /** @format int32 */
  size?: number;
  /** @format int64 */
  startRow?: number;
  /** @format int64 */
  endRow?: number;
  /** @format int32 */
  pages?: number;
  /** @format int32 */
  prePage?: number;
  /** @format int32 */
  nextPage?: number;
  isFirstPage?: boolean;
  isLastPage?: boolean;
  hasPreviousPage?: boolean;
  hasNextPage?: boolean;
  /** @format int32 */
  navigatePages?: number;
  navigatepageNums?: number[];
  /** @format int32 */
  navigateFirstPage?: number;
  /** @format int32 */
  navigateLastPage?: number;
}

export interface ResultPageInfoDoctorPendingVO {
  /** @format int32 */
  code?: number;
  msg?: string;
  data?: PageInfoDoctorPendingVO;
}

export interface ResultListTrendDataVO {
  /** @format int32 */
  code?: number;
  msg?: string;
  data?: TrendDataVO[];
}

/** 每日趋势数据点 */
export interface TrendDataVO {
  /**
   * 日期
   * @format date
   */
  date?: string;
  /**
   * 当日挂号量
   * @format int64
   */
  appointments?: number;
  /**
   * 当日支付量
   * @format int64
   */
  paid?: number;
  /** 当日收入（固定单价估算） */
  income?: number;
}

export interface ResultListStatusDistributionVO {
  /** @format int32 */
  code?: number;
  msg?: string;
  data?: StatusDistributionVO[];
}

/** 预约状态分布 VO */
export interface StatusDistributionVO {
  /**
   * 状态码：0-待支付，1-已支付，2-已就诊，3-已取消，4-已过期
   * @format int32
   */
  status?: number;
  /** 状态描述 */
  label?: string;
  /**
   * 数量
   * @format int64
   */
  count?: number;
}

/** 管理端总览统计数据 VO */
export interface DashboardOverviewVO {
  /**
   * 今日挂号总数（含待支付、已支付、已就诊）
   * @format int64
   */
  todayAppointments?: number;
  /**
   * 今日已支付数
   * @format int64
   */
  todayPaid?: number;
  /** 今日收入（固定单价计算） */
  todayIncome?: number;
  /**
   * 历史总挂号数（有效预约）
   * @format int64
   */
  totalAppointments?: number;
  /** 历史总收入（固定单价估算） */
  totalIncome?: number;
  /**
   * 总患者数
   * @format int64
   */
  totalPatients?: number;
  /**
   * 总医生数（审核通过且启用）
   * @format int64
   */
  totalDoctors?: number;
  /**
   * 总评价数
   * @format int64
   */
  totalEvaluations?: number;
}

export interface ResultDashboardOverviewVO {
  /** @format int32 */
  code?: number;
  msg?: string;
  /** 管理端总览统计数据 VO */
  data?: DashboardOverviewVO;
}

/** 医生排行数据 VO */
export interface DoctorRankingVO {
  /**
   * 医生ID
   * @format int64
   */
  doctorId?: number;
  /** 医生姓名 */
  doctorName?: string;
  /** 科室名称 */
  departmentName?: string;
  /**
   * 预约总数
   * @format int64
   */
  appointmentCount?: number;
  /** 平均评分（5分制） */
  avgScore?: number;
  /**
   * 排名
   * @format int32
   */
  rank?: number;
}

export interface ResultListDoctorRankingVO {
  /** @format int32 */
  code?: number;
  msg?: string;
  data?: DoctorRankingVO[];
}

/** 科室排行数据 VO */
export interface DepartmentRankingVO {
  /**
   * 科室ID
   * @format int64
   */
  departmentId?: number;
  /** 科室名称 */
  departmentName?: string;
  /**
   * 预约总数
   * @format int64
   */
  appointmentCount?: number;
  /** 占比（百分比） */
  ratio?: number;
}

export interface ResultListDepartmentRankingVO {
  /** @format int32 */
  code?: number;
  msg?: string;
  data?: DepartmentRankingVO[];
}

export interface PageInfoPendingCertAuditVO {
  /** @format int64 */
  total?: number;
  list?: PendingCertAuditVO[];
  /** @format int32 */
  pageNum?: number;
  /** @format int32 */
  pageSize?: number;
  /** @format int32 */
  size?: number;
  /** @format int64 */
  startRow?: number;
  /** @format int64 */
  endRow?: number;
  /** @format int32 */
  pages?: number;
  /** @format int32 */
  prePage?: number;
  /** @format int32 */
  nextPage?: number;
  isFirstPage?: boolean;
  isLastPage?: boolean;
  hasPreviousPage?: boolean;
  hasNextPage?: boolean;
  /** @format int32 */
  navigatePages?: number;
  navigatepageNums?: number[];
  /** @format int32 */
  navigateFirstPage?: number;
  /** @format int32 */
  navigateLastPage?: number;
}

export interface ResultPageInfoPendingCertAuditVO {
  /** @format int32 */
  code?: number;
  msg?: string;
  data?: PageInfoPendingCertAuditVO;
}

import type {
  AxiosInstance,
  AxiosRequestConfig,
  AxiosResponse,
  HeadersDefaults,
  ResponseType,
} from "axios";
import axios from "axios";

export type QueryParamsType = Record<string | number, any>;

export interface FullRequestParams
  extends Omit<AxiosRequestConfig, "data" | "params" | "url" | "responseType"> {
  /** set parameter to `true` for call `securityWorker` for this request */
  secure?: boolean;
  /** request path */
  path: string;
  /** content type of request body */
  type?: ContentType;
  /** query params */
  query?: QueryParamsType;
  /** format of response (i.e. response.json() -> format: "json") */
  format?: ResponseType;
  /** request body */
  body?: unknown;
}

export type RequestParams = Omit<
  FullRequestParams,
  "body" | "method" | "query" | "path"
>;

export interface ApiConfig<SecurityDataType = unknown>
  extends Omit<AxiosRequestConfig, "data" | "cancelToken"> {
  securityWorker?: (
    securityData: SecurityDataType | null,
  ) => Promise<AxiosRequestConfig | void> | AxiosRequestConfig | void;
  secure?: boolean;
  format?: ResponseType;
}

export enum ContentType {
  Json = "application/json",
  JsonApi = "application/vnd.api+json",
  FormData = "multipart/form-data",
  UrlEncoded = "application/x-www-form-urlencoded",
  Text = "text/plain",
}

export class HttpClient<SecurityDataType = unknown> {
  public instance: AxiosInstance;
  private securityData: SecurityDataType | null = null;
  private securityWorker?: ApiConfig<SecurityDataType>["securityWorker"];
  private secure?: boolean;
  private format?: ResponseType;

  constructor({
    securityWorker,
    secure,
    format,
    ...axiosConfig
  }: ApiConfig<SecurityDataType> = {}) {
    this.instance = axios.create({
      ...axiosConfig,
      baseURL: axiosConfig.baseURL || "http://localhost:8083",
    });
    this.secure = secure;
    this.format = format;
    this.securityWorker = securityWorker;
  }

  public setSecurityData = (data: SecurityDataType | null) => {
    this.securityData = data;
  };

  protected mergeRequestParams(
    params1: AxiosRequestConfig,
    params2?: AxiosRequestConfig,
  ): AxiosRequestConfig {
    const method = params1.method || (params2 && params2.method);

    return {
      ...this.instance.defaults,
      ...params1,
      ...(params2 || {}),
      headers: {
        ...((method &&
          this.instance.defaults.headers[
            method.toLowerCase() as keyof HeadersDefaults
          ]) ||
          {}),
        ...(params1.headers || {}),
        ...((params2 && params2.headers) || {}),
      },
    };
  }

  protected stringifyFormItem(formItem: unknown) {
    if (typeof formItem === "object" && formItem !== null) {
      return JSON.stringify(formItem);
    } else {
      return `${formItem}`;
    }
  }

  protected createFormData(input: Record<string, unknown>): FormData {
    if (input instanceof FormData) {
      return input;
    }
    return Object.keys(input || {}).reduce((formData, key) => {
      const property = input[key];
      const propertyContent: any[] =
        property instanceof Array ? property : [property];

      for (const formItem of propertyContent) {
        const isFileType = formItem instanceof Blob || formItem instanceof File;
        formData.append(
          key,
          isFileType ? formItem : this.stringifyFormItem(formItem),
        );
      }

      return formData;
    }, new FormData());
  }

  public request = async <T = any, _E = any>({
    secure,
    path,
    type,
    query,
    format,
    body,
    ...params
  }: FullRequestParams): Promise<AxiosResponse<T>> => {
    const secureParams =
      ((typeof secure === "boolean" ? secure : this.secure) &&
        this.securityWorker &&
        (await this.securityWorker(this.securityData))) ||
      {};
    const requestParams = this.mergeRequestParams(params, secureParams);
    const responseFormat = format || this.format || undefined;

    if (
      type === ContentType.FormData &&
      body &&
      body !== null &&
      typeof body === "object"
    ) {
      body = this.createFormData(body as Record<string, unknown>);
    }

    if (
      type === ContentType.Text &&
      body &&
      body !== null &&
      typeof body !== "string"
    ) {
      body = JSON.stringify(body);
    }

    return this.instance.request({
      ...requestParams,
      headers: {
        ...(requestParams.headers || {}),
        ...(type ? { "Content-Type": type } : {}),
      },
      params: query,
      responseType: responseFormat,
      data: body,
      url: path,
    });
  };
}

/**
 * @title MediReserve 智慧医疗平台 API
 * @version 1.0.0
 * @license Apache 2.0 (https://www.apache.org/licenses/LICENSE-2.0.html)
 * @baseUrl http://localhost:8083
 * @contact wzx <2846334903@qq.com> (https://github.com/xiaoyuwzx)
 *
 * 患者端 / 医生端 / 管理端 接口文档
 */
export class Api<
  SecurityDataType extends unknown,
> extends HttpClient<SecurityDataType> {
  admin = {
    /**
     * No description
     *
     * @tags 管理端 - 权限管理
     * @name GetRolePermissionIds
     * @summary 查询角色拥有的权限ID列表
     * @request GET:/admin/permissions/roles/{roleId}/permissions
     */
    getRolePermissionIds: (roleId: number, params: RequestParams = {}) =>
      this.request<ResultListLong, ResultVoid>({
        path: `/admin/permissions/roles/${roleId}/permissions`,
        method: "GET",
        ...params,
      }),

    /**
     * No description
     *
     * @tags 管理端 - 权限管理
     * @name UpdateRolePermissions
     * @summary 更新角色权限
     * @request PUT:/admin/permissions/roles/{roleId}/permissions
     */
    updateRolePermissions: (
      roleId: number,
      data: RolePermissionUpdateDTO,
      params: RequestParams = {},
    ) =>
      this.request<ResultVoid, ResultVoid>({
        path: `/admin/permissions/roles/${roleId}/permissions`,
        method: "PUT",
        body: data,
        type: ContentType.Json,
        ...params,
      }),

    /**
     * @description 验证旧密码后更新为新密码
     *
     * @tags 管理员 - 认证管理
     * @name UpdatePassword
     * @summary 修改密码
     * @request PUT:/admin/password
     */
    updatePassword: (data: PasswordUpdateDTO, params: RequestParams = {}) =>
      this.request<ResultVoid, ResultVoid>({
        path: `/admin/password`,
        method: "PUT",
        body: data,
        type: ContentType.Json,
        ...params,
      }),

    /**
     * @description 创建管理员账号（仅限超级管理员操作）
     *
     * @tags 管理员 - 认证管理
     * @name Register
     * @summary 管理员注册
     * @request POST:/admin/register
     */
    register: (data: AdminRegisterDTO, params: RequestParams = {}) =>
      this.request<ResultMapStringObject, ResultVoid>({
        path: `/admin/register`,
        method: "POST",
        body: data,
        type: ContentType.Json,
        ...params,
      }),

    /**
     * @description 用户名 + 密码登录，成功后返回 JWT 令牌
     *
     * @tags 管理员 - 认证管理
     * @name Login
     * @summary 管理员登录
     * @request POST:/admin/login
     */
    login: (data: LoginDTO, params: RequestParams = {}) =>
      this.request<ResultMapStringObject, ResultVoid>({
        path: `/admin/login`,
        method: "POST",
        body: data,
        type: ContentType.Json,
        ...params,
      }),

    /**
     * @description 禁用或启用管理员账号
     *
     * @tags 管理员 - 认证管理
     * @name UpdateStatus
     * @summary 修改管理员状态
     * @request PATCH:/admin/{id}/status
     */
    updateStatus: (
      id: number,
      data: Record<string, number>,
      params: RequestParams = {},
    ) =>
      this.request<ResultString, ResultVoid>({
        path: `/admin/${id}/status`,
        method: "PATCH",
        body: data,
        type: ContentType.Json,
        ...params,
      }),

    /**
     * @description 通过：新证件生效；驳回：清空待审核数据，记录驳回原因
     *
     * @tags 管理端 - 医生审核
     * @name AuditCertificate
     * @summary 审核医生证件变更
     * @request PATCH:/admin/{doctorId}/cert-audit
     */
    auditCertificate: (
      doctorId: number,
      data: CertificateAuditDTO,
      params: RequestParams = {},
    ) =>
      this.request<ResultVoid, ResultVoid>({
        path: `/admin/${doctorId}/cert-audit`,
        method: "PATCH",
        body: data,
        type: ContentType.Json,
        ...params,
      }),

    /**
     * @description 管理员审核驳回医生注册申请，需填写驳回原因(仅限超级管理员)
     *
     * @tags 管理端 - 医生审核
     * @name Reject
     * @summary 审核驳回
     * @request PATCH:/admin/doctors/{id}/reject
     */
    reject: (id: number, data: AuditRejectDTO, params: RequestParams = {}) =>
      this.request<ResultString, ResultVoid>({
        path: `/admin/doctors/${id}/reject`,
        method: "PATCH",
        body: data,
        type: ContentType.Json,
        ...params,
      }),

    /**
     * @description 管理员审核医生注册申请(仅限超级管理员)
     *
     * @tags 管理端 - 医生审核
     * @name Approve
     * @summary 审核通过
     * @request PATCH:/admin/doctors/{id}/approve
     */
    approve: (id: number, params: RequestParams = {}) =>
      this.request<ResultString, ResultVoid>({
        path: `/admin/doctors/${id}/approve`,
        method: "PATCH",
        ...params,
      }),

    /**
     * No description
     *
     * @tags 管理端 - 医生审核
     * @name GetCertPendingDetail
     * @summary 待审核证件详情
     * @request GET:/admin/{doctorId}/cert-pending-detail
     */
    getCertPendingDetail: (doctorId: number, params: RequestParams = {}) =>
      this.request<ResultPendingCertAuditVO, ResultVoid>({
        path: `/admin/${doctorId}/cert-pending-detail`,
        method: "GET",
        ...params,
      }),

    /**
     * No description
     *
     * @tags 管理端 - 权限管理
     * @name GetPermissionTree
     * @summary 获取权限树
     * @request GET:/admin/permissions/tree
     */
    getPermissionTree: (params: RequestParams = {}) =>
      this.request<ResultListPermissionNodeVO, ResultVoid>({
        path: `/admin/permissions/tree`,
        method: "GET",
        ...params,
      }),

    /**
     * No description
     *
     * @tags 管理端 - 权限管理
     * @name GetAllRolesWithPermissions
     * @summary 查询所有角色及其权限
     * @request GET:/admin/permissions/roles
     */
    getAllRolesWithPermissions: (params: RequestParams = {}) =>
      this.request<ResultListRoleVO, ResultVoid>({
        path: `/admin/permissions/roles`,
        method: "GET",
        ...params,
      }),

    /**
     * @description 支持按操作人、模块、时间范围、结果状态筛选
     *
     * @tags 管理端 - 操作日志
     * @name List
     * @summary 查询日志列表
     * @request GET:/admin/operation-logs
     */
    list: (
      query: {
        /** 操作日志查询条件 DTO */
        query: OperationLogQueryDTO;
      },
      params: RequestParams = {},
    ) =>
      this.request<ResultPageInfoOperationLogVO, ResultVoid>({
        path: `/admin/operation-logs`,
        method: "GET",
        query: query,
        ...params,
      }),

    /**
     * No description
     *
     * @tags 管理端 - 操作日志
     * @name Detail
     * @summary 查看日志详情
     * @request GET:/admin/operation-logs/{id}
     */
    detail: (id: number, params: RequestParams = {}) =>
      this.request<ResultOperationLogVO, ResultVoid>({
        path: `/admin/operation-logs/${id}`,
        method: "GET",
        ...params,
      }),

    /**
     * No description
     *
     * @tags 管理端 - 操作日志
     * @name Delete
     * @summary 删除日志
     * @request DELETE:/admin/operation-logs/{id}
     */
    delete: (id: number, params: RequestParams = {}) =>
      this.request<ResultVoid, ResultVoid>({
        path: `/admin/operation-logs/${id}`,
        method: "DELETE",
        ...params,
      }),

    /**
     * @description 获取所有管理员账号列表（仅超级管理员）
     *
     * @tags 管理员 - 认证管理
     * @name ListAdmins
     * @summary 管理员列表
     * @request GET:/admin/list
     */
    listAdmins: (
      query?: {
        /**
         * @format int32
         * @default 1
         */
        page?: number;
        /**
         * @format int32
         * @default 10
         */
        size?: number;
      },
      params: RequestParams = {},
    ) =>
      this.request<ResultPageInfoAdmin, ResultVoid>({
        path: `/admin/list`,
        method: "GET",
        query: query,
        ...params,
      }),

    /**
     * @description 查看某位医生的完整注册信息和审核资料
     *
     * @tags 管理端 - 医生审核
     * @name GetAuditDetail
     * @summary 查看医生审核详细
     * @request GET:/admin/doctors/{id}/audit-detail
     */
    getAuditDetail: (id: number, params: RequestParams = {}) =>
      this.request<ResultDoctorAudit, ResultVoid>({
        path: `/admin/doctors/${id}/audit-detail`,
        method: "GET",
        ...params,
      }),

    /**
     * @description 分页查询所有待审核的医生(按注册时间升序)
     *
     * @tags 管理端 - 医生审核
     * @name ListPending
     * @summary 查询待审核医生列表
     * @request GET:/admin/doctors/pending
     */
    listPending: (
      query?: {
        /**
         * @format int32
         * @default 1
         */
        page?: number;
        /**
         * @format int32
         * @default 10
         */
        size?: number;
      },
      params: RequestParams = {},
    ) =>
      this.request<ResultPageInfoDoctorPendingVO, ResultVoid>({
        path: `/admin/doctors/pending`,
        method: "GET",
        query: query,
        ...params,
      }),

    /**
     * @description 近N天每日挂号/支付/收入趋势
     *
     * @tags 管理端 - 数据统计看板
     * @name GetTrend
     * @summary 趋势数据
     * @request GET:/admin/dashboard/trend
     */
    getTrend: (
      query?: {
        /**
         * 查询天数，默认7，最大90
         * @format int32
         * @default 7
         */
        days?: number;
      },
      params: RequestParams = {},
    ) =>
      this.request<ResultListTrendDataVO, ResultVoid>({
        path: `/admin/dashboard/trend`,
        method: "GET",
        query: query,
        ...params,
      }),

    /**
     * @description 统计各状态预约数量
     *
     * @tags 管理端 - 数据统计看板
     * @name GetStatusDistribution
     * @summary 预约状态分布
     * @request GET:/admin/dashboard/status-distribution
     */
    getStatusDistribution: (params: RequestParams = {}) =>
      this.request<ResultListStatusDistributionVO, ResultVoid>({
        path: `/admin/dashboard/status-distribution`,
        method: "GET",
        ...params,
      }),

    /**
     * @description 返回今日关键指标及总量
     *
     * @tags 管理端 - 数据统计看板
     * @name GetOverview
     * @summary 总览统计
     * @request GET:/admin/dashboard/overview
     */
    getOverview: (params: RequestParams = {}) =>
      this.request<ResultDashboardOverviewVO, ResultVoid>({
        path: `/admin/dashboard/overview`,
        method: "GET",
        ...params,
      }),

    /**
     * @description 按挂号量或评分排序医生
     *
     * @tags 管理端 - 数据统计看板
     * @name GetDoctorRanking
     * @summary 医生排行
     * @request GET:/admin/dashboard/doctor-ranking
     */
    getDoctorRanking: (
      query?: {
        /**
         * 返回前N条，默认10，最大50
         * @format int32
         * @default 10
         */
        limit?: number;
        /**
         * 排序字段：appointment（挂号量）或 score（评分）
         * @default "appointment"
         */
        sortBy?: string;
      },
      params: RequestParams = {},
    ) =>
      this.request<ResultListDoctorRankingVO, ResultVoid>({
        path: `/admin/dashboard/doctor-ranking`,
        method: "GET",
        query: query,
        ...params,
      }),

    /**
     * @description 按挂号量降序返回科室排名
     *
     * @tags 管理端 - 数据统计看板
     * @name GetDepartmentRanking
     * @summary 科室排行
     * @request GET:/admin/dashboard/department-ranking
     */
    getDepartmentRanking: (
      query?: {
        /**
         * 返回前N条，默认10，最大50
         * @format int32
         * @default 10
         */
        limit?: number;
      },
      params: RequestParams = {},
    ) =>
      this.request<ResultListDepartmentRankingVO, ResultVoid>({
        path: `/admin/dashboard/department-ranking`,
        method: "GET",
        query: query,
        ...params,
      }),

    /**
     * @description 返回已提交证件变更申请的医生列表
     *
     * @tags 管理端 - 医生审核
     * @name ListCertPending
     * @summary 待审核证件列表
     * @request GET:/admin/cert-pending
     */
    listCertPending: (
      query?: {
        /**
         * 页码
         * @format int32
         * @default 1
         */
        pageNum?: number;
        /**
         * 每页大小
         * @format int32
         * @default 10
         */
        pageSize?: number;
      },
      params: RequestParams = {},
    ) =>
      this.request<ResultPageInfoPendingCertAuditVO, ResultVoid>({
        path: `/admin/cert-pending`,
        method: "GET",
        query: query,
        ...params,
      }),
  };
}
