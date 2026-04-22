# PROJECT PLAN - Healthcare Platform MVP

## 1. Muc tieu va dau ra
- Xay dung MVP theo microservices cho luong dat lich kham.
- Giu repo o trang thai co the phat trien theo phase, khong lam monolith tam.
- Dau ra cua phase MVP gom backend 5 service, frontend booking flow, bo test co the chay local, va tai lieu ky thuat/nghiep vu dong bo.

## 2. Assumption da khoa
- Stack chung: Java 21, Spring Boot 3.x, Maven, Docker Compose, PostgreSQL, Redis, RabbitMQ, React + Vite.
- API Gateway co trach nhiem verify JWT va forward `X-User-Id`, `X-User-Role`.
- `SKILL.md`, `BA.md`, `TESTING.md`, `PROJECT_PLAN.md` la bo tai lieu song hanh voi code va phai duoc cap nhat khi contract doi.
- MVP chi cover `auth`, `user`, `doctor`, `appointment`, `notification` va frontend cho benh nhan.

## 3. Ke hoach phase

| Phase | Muc tieu | Dau ra bat buoc | Phu thuoc |
| --- | --- | --- | --- |
| Phase 0 | Chuan bi nen ky thuat | Maven parent, module skeleton, Docker Compose, env convention, API convention, docs nen | Khong |
| Phase 1 | Xay auth va user | Dang ky/dang nhap/refresh, profile benh nhan, danh sach bac si, Postman auth-user | Phase 0 |
| Phase 2 | Xay doctor va appointment | CRUD slot, dat lich, huy lich, doi lich, confirm lich, locking strategy | Phase 1 |
| Phase 3 | Xay notification va frontend | Consumer event, notification view, React booking flow, route guard, API service layer | Phase 2 |
| Phase 4 | Hardening va test matrix | Unit/integration test bo sung, smoke test, doc hardening, backlog phase sau | Phase 3 |

## 4. Cong viec chi tiet theo phase

### Phase 0
- Tao `backend/pom.xml` parent va module cho 5 service MVP.
- Tao `shared/api-contract`, `shared/common`, `shared/security-contract`.
- Tao `docker/compose.yml` voi PostgreSQL, Redis, RabbitMQ.
- Chot convention env: `APP_PORT`, `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, `RABBITMQ_URL`.
- Chot response envelope, error code, role, enum.
- Ghi lai guardrail SOLID va refactor backlog trong `SOLID_GUIDELINES.md`.

### Phase 1
- Implement `auth-service` voi register patient, login patient/doctor, refresh, logout, JWT issue.
- Implement `user-service` voi profile benh nhan, doctor directory, doctor detail.
- Tao collection Postman cho auth va user.
- Cap nhat seed data local cho patient va doctor.

### Phase 2
- Implement `doctor-service` voi tao/sua/block slot, liet ke slot trong.
- Implement `appointment-service` voi booking, detail, cancel, reschedule, confirm.
- Them optimistic locking cho booking slot.
- Publish event appointment len RabbitMQ.

### Phase 3
- Implement `notification-service` consume event va luu notification.
- Tao frontend React + Vite voi login, doctor list, doctor detail, dat lich, chi tiet lich hen, thong bao.
- Tich hop auth session, API wrapper, route guard, CSS Modules.

### Phase 4
- Viet unit test va integration test bo sung cho booking, auth, notification.
- Hoan thien collection Postman va smoke flow.
- Ra soat lai docs va backlog cho 7 service future scope.

## 5. Milestone va tieu chi qua cong

| Milestone | Dieu kien hoan thanh |
| --- | --- |
| M0 - Foundation ready | Module skeleton, Docker Compose va docs nen da co |
| M1 - Auth/User ready | Patient dang nhap duoc va xem duoc doctor directory |
| M2 - Booking ready | Patient dat lich duoc, doctor xac nhan duoc, conflict slot duoc chan |
| M3 - MVP UI ready | Frontend hoan thanh booking flow va xem notification |
| M4 - MVP hardening | Co smoke test, regression checklist, docs dong bo |

## 6. Dependency quan trong
- `user-service` phu thuoc `auth-service` de xac dinh danh tinh va role.
- `appointment-service` phu thuoc `doctor-service` de khoa/giai phong slot.
- `notification-service` phu thuoc event tu `appointment-service`.
- Frontend phu thuoc contract on dinh cua `auth`, `user`, `doctor`, `appointment`, `notification`.

## 7. Quy trinh branch va task
- Dung `main` lam nhanh on dinh.
- Moi cong viec phat trien dung feature branch ngan:
  - `feature/auth-login`
  - `feature/doctor-timeslot`
  - `feature/frontend-booking`
  - `docs/ba-mvp`
  - `test/postman-appointment`
- Moi branch chi gom mot nhom thay doi dong nhat.
- Merge thong qua pull request sau khi docs, test va contract duoc ra soat.

## 8. Definition of Done cap du an
- Code build duoc o muc module lien quan.
- API contract khop voi `BA.md` va `SKILL.md`.
- Test case lien quan da duoc bo sung vao `TESTING.md` va collection Postman.
- Khong co flow nghiep vu MVP nao bi mo ta mau thuan giua docs va code.

## 9. Rui ro va cach giam thieu

| Rui ro | Anh huong | Giam thieu |
| --- | --- | --- |
| Booking race condition | Double booking | Optimistic locking + integration test conflict |
| Contract thay doi khong dong bo | Frontend va Postman vo | Chot response envelope, update docs cung luc |
| Service phu thuoc chat che | Kho debug local | Tach shared contract nho, timeout ro rang, event cho notification |
| MVP phong to nhanh | Cham tien do | Khoa scope 5 service + booking flow |

## 10. Backlog sau MVP
- `medical-record-service`
- `payment-service`
- `prescription-service`
- `review-service`
- `report-service`
- `admin-service`
- `ai-service`

## 11. Thu tu uu tien sau MVP
1. `medical-record-service` vi lien quan truc tiep den buoi kham sau booking.
2. `payment-service` de chot luong thu phi kham.
3. `prescription-service` de hoan thanh luong sau kham.
4. `review-service` de dong vong phan hoi.
5. `admin-service` va `report-service` de van hanh.
6. `ai-service` sau khi co du lieu va flow on dinh.
