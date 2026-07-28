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

export interface ResultString {
  /** @format int32 */
  code?: number;
  msg?: string;
  data?: string;
}

/** 问诊室信息返回 VO */
export interface ConsultationRoomVO {
  /**
   * 预约ID
   * @format int64
   */
  appointmentId?: number;
  /**
   * 患者ID
   * @format int64
   */
  patientId?: number;
  /** 患者姓名 */
  patientName?: string;
  /**
   * 医生ID
   * @format int64
   */
  doctorId?: number;
  /** 医生姓名 */
  doctorName?: string;
  /** 科室名称 */
  departmentName?: string;
  /** 排班日期 */
  scheduleDate?: string;
  /**
   * 问诊状态：1-进行中，0-已结束
   * @format int32
   */
  status?: number;
  /** 状态文本 */
  statusText?: string;
  /**
   * 当前在线人数
   * @format int32
   */
  onlineCount?: number;
}

export interface ResultConsultationRoomVO {
  /** @format int32 */
  code?: number;
  msg?: string;
  /** 问诊室信息返回 VO */
  data?: ConsultationRoomVO;
}

/** 聊天消息返回 VO */
export interface ChatMessageVO {
  /**
   * 消息ID
   * @format int64
   */
  messageId?: number;
  /**
   * 发送者ID
   * @format int64
   */
  senderId?: number;
  /** 发送者姓名 */
  senderName?: string;
  /** 发送者角色：PATIENT/DOCTOR */
  senderRole?: string;
  /** 消息内容（已过滤XSS） */
  content?: string;
  /**
   * 发送时间
   * @format date-time
   */
  sendTime?: string;
  /** 是否为自己发送（前端控制气泡方向） */
  isSelf?: boolean;
}

export interface PageInfoChatMessageVO {
  /** @format int64 */
  total?: number;
  list?: ChatMessageVO[];
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

export interface ResultPageInfoChatMessageVO {
  /** @format int32 */
  code?: number;
  msg?: string;
  data?: PageInfoChatMessageVO;
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
      baseURL: axiosConfig.baseURL || "http://localhost:8084",
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
 * @baseUrl http://localhost:8084
 * @contact wzx <2846334903@qq.com> (https://github.com/xiaoyuwzx)
 *
 * 患者端 / 医生端 / 管理端 接口文档
 */
export class Api<
  SecurityDataType extends unknown,
> extends HttpClient<SecurityDataType> {
  consultation = {
    /**
     * @description 患者或医生主动结束问诊，修改预约状态为已完成
     *
     * @tags WebSocket 在线问诊
     * @name EndConsultation
     * @summary 结束问诊
     * @request POST:/consultation/end/{appointmentId}
     */
    endConsultation: (appointmentId: number, params: RequestParams = {}) =>
      this.request<ResultString, ResultVoid>({
        path: `/consultation/end/${appointmentId}`,
        method: "POST",
        ...params,
      }),

    /**
     * @description 返回患者/医生信息、在线人数等
     *
     * @tags WebSocket 在线问诊
     * @name GetRoomInfo
     * @summary 获取问诊室信息
     * @request GET:/consultation/room/{appointmentId}
     */
    getRoomInfo: (appointmentId: number, params: RequestParams = {}) =>
      this.request<ResultConsultationRoomVO, ResultVoid>({
        path: `/consultation/room/${appointmentId}`,
        method: "GET",
        ...params,
      }),

    /**
     * @description 分页加载历史聊天记录
     *
     * @tags WebSocket 在线问诊
     * @name GetHistory
     * @summary 获取聊天历史
     * @request GET:/consultation/history/{appointmentId}
     */
    getHistory: (
      appointmentId: number,
      query?: {
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
      this.request<ResultPageInfoChatMessageVO, ResultVoid>({
        path: `/consultation/history/${appointmentId}`,
        method: "GET",
        query: query,
        ...params,
      }),
  };
}
