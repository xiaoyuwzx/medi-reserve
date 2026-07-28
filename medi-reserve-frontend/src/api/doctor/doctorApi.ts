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

/** 医生个人信息更新请求 DTO（普通信息立即生效，证件提交审核） */
export interface DoctorUpdateDTO {
  /** 姓名 */
  name: string;
  /**
   * 手机号（11位）
   * @pattern ^1[3-9]\d{9}$
   */
  phone: string;
  /**
   * 性别：0=未知，1=男，2=女
   * @format int32
   */
  gender?: number;
  /**
   * 身份证号（18位）
   * @pattern ^[1-9]\d{16}[0-9Xx]$
   */
  idCard?: string;
  /** 擅长领域 */
  specialty?: string;
  /** 个人简介 */
  introduction?: string;
  /** 新执业证书图片URL（提交审核，需管理员审批） */
  certificateUrl?: string;
  /** 新资格证图片URL（提交审核，需管理员审批） */
  qualificationUrl?: string;
}

export interface ResultMapStringObject {
  /** @format int32 */
  code?: number;
  msg?: string;
  data?: Record<string, object>;
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

/** 新增排班请求 DTO */
export interface ScheduleCreateDTO {
  /**
   * 排班日期（yyyy-MM-dd）
   * @format date
   */
  scheduleDate: string;
  /**
   * 时段：1=上午，2=下午
   * @format int32
   * @min 1
   * @max 2
   */
  period: 1 | 2;
  /**
   * 最大挂号数（1-100）
   * @format int32
   * @min 1
   * @max 100
   */
  maxCount: number;
}

/** 医生注册请求 DTO（含审核资料） */
export interface DoctorRegisterDTO {
  /** 姓名 */
  name: string;
  /**
   * 手机号（11位）
   * @pattern ^1[3-9]\d{9}$
   */
  phone: string;
  /**
   * 密码（6-20位字母和数字组合）
   * @pattern ^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d]{6,20}$
   */
  password: string;
  /**
   * 科室ID
   * @format int64
   */
  departmentId: number;
  /**
   * 职称ID
   * @format int64
   */
  titleId: number;
  /**
   * 身份证号（18位）
   * @pattern ^[1-9]\d{16}[0-9Xx]$
   */
  idCard?: string;
  /**
   * 性别：0=未知，1=男，2=女
   * @format int32
   * @min 0
   * @max 2
   */
  gender?: number;
  /** 擅长领域 */
  specialty?: string;
  /** 个人简介 */
  introduction?: string;
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

/** 每日趋势数据点 */
export interface DailyTrendVO {
  /**
   * 日期
   * @format date
   */
  date?: string;
  /**
   * 当日接诊量
   * @format int64
   */
  count?: number;
}

export interface ResultListDailyTrendVO {
  /** @format int32 */
  code?: number;
  msg?: string;
  data?: DailyTrendVO[];
}

/** 医生端统计总览数据 VO */
export interface DoctorStatisticsOverviewVO {
  /**
   * 总接诊人数（已支付+已完成）
   * @format int64
   */
  totalPatients?: number;
  /**
   * 今日接诊人数（已支付+已完成）
   * @format int64
   */
  todayPatients?: number;
  /** 平均评分（0-5，保留2位） */
  avgScore?: number;
  /** 好评率（评分>=4的比例，百分比） */
  positiveRate?: number;
  /**
   * 评价总数
   * @format int64
   */
  evaluationCount?: number;
  /**
   * 待处理问诊数（已支付未就诊）
   * @format int64
   */
  pendingConsultations?: number;
}

export interface ResultDoctorStatisticsOverviewVO {
  /** @format int32 */
  code?: number;
  msg?: string;
  /** 医生端统计总览数据 VO */
  data?: DoctorStatisticsOverviewVO;
}

/** 医生收到的评价列表项 VO */
export interface DoctorEvaluationVO {
  /**
   * 评价ID
   * @format int64
   */
  evaluationId?: number;
  /** 患者姓名（匿名时显示'匿名用户'） */
  patientName?: string;
  /**
   * 评分（1-5）
   * @format int32
   */
  score?: number;
  /** 评价内容 */
  content?: string;
  /** 是否匿名 */
  isAnonymous?: boolean;
  /**
   * 评价时间
   * @format date-time
   */
  createdAt?: string;
}

export interface PageInfoDoctorEvaluationVO {
  /** @format int64 */
  total?: number;
  list?: DoctorEvaluationVO[];
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

export interface ResultPageInfoDoctorEvaluationVO {
  /** @format int32 */
  code?: number;
  msg?: string;
  data?: PageInfoDoctorEvaluationVO;
}

/** 排班查询请求 DTO */
export interface ScheduleQueryDTO {
  /**
   * 查询开始日期（yyyy-MM-dd）
   * @format date
   */
  startDate?: string;
  /**
   * 查询结束日期（yyyy-MM-dd）
   * @format date
   */
  endDate?: string;
  /**
   * 排班状态：1-正常，2-停诊，3-已满
   * @format int32
   * @min 1
   * @max 3
   */
  status?: number;
  endDateAfterStartDate?: boolean;
}

export interface ResultListSchedule {
  /** @format int32 */
  code?: number;
  msg?: string;
  data?: Schedule[];
}

export interface Schedule {
  /** @format int64 */
  id?: number;
  /** @format int64 */
  doctorId?: number;
  /** @format date */
  scheduleDate?: string;
  /** @format int32 */
  period?: number;
  /** @format int32 */
  maxCount?: number;
  /** @format int32 */
  remainingCount?: number;
  /** @format int32 */
  status?: number;
  /** @format date-time */
  createdAt?: string;
  /** @format date-time */
  updatedAt?: string;
}

/** 医生审核资料信息 VO（医生端查询审核状态） */
export interface DoctorAuditInfoVO {
  /**
   * 医生ID
   * @format int64
   */
  doctorId?: number;
  /** 当前生效的执业证书URL */
  certificateUrl?: string;
  /** 当前生效的资格证URL */
  qualificationUrl?: string;
  /** 待审核的执业证书URL */
  pendingCertificateUrl?: string;
  /** 待审核的资格证URL */
  pendingQualificationUrl?: string;
  /**
   * 证件审核状态：0-待审核，1-已通过，2-已驳回
   * @format int32
   */
  certAuditStatus?: number;
  /** 审核状态描述 */
  certAuditStatusText?: string;
  /** 审核备注（驳回原因） */
  certAuditRemark?: string;
  /**
   * 审核时间
   * @format date-time
   */
  certAuditTime?: string;
  /** 擅长领域 */
  specialty?: string;
  /** 个人简介 */
  introduction?: string;
}

export interface ResultDoctorAuditInfoVO {
  /** @format int32 */
  code?: number;
  msg?: string;
  /** 医生审核资料信息 VO（医生端查询审核状态） */
  data?: DoctorAuditInfoVO;
}

/** OSS STS 临时凭证返回对象 */
export interface OssStsVO {
  /** 临时 AccessKeyId */
  accessKeyId?: string;
  /** 临时 AccessKeySecret */
  accessKeySecret?: string;
  /** 安全令牌（STS） */
  securityToken?: string;
  /** 凭证过期时间（ISO 8601） */
  expiration?: string;
  /** OSS 存储空间名称 */
  bucket?: string;
  /** OSS 访问端点 */
  endpoint?: string;
  /** 上传目标目录（需前端拼接） */
  dir?: string;
}

export interface ResultOssStsVO {
  /** @format int32 */
  code?: number;
  msg?: string;
  /** OSS STS 临时凭证返回对象 */
  data?: OssStsVO;
}

/** 预约列表返回 VO（患者端/医生端通用） */
export interface AppointmentListVO {
  /**
   * 预约ID
   * @format int64
   */
  id?: number;
  /** 预约单号 */
  appointmentNo?: string;
  /**
   * 排班ID
   * @format int64
   */
  scheduleId?: number;
  /**
   * 患者ID
   * @format int64
   */
  patientId?: number;
  /**
   * 医生ID
   * @format int64
   */
  doctorId?: number;
  /**
   * 预约状态：0-待支付，1-已支付，2-已就诊，3-已取消，4-已过期
   * @format int32
   */
  status?: number;
  /**
   * 创建时间
   * @format date-time
   */
  createdAt?: string;
  /** 医生姓名 */
  doctorName?: string;
  /** 科室名称 */
  departmentName?: string;
  /** 职称名称 */
  titleName?: string;
  /**
   * 就诊日期
   * @format date
   */
  scheduleDate?: string;
  /**
   * 时段：1=上午，2=下午
   * @format int32
   */
  period?: number;
  /** 时段文本（上午/下午） */
  periodText?: string;
  /** 患者姓名（医生端查询） */
  patientName?: string;
  /** 患者手机号（医生端查询） */
  patientPhone?: string;
}

export interface PageInfoAppointmentListVO {
  /** @format int64 */
  total?: number;
  list?: AppointmentListVO[];
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

export interface ResultPageInfoAppointmentListVO {
  /** @format int32 */
  code?: number;
  msg?: string;
  data?: PageInfoAppointmentListVO;
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
      baseURL: axiosConfig.baseURL || "http://localhost:8082",
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
 * @baseUrl http://localhost:8082
 * @contact wzx <2846334903@qq.com> (https://github.com/xiaoyuwzx)
 *
 * 患者端 / 医生端 / 管理端 接口文档
 */
export class Api<
  SecurityDataType extends unknown,
> extends HttpClient<SecurityDataType> {
  doctor = {
    /**
     * @description 普通信息立即生效，证件信息提交审核（需管理员审批）
     *
     * @tags 医生端 - 认证管理
     * @name UpdateProfile
     * @summary 修改个人信息
     * @request PUT:/doctor/profile
     */
    updateProfile: (data: DoctorUpdateDTO, params: RequestParams = {}) =>
      this.request<ResultMapStringObject, ResultVoid>({
        path: `/doctor/profile`,
        method: "PUT",
        body: data,
        type: ContentType.Json,
        ...params,
      }),

    /**
     * @description 验证旧密码后更新为新密码
     *
     * @tags 医生端 - 认证管理
     * @name UpdatePassword
     * @summary 修改密码
     * @request PUT:/doctor/password
     */
    updatePassword: (data: PasswordUpdateDTO, params: RequestParams = {}) =>
      this.request<ResultVoid, ResultVoid>({
        path: `/doctor/password`,
        method: "PUT",
        body: data,
        type: ContentType.Json,
        ...params,
      }),

    /**
     * @description 医生查看自己的排班列表，支持按日期范围筛选
     *
     * @tags 医生端 - 排班管理
     * @name ListSchedules
     * @summary 查询我的排班
     * @request GET:/doctor/schedules
     */
    listSchedules: (
      query: {
        /** 排班查询请求 DTO */
        scheduleQueryDTO: ScheduleQueryDTO;
      },
      params: RequestParams = {},
    ) =>
      this.request<ResultListSchedule, ResultVoid>({
        path: `/doctor/schedules`,
        method: "GET",
        query: query,
        ...params,
      }),

    /**
     * @description 医生选择日期和时段，设置最大挂号数（系统会基于历史数据智能推荐）
     *
     * @tags 医生端 - 排班管理
     * @name CreateSchedule
     * @summary 新增排班
     * @request POST:/doctor/schedules
     */
    createSchedule: (data: ScheduleCreateDTO, params: RequestParams = {}) =>
      this.request<ResultMapStringObject, ResultVoid>({
        path: `/doctor/schedules`,
        method: "POST",
        body: data,
        type: ContentType.Json,
        ...params,
      }),

    /**
     * @description 填写个人信息注册医生账号，提交后进入待审核状态
     *
     * @tags 医生端 - 认证管理
     * @name Register
     * @summary 医生注册
     * @request POST:/doctor/register
     */
    register: (data: DoctorRegisterDTO, params: RequestParams = {}) =>
      this.request<ResultMapStringObject, ResultVoid>({
        path: `/doctor/register`,
        method: "POST",
        body: data,
        type: ContentType.Json,
        ...params,
      }),

    /**
     * @description 手机号 + 密码登录，成功后返回 JWT 令牌
     *
     * @tags 医生端 - 认证管理
     * @name Login
     * @summary 医生登录
     * @request POST:/doctor/login
     */
    login: (data: LoginDTO, params: RequestParams = {}) =>
      this.request<ResultMapStringObject, ResultVoid>({
        path: `/doctor/login`,
        method: "POST",
        body: data,
        type: ContentType.Json,
        ...params,
      }),

    /**
     * @description 传入 status=2 停诊，status=1 恢复
     *
     * @tags 医生端 - 排班管理
     * @name UpdateScheduleStatus
     * @summary 停诊/恢复排班
     * @request PATCH:/doctor/schedules/{id}/status
     */
    updateScheduleStatus: (
      id: number,
      query: {
        /** @format int32 */
        status: number;
      },
      params: RequestParams = {},
    ) =>
      this.request<ResultString, ResultVoid>({
        path: `/doctor/schedules/${id}/status`,
        method: "PATCH",
        query: query,
        ...params,
      }),

    /**
     * @description 近 N 天每日接诊量，默认 7 天，最大 90 天
     *
     * @tags 医生端 - 数据统计
     * @name GetTrend
     * @summary 每日接诊趋势
     * @request GET:/doctor/statistics/trend
     */
    getTrend: (
      query?: {
        /**
         * 天数，默认 7，最大 90
         * @format int32
         * @default 7
         */
        days?: number;
      },
      params: RequestParams = {},
    ) =>
      this.request<ResultListDailyTrendVO, ResultVoid>({
        path: `/doctor/statistics/trend`,
        method: "GET",
        query: query,
        ...params,
      }),

    /**
     * @description 返回总接诊数、好评率、平均评分、今日接诊数、待处理问诊数
     *
     * @tags 医生端 - 数据统计
     * @name GetOverview
     * @summary 总览统计
     * @request GET:/doctor/statistics/overview
     */
    getOverview: (params: RequestParams = {}) =>
      this.request<ResultDoctorStatisticsOverviewVO, ResultVoid>({
        path: `/doctor/statistics/overview`,
        method: "GET",
        ...params,
      }),

    /**
     * @description 分页获取患者对医生的评价
     *
     * @tags 医生端 - 数据统计
     * @name GetEvaluations
     * @summary 评价列表
     * @request GET:/doctor/statistics/evaluations
     */
    getEvaluations: (
      query?: {
        /**
         * 页码，默认 1
         * @format int32
         * @default 1
         */
        page?: number;
        /**
         * 每页大小，默认 10，最大 50
         * @format int32
         * @default 10
         */
        size?: number;
      },
      params: RequestParams = {},
    ) =>
      this.request<ResultPageInfoDoctorEvaluationVO, ResultVoid>({
        path: `/doctor/statistics/evaluations`,
        method: "GET",
        query: query,
        ...params,
      }),

    /**
     * @description 基于历史就诊数据，智能推荐号源数量（仅做参考，用户可自行修改）
     *
     * @tags 医生端 - 排班管理
     * @name GetRecommendedMaxCount
     * @summary 获取推荐号源数
     * @request GET:/doctor/schedules/recommend
     */
    getRecommendedMaxCount: (
      query: {
        /** @format date */
        scheduleDate: string;
        /**
         * @format int32
         * @default 20
         */
        userInputMax?: number;
      },
      params: RequestParams = {},
    ) =>
      this.request<ResultMapStringObject, ResultVoid>({
        path: `/doctor/schedules/recommend`,
        method: "GET",
        query: query,
        ...params,
      }),

    /**
     * @description 返回当前证件审核状态：待审核/已通过/已驳回/未提交
     *
     * @tags 医生端 - 认证管理
     * @name GetAuditStatus
     * @summary 查询证件审核状态
     * @request GET:/doctor/profile/audit-status
     */
    getAuditStatus: (params: RequestParams = {}) =>
      this.request<ResultDoctorAuditInfoVO, ResultVoid>({
        path: `/doctor/profile/audit-status`,
        method: "GET",
        ...params,
      }),

    /**
     * @description 返回 STS 临时凭证，供前端直传文件使用（有效期30分钟）
     *
     * @tags 医生端 - 文件上传
     * @name GetStsToken
     * @summary 获取 OSS 上传凭证
     * @request GET:/doctor/oss/sts
     */
    getStsToken: (params: RequestParams = {}) =>
      this.request<ResultOssStsVO, ResultVoid>({
        path: `/doctor/oss/sts`,
        method: "GET",
        ...params,
      }),

    /**
     * @description 返回 STS 临时凭证，供前端直传文件使用（有效期30分钟）
     *
     * @tags 医生端 - 文件上传
     * @name GetStsToken1
     * @summary 获取 OSS 上传凭证
     * @request GET:/doctor/oss/sts-token
     */
    getStsToken1: (params: RequestParams = {}) =>
      this.request<ResultOssStsVO, ResultVoid>({
        path: `/doctor/oss/sts-token`,
        method: "GET",
        ...params,
      }),

    /**
     * @description 查询已支付的预约，默认查今天
     *
     * @tags 医生端 - 在线问诊
     * @name GetDoctorAppointments
     * @summary 查询医生预约列表
     * @request GET:/doctor/appointments
     */
    getDoctorAppointments: (
      query?: {
        date?: string;
        /** @format int32 */
        status?: number;
        /**
         * @format int32
         * @default 1
         */
        page?: number;
        /**
         * @format int32
         * @default 20
         */
        size?: number;
      },
      params: RequestParams = {},
    ) =>
      this.request<ResultPageInfoAppointmentListVO, ResultVoid>({
        path: `/doctor/appointments`,
        method: "GET",
        query: query,
        ...params,
      }),

    /**
     * @description 物理删除排班（仅当该排班下无预约记录时允许）
     *
     * @tags 医生端 - 排班管理
     * @name DeleteSchedule
     * @summary 删除排班
     * @request DELETE:/doctor/schedules/{id}
     */
    deleteSchedule: (id: number, params: RequestParams = {}) =>
      this.request<ResultString, ResultVoid>({
        path: `/doctor/schedules/${id}`,
        method: "DELETE",
        ...params,
      }),
  };
}
