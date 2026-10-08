# Lyra5GC

Java 实现的 5GC SBI 网元,参照 Open5GS 源码与 3GPP TS 29.500 / TS 29.510 规范。

## 软件架构

Maven 多模块工程(Java 17 + Spring Boot 3 + WebFlux/Reactor Netty,SBI 采用 HTTP/2 cleartext,兼容 HTTP/1.1):

```
lyra5-gc (parent)
├── common   # SBI 公共层:TS 29.510 模型(NFProfile/NFService/SearchResult/
│            #   SubscriptionData/NotificationData/PatchItem/ProblemDetails)、
│            #   枚举(NfType/NfStatus/...)、SBI 常量(3gpp-Sbi-* 头)、
│            #   DiscoveryOption 及 3gpp-Sbi-Discovery-* 头解析器
├── nrf      # NRF 网元:Nnrf_NFManagement + Nnrf_NFDiscovery
├── scp      # SCP 网元:SBI 代理(间接通信 + 委托发现)
├── amf      # AMF 网元:启动注册 NRF + 周期心跳 + 去注册;含 NRF 调用测试接口
└── ausf     # AUSF 网元:启动注册 NRF + 周期心跳 + 去注册
```

### AMF(默认端口 8081)/ AUSF(默认端口 8082)

- 启动后构建自身 NFProfile(AMF:namf-comm/namf-evts/namf-mt/namf-loc;
  AUSF:nausf-auth/nausf-sorprotection/nausf-upuprotection),PUT 注册到 NRF,
  按 heartBeatTimer 周期发送 PATCH 心跳,心跳失败自动重新注册,关闭时 DELETE 去注册
  (客户端逻辑在 `common` 的 `NfRegistrationClient` / `SelfNfRegistrar`,对应
  Open5GS `lib/sbi/nnrf-build.c` 与 `lib/sbi/nf-sm.c`)
- AMF 测试接口(`/test`,演示 AMF 调 NRF):
  - `GET /test/self` 本端 NFProfile
  - `GET /test/discover?targetNfType=AUSF&serviceNames=nausf-auth` Nnrf_NFDiscovery
  - `GET /test/nf-instances` / `GET /test/nf-instances/{id}` 列表/Profile 查询
  - `POST /test/heartbeat` 手动触发一次心跳

### NRF(默认端口 7777)

对应 Open5GS `src/nrf/`:

- `PUT /nnrf-nfm/v1/nf-instances/{id}` NFRegister:校验 NFInstanceId/NFType/NFStatus、
  heartBeatTimer>0、PLMN 白名单;首次 201+Location,重复注册(完全替换)200;
  支持 `nfProfileChangesSupportInd` 差异响应(TS 29.510 Annex B)
- `PATCH /nnrf-nfm/v1/nf-instances/{id}` NFUpdate/心跳(`/nfStatus`、`/load`、`/plmnList`)
- `DELETE /nnrf-nfm/v1/nf-instances/{id}` NFDeregister
- `GET /nnrf-nfm/v1/nf-instances[/{id}]` NFListRetrieval(3gppHal+json)/NFProfileRetrieval
- `POST|PATCH|DELETE /nnrf-nfm/v1/subscriptions[/{id}]` NFStatus 订阅(validityTime 定时器)
- `GET /nnrf-disc/v1/nf-instances` NFDiscovery:target-nf-type、requester-nf-type、
  service-names、snssais、dnn、target-plmn-list、limit 等过滤,返回 SearchResult +
  `Cache-Control: max-age`
- 心跳超时(heartBeatTimer + margin)自动剔除并广播 `NF_DEREGISTERED`;
  注册/去注册向匹配订阅者 POST `NotificationData`(`3gpp-Sbi-Callback: Nnrf_NFManagement_NFStatusNotify`)

### SCP(默认端口 7778)

对应 Open5GS `src/scp/sbi-path.c`:

1. 带 `3gpp-Sbi-Target-apiRoot` → 直接转发
2. 带 `3gpp-Sbi-Discovery-target-nf-type` + `3gpp-Sbi-Discovery-service-names` →
   本地缓存命中则直接转发,否则向 NRF 发起委托发现(Nnrf_NFDiscovery)、
   缓存结果后转发;无匹配返回 504
3. 转发时剥离 `3gpp-Sbi-Discovery-*`/`3gpp-Sbi-Target-apiRoot` 头,响应附带
   `3gpp-Sbi-Producer-Id`
4. 支持 `3gpp-Sbi-Nrf-Uri` 头指定 NRF

## 安装与构建

```bash
mvn clean package
```

产物:`nrf/target/nrf-1.0.0-SNAPSHOT.jar`、`scp/target/scp-1.0.0-SNAPSHOT.jar`

## 使用说明

```bash
java -jar nrf/target/nrf-1.0.0-SNAPSHOT.jar    # :7777
java -jar scp/target/scp-1.0.0-SNAPSHOT.jar    # :7778,scp.nrf-uri 指向 NRF
java -jar amf/target/amf-1.0.0-SNAPSHOT.jar    # :8081,自动注册到 NRF
java -jar ausf/target/ausf-1.0.0-SNAPSHOT.jar  # :8082,自动注册到 NRF
```

配置见各模块 `src/main/resources/application.yml`(服务 PLMN、心跳间隔、
订阅有效期、NRF 地址等,均可用环境变量/命令行覆盖,如 `--nrf.heartbeat-interval=10`)。

快速验证(HTTP/1.1;生产对接请用 HTTP/2,如 `curl --http2-prior-knowledge`):

```bash
# NFRegister
curl -X PUT http://127.0.0.1:7777/nnrf-nfm/v1/nf-instances/<uuid> \
  -H "Content-Type: application/json" -H "User-Agent: AMF-local" \
  -d '{"nfInstanceId":"<uuid>","nfType":"AMF","nfStatus":"REGISTERED",
       "heartBeatTimer":60,"ipv4Addresses":["127.0.0.1"]}'

# NFDiscovery
curl "http://127.0.0.1:7777/nnrf-disc/v1/nf-instances?target-nf-type=AMF&requester-nf-type=SMF"

# 经 SCP 委托发现并转发
curl http://127.0.0.1:7778/namf-comm/v1/... \
  -H "User-Agent: SMF-local" \
  -H "3gpp-Sbi-Discovery-target-nf-type: AMF" \
  -H "3gpp-Sbi-Discovery-service-names: namf-comm"
```
