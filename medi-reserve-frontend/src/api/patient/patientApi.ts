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

/** 患者个人信息修改请求 DTO */
export interface PatientUpdateDTO {
  /** 姓名 */
  name: string;
  /**
   * 手机号（11位）
   * @pattern ^1[3-9]\d{9}$
   */
  phone: string;
  /**
   * 身份证号（18位）
   * @pattern ^[1-9]\d{16}[0-9Xx]$
   */
  idCard?: string;
  /**
   * 性别：0=未知，1=男，2=女
   * @format int32
   */
  gender?: number;
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

/** 患者注册请求 DTO */
export interface PatientRegisterDTO {
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
   * 身份证号（18位）
   * @pattern ^[1-9]\d{16}[0-9Xx]$
   */
  idCard?: string;
  /**
   * 性别：0=未知，1=男，2=女
   * @format int32
   */
  gender?: number;
}

/** 通用登录请求 DTO（患者/医生使用手机号，管理员使用用户名） */
export interface LoginDTO {
  /** 登录账号（手机号或用户名） */
  username: string;
  /** 密码 */
  password: string;
}

/** 创建评价请求 DTO */
export interface EvaluationCreateDTO {
  /**
   * 预约ID
   * @format int64
   */
  appointmentId: number;
  /**
   * 评分（1-5）
   * @format int32
   * @min 1
   * @max 5
   */
  score: number;
  /**
   * 评价内容（最多500字）
   * @minLength 0
   * @maxLength 500
   */
  content?: string;
  /** 是否匿名（默认false） */
  isAnonymous?: boolean;
}

/** 创建预约请求 DTO */
export interface AppointmentCreateDTO {
  /**
   * 排班ID
   * @format int64
   */
  scheduleId: number;
}

export interface ResultString {
  /** @format int32 */
  code?: number;
  msg?: string;
  data?: string;
}

export interface ResultScheduleDetailVO {
  /** @format int32 */
  code?: number;
  msg?: string;
  /** 排班详情返回 VO（挂号确认页） */
  data?: ScheduleDetailVO;
}

/** 排班详情返回 VO（挂号确认页） */
export interface ScheduleDetailVO {
  /**
   * 排班ID
   * @format int64
   */
  scheduleId?: number;
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
  /**
   * 排班日期
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
  /**
   * 剩余号源
   * @format int32
   */
  remainingCount?: number;
  /**
   * 排班状态：1-正常，2-停诊，3-已满
   * @format int32
   */
  status?: number;
  /** 状态文本 */
  statusText?: string;
}

/** 我的评价返回 VO（患者端查看自己的评价） */
export interface MyEvaluationVO {
  /**
   * 评价ID
   * @format int64
   */
  evaluationId?: number;
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
   * 评分（1-5）
   * @format int32
   */
  score?: number;
  /** 评价内容 */
  content?: string;
  /** 是否匿名 */
  isAnonymous?: boolean;
  /**
   * 评价状态：1-已发布，2-已删除
   * @format int32
   */
  status?: number;
  /**
   * 创建时间
   * @format date-time
   */
  createdAt?: string;
}

export interface PageInfoMyEvaluationVO {
  /** @format int64 */
  total?: number;
  list?: MyEvaluationVO[];
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

export interface ResultPageInfoMyEvaluationVO {
  /** @format int32 */
  code?: number;
  msg?: string;
  data?: PageInfoMyEvaluationVO;
}

/** 医生列表查询请求 DTO */
export interface DoctorListQueryDTO {
  /** 科室筛选（精确匹配） */
  department?: string;
  /** 关键词搜索（姓名/擅长领域） */
  keyword?: string;
  /**
   * 页码（从1开始）
   * @format int32
   * @min 1
   */
  page?: number;
  /**
   * 每页条数（1-100）
   * @format int32
   * @min 1
   */
  size?: number;
}

/** 医生列表返回对象（患者端） */
export interface DoctorListVO {
  /**
   * 医生ID
   * @format int64
   */
  doctorId?: number;
  /** 医生姓名 */
  name?: string;
  /** 科室名称 */
  department?: string;
  /** 职称名称 */
  title?: string;
  /** 擅长领域 */
  specialty?: string;
  /**
   * 职称权重（4=主任医师，1=住院医师）
   * @format int32
   */
  titleWeight?: number;
  /** 未来7天是否有可用号源 */
  hasAvailableSlot?: boolean;
}

export interface PageInfoDoctorListVO {
  /** @format int64 */
  total?: number;
  list?: DoctorListVO[];
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

export interface ResultPageInfoDoctorListVO {
  /** @format int32 */
  code?: number;
  msg?: string;
  data?: PageInfoDoctorListVO;
}

export interface ResultListScheduleCalendarVO {
  /** @format int32 */
  code?: number;
  msg?: string;
  data?: ScheduleCalendarVO[];
}

/** 排班日历返回 VO（患者端） */
export interface ScheduleCalendarVO {
  /**
   * 排班ID
   * @format int64
   */
  scheduleId?: number;
  /**
   * 排班日期
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
  /**
   * 剩余号源
   * @format int32
   */
  remainingCount?: number;
  /**
   * 排班状态：1-正常，2-停诊，3-已满
   * @format int32
   */
  status?: number;
  /** 状态文本 */
  statusText?: string;
}

/** 医生评价列表返回 VO（患者端查看医生评价） */
export interface EvaluationListVO {
  /**
   * 评价ID
   * @format int64
   */
  evaluationId?: number;
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
   * 排班日期（就诊日期）
   * @format date
   */
  scheduleDate?: string;
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

export interface PageInfoEvaluationListVO {
  /** @format int64 */
  total?: number;
  list?: EvaluationListVO[];
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

export interface ResultPageInfoEvaluationListVO {
  /** @format int32 */
  code?: number;
  msg?: string;
  data?: PageInfoEvaluationListVO;
}

/** 热门医生排行返回 VO */
export interface DoctorHotVO {
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
  /** 头像URL */
  avatar?: string;
  /**
   * 热度综合评分（保留2位小数）
   * @format double
   */
  hotScore?: number;
  /**
   * 评价总数
   * @format int32
   */
  evaluationCount?: number;
}

export interface ResultListDoctorHotVO {
  /** @format int32 */
  code?: number;
  msg?: string;
  data?: DoctorHotVO[];
}

export interface ResultListTitle {
  /** @format int32 */
  code?: number;
  msg?: string;
  data?: Title[];
}

export interface Title {
  /** @format int64 */
  id?: number;
  name?: string;
  /** @format int32 */
  sortOrder?: number;
  /** @format date-time */
  createdAt?: string;
  /** @format date-time */
  updatedAt?: string;
}

/** 科室列表返回对象 */
export interface DepartmentVO {
  /**
   * 科室ID
   * @format int64
   */
  id?: number;
  /** 科室名称 */
  department?: string;
  /**
   * 该科室可挂号医生数量
   * @format int32
   */
  doctorCount?: number;
}

export interface ResultListDepartmentVO {
  /** @format int32 */
  code?: number;
  msg?: string;
  data?: DepartmentVO[];
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
      baseURL: axiosConfig.baseURL || "http://localhost:8081",
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
 * @baseUrl http://localhost:8081
 * @contact wzx <2846334903@qq.com> (https://github.com/xiaoyuwzx)
 *
 * 患者端 / 医生端 / 管理端 接口文档
 */
export class Api<
  SecurityDataType extends unknown,
> extends HttpClient<SecurityDataType> {
  patient = {
    /**
     * @description 修改姓名、手机号、性别、身份证号
     *
     * @tags 患者端 - 认证管理
     * @name UpdateProfile
     * @summary 修改个人信息
     * @request PUT:/patient/profile
     */
    updateProfile: (data: PatientUpdateDTO, params: RequestParams = {}) =>
      this.request<ResultMapStringObject, ResultVoid>({
        path: `/patient/profile`,
        method: "PUT",
        body: data,
        type: ContentType.Json,
        ...params,
      }),

    /**
     * @description 验证旧密码后更新为新密码
     *
     * @tags 患者端 - 认证管理
     * @name UpdatePassword
     * @summary 修改密码
     * @request PUT:/patient/password
     */
    updatePassword: (data: PasswordUpdateDTO, params: RequestParams = {}) =>
      this.request<ResultVoid, ResultVoid>({
        path: `/patient/password`,
        method: "PUT",
        body: data,
        type: ContentType.Json,
        ...params,
      }),

    /**
     * @description 填写个人信息注册患者账号
     *
     * @tags 患者端 - 认证管理
     * @name Register
     * @summary 患者注册
     * @request POST:/patient/register
     */
    register: (data: PatientRegisterDTO, params: RequestParams = {}) =>
      this.request<ResultMapStringObject, ResultVoid>({
        path: `/patient/register`,
        method: "POST",
        body: data,
        type: ContentType.Json,
        ...params,
      }),

    /**
     * @description 手机号 + 密码，成功登录后返回 JWT 令牌
     *
     * @tags 患者端 - 认证管理
     * @name Login
     * @summary 患者登录
     * @request POST:/patient/login
     */
    login: (data: LoginDTO, params: RequestParams = {}) =>
      this.request<ResultMapStringObject, ResultVoid>({
        path: `/patient/login`,
        method: "POST",
        body: data,
        type: ContentType.Json,
        ...params,
      }),

    /**
     * @description 患者对已就诊的预约进行评分和文字评价
     *
     * @tags 患者端 - 就诊评价
     * @name CreateEvaluation
     * @summary 创建评价
     * @request POST:/patient/evaluations
     */
    createEvaluation: (data: EvaluationCreateDTO, params: RequestParams = {}) =>
      this.request<ResultMapStringObject, ResultVoid>({
        path: `/patient/evaluations`,
        method: "POST",
        body: data,
        type: ContentType.Json,
        ...params,
      }),

    /**
     * @description 分页查询当前患者的预约记录，支持按状态筛选
     *
     * @tags 患者端 - 预约挂号
     * @name GetMyAppointments
     * @summary 查询我的预约列表
     * @request GET:/patient/appointments
     */
    getMyAppointments: (
      query?: {
        /** @format int32 */
        status?: number;
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
      this.request<ResultPageInfoAppointmentListVO, ResultVoid>({
        path: `/patient/appointments`,
        method: "GET",
        query: query,
        ...params,
      }),

    /**
     * @description 患者选择排班，扣减号源，生成待支付预约单
     *
     * @tags 患者端 - 预约挂号
     * @name CreateAppointment
     * @summary 创建预约(下单)
     * @request POST:/patient/appointments
     */
    createAppointment: (
      data: AppointmentCreateDTO,
      params: RequestParams = {},
    ) =>
      this.request<ResultMapStringObject, ResultVoid>({
        path: `/patient/appointments`,
        method: "POST",
        body: data,
        type: ContentType.Json,
        ...params,
      }),

    /**
     * @description 模拟微信支付回调，将预约状态改为已支付
     *
     * @tags 患者端 - 预约挂号
     * @name PayAppointment
     * @summary 模拟支付
     * @request POST:/patient/appointments/{appointmentId}/pay
     */
    payAppointment: (appointmentId: number, params: RequestParams = {}) =>
      this.request<ResultString, ResultVoid>({
        path: `/patient/appointments/${appointmentId}/pay`,
        method: "POST",
        ...params,
      }),

    /**
     * @description 手动刷新热门医生排行榜(仅超级管理员)
     *
     * @tags 患者端 - 就诊评价
     * @name RefreshHotCache
     * @summary 刷新热门医生缓存
     * @request POST:/patient/admin/refresh-hot-cache
     */
    refreshHotCache: (params: RequestParams = {}) =>
      this.request<ResultString, ResultVoid>({
        path: `/patient/admin/refresh-hot-cache`,
        method: "POST",
        ...params,
      }),

    /**
     * @description 挂号前确定排班信息(日期、时段、医生、剩余号源)
     *
     * @tags 患者端 - 预约挂号
     * @name GetScheduleDetail
     * @summary 查询排班详细
     * @request GET:/patient/schedules/{scheduleId}
     */
    getScheduleDetail: (scheduleId: number, params: RequestParams = {}) =>
      this.request<ResultScheduleDetailVO, ResultVoid>({
        path: `/patient/schedules/${scheduleId}`,
        method: "GET",
        ...params,
      }),

    /**
     * @description 分页查询当前患者提交的所有评价
     *
     * @tags 患者端 - 就诊评价
     * @name GetMyEvaluations
     * @summary 查询我的评价
     * @request GET:/patient/my-evaluations
     */
    getMyEvaluations: (
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
      this.request<ResultPageInfoMyEvaluationVO, ResultVoid>({
        path: `/patient/my-evaluations`,
        method: "GET",
        query: query,
        ...params,
      }),

    /**
     * @description 支持按科室删选、关键词搜索(名字/擅长)
     *
     * @tags 患者端 - 号源展示
     * @name GetDoctorList
     * @summary 分页查询医生列表
     * @request GET:/patient/doctors
     */
    getDoctorList: (
      query: {
        /** 医生列表查询请求 DTO */
        doctorListQueryDTO: DoctorListQueryDTO;
      },
      params: RequestParams = {},
    ) =>
      this.request<ResultPageInfoDoctorListVO, ResultVoid>({
        path: `/patient/doctors`,
        method: "GET",
        query: query,
        ...params,
      }),

    /**
     * @description 查看某医生未来7天的排班号源情况
     *
     * @tags 患者端 - 号源展示
     * @name GetScheduleCalendar
     * @summary 获取医生排班日历
     * @request GET:/patient/doctors/{doctorId}/schedules
     */
    getScheduleCalendar: (doctorId: number, params: RequestParams = {}) =>
      this.request<ResultListScheduleCalendarVO, ResultVoid>({
        path: `/patient/doctors/${doctorId}/schedules`,
        method: "GET",
        ...params,
      }),

    /**
     * @description 分页查询某医生的历史评价（公开接口，无需登录）
     *
     * @tags 患者端 - 就诊评价
     * @name GetDoctorEvaluations
     * @summary 查询医生评价列表
     * @request GET:/patient/doctors/{doctorId}/evaluations
     */
    getDoctorEvaluations: (
      doctorId: number,
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
      this.request<ResultPageInfoEvaluationListVO, ResultVoid>({
        path: `/patient/doctors/${doctorId}/evaluations`,
        method: "GET",
        query: query,
        ...params,
      }),

    /**
     * @description 获取热度前10的医生(基于近30天评价，时间衰败算法)
     *
     * @tags 患者端 - 就诊评价
     * @name GetHotDoctors
     * @summary 热门医生排行榜
     * @request GET:/patient/doctors/hot
     */
    getHotDoctors: (params: RequestParams = {}) =>
      this.request<ResultListDoctorHotVO, ResultVoid>({
        path: `/patient/doctors/hot`,
        method: "GET",
        ...params,
      }),

    /**
     * No description
     *
     * @tags 公共字典：获取所有科室、职称信息
     * @name GetTitles
     * @summary 获取职称列表
     * @request GET:/patient/dict/titles
     */
    getTitles: (params: RequestParams = {}) =>
      this.request<ResultListTitle, ResultVoid>({
        path: `/patient/dict/titles`,
        method: "GET",
        ...params,
      }),

    /**
     * No description
     *
     * @tags 公共字典：获取所有科室、职称信息
     * @name GetDepartments
     * @summary 获取科室列表
     * @request GET:/patient/dict/departments
     */
    getDepartments: (params: RequestParams = {}) =>
      this.request<ResultListDepartmentVO, ResultVoid>({
        path: `/patient/dict/departments`,
        method: "GET",
        ...params,
      }),

    /**
     * No description
     *
     * @tags 公共字典：获取所有科室、职称信息
     * @name GetAll
     * @summary 获取所有字典数据：获取科室、职称列表
     * @request GET:/patient/dict/all
     */
    getAll: (params: RequestParams = {}) =>
      this.request<ResultMapStringObject, ResultVoid>({
        path: `/patient/dict/all`,
        method: "GET",
        ...params,
      }),

    /**
     * @description 获取所有科室及医生数量，用于前端下拉筛选
     *
     * @tags 患者端 - 号源展示
     * @name GetAllDepartments
     * @summary 获取科室列表
     * @request GET:/patient/departments
     */
    getAllDepartments: (params: RequestParams = {}) =>
      this.request<ResultListDepartmentVO, ResultVoid>({
        path: `/patient/departments`,
        method: "GET",
        ...params,
      }),

    /**
     * @description 患者软删除自己提交的评价(状态改为隐藏)
     *
     * @tags 患者端 - 就诊评价
     * @name DeleteEvaluation
     * @summary 删除评价
     * @request DELETE:/patient/evaluations/{evaluationId}
     */
    deleteEvaluation: (evaluationId: number, params: RequestParams = {}) =>
      this.request<ResultString, ResultVoid>({
        path: `/patient/evaluations/${evaluationId}`,
        method: "DELETE",
        ...params,
      }),
  };
}
