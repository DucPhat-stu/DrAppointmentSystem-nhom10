# SOLID GUIDELINES - Healthcare Platform MVP

## 1. Muc dich
- Giu skeleton Phase 0 phat trien tiep ma khong lam shared module phinh to.
- Chot guardrail de cac task Phase 1-4 van dung huong tach service, tach domain va khong pha contract MVP da khoa.
- Bao toan logic hien co cua du an:
  - 5 service MVP: `auth`, `user`, `doctor`, `appointment`, `notification`
  - response envelope va `ErrorCode` thong nhat
  - gateway verify JWT va downstream nhan forwarded identity
  - database tach theo service

## 2. Danh gia hien tai
- Skeleton hien tai da co huong tot o muc module-level:
  - co multi-module Maven
  - co shared contract rieng
  - co service tach database va config
- Tuy nhien, muc do tuan thu SOLID moi o muc mot phan.
- Rui ro chinh nam o viec `shared/common` dang tro thanh noi chua ca web policy, controller, request context va exception strategy cho tat ca service.

## 3. Refactor backlog uu tien

### P1 - Tach shared transport khoi domain enum
- Giu `ApiResponse`, `ApiErrorResponse`, `ApiMeta`, `ErrorCode` trong `shared/api-contract`.
- Dua `AppointmentStatus` vao module/domain cua `appointment-service`.
- Dua `TimeSlotStatus` vao module/domain cua `doctor-service`.
- Chi tao module contract rieng neu co nhu cau chia se that su giua nhieu service, vi du `appointment-contract`.

**Ly do SOLID**
- SRP: module transport khong nen gan domain cua mot service cu the.
- OCP: them status moi cho appointment khong nen buoc service khac rebuild va nhan coupling thua.

### P1 - Lam `shared/common` thanh infrastructure library, khong phai application layer
- Di chuyen `FoundationController` ra tung service hoac mot auto-configuration opt-in.
- `GlobalExceptionHandler` chi nen o shared neu duoc thiet ke thanh extension point; neu khong, moi service tu so huu ban mapping rieng.
- `shared/common` chi giu:
  - request metadata abstraction
  - logging helper
  - utility khong chua route business

**Ly do SOLID**
- SRP: shared module khong nen vua lo infra vua lo HTTP behavior.
- DIP: service nen phu thuoc abstraction, khong bi shared module ap dat endpoint/policy.

### P2 - Bo static context de de test va de mo rong async
- Thay `RequestContext` static `ThreadLocal` bang abstraction nhu `RequestMetadataProvider`.
- Inject `Clock` hoac metadata provider vao `ApiResponseFactory`.
- Trong luong async va message consumer, metadata duoc tao/nap ro rang thay vi ngua vao context MVC.

**Ly do SOLID**
- DIP: domain/application code phu thuoc interface thay vi static global state.
- SRP: response factory chi build response, khong tu quan ly cach lay request context va timestamp.

### P2 - Tinh sach security phai la local policy cua service
- `auth-service` co the tam thoi mo toan bo endpoint trong Phase 0, nhung can co TODO ro rang va switch sang policy that khi vao Phase 1.
- Khong dua security policy mac dinh vao shared module.
- Downstream service can abstraction doc forwarded identity, nhung khong couple vao implementation cua `auth-service`.

**Ly do SOLID**
- SRP: shared security contract chi nen mo ta role/header, khong quyet dinh runtime security policy.
- ISP: downstream service chi can contract identity toi thieu.

### P3 - Frontend tach auth session persistence khoi UI route
- Tao `sessionStorageAdapter` rieng.
- `AuthContext` chi quan ly state va action, khong truc tiep goi Web Storage.
- `LoginPage` khong hardcode mock session khi bat dau tich hop `auth-service`; doi sang auth service adapter.

**Ly do SOLID**
- SRP: provider khong vua lam state container vua lam persistence adapter.
- DIP: UI phu thuoc auth service abstraction, khong phu thuoc thang vao storage va mock data.

## 4. Guardrail cho task sau

### 4.1 Shared module
- Khong dua entity, repository, business rule cua tung service vao `shared/*`.
- Khong dua controller business vao `shared/common`.
- Khong dua enum domain vao `shared/api-contract` neu no chi thuoc mot bounded context.

### 4.2 Service module
- Moi service so huu:
  - controller
  - application service/use case
  - domain model/rule
  - persistence adapter
  - exception mapping neu can behavior rieng
- Service duoc phep tai su dung envelope/error code chung, nhung khong duoc day domain rule nguoc vao shared.

### 4.3 Dependency direction
- Controller -> application -> domain
- Persistence/message/web framework la adapter bao quanh domain, khong de domain phu thuoc Spring hay HTTP.
- Shared contract la dependency one-way; service domain khong duoc bi "rut nguoc" ve shared cho tien.

### 4.4 Contract stability
- Giu on dinh:
  - response envelope
  - `ErrorCode`
  - role contract
  - env convention da chot trong docs
- Neu can mo rong contract, uu tien them type/adapter moi thay vi sua shared type hien co theo cach gay coupling.

## 5. Checklist review truoc khi merge task moi
- Co file nao trong `shared/*` dang chua logic chi thuoc mot service khong?
- Controller/service moi co dang phu thuoc truc tiep vao storage, HTTP request hoac framework detail khong?
- Co static helper nao dang om state toan cuc, lam kho test hoac kho xu ly async khong?
- Them enum/DTO moi co that su la cross-service contract hay chi la domain noi bo?
- Security/config policy moi co dang bi dat o shared thay vi local service khong?
- Thay doi co giu nguyen logic MVP da khoa trong `BA.md`, `SKILL.md`, `TESTING.md`, `PROJECT_PLAN.md` khong?

## 6. Thu tu thuc hien de khong vo logic hien tai
1. Tach domain enum khoi `shared/api-contract`.
2. Rut `FoundationController` khoi `shared/common`, giu endpoint tam bang service-local controller.
3. Truu tuong hoa request metadata va response factory.
4. Sau do moi dua logic Phase 1 vao tung service.

Thu tu nay giu duoc logic skeleton hien co, dong thoi giam coupling truoc khi code business tang len.
