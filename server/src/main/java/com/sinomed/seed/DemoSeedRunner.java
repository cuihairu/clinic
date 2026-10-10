package com.sinomed.seed;

import com.sinomed.entity.AppointmentEntity;
import com.sinomed.entity.CustomerEntity;
import com.sinomed.entity.ItemEntity;
import com.sinomed.entity.OrderEntity;
import com.sinomed.entity.PrescriptionEntity;
import com.sinomed.entity.PrescriptionItemEntity;
import com.sinomed.entity.RechargeEntity;
import com.sinomed.entity.ReviewCustomerEntity;
import com.sinomed.entity.ReviewEntity;
import com.sinomed.entity.ReviewStaffEntity;
import com.sinomed.entity.SignEntity;
import com.sinomed.entity.StaffEntity;
import com.sinomed.entity.TreatEntity;
import com.sinomed.repository.AppointmentRepository;
import com.sinomed.repository.CustomerRepository;
import com.sinomed.repository.ItemRepository;
import com.sinomed.repository.HerbRepository;
import com.sinomed.entity.HerbEntity;
import com.sinomed.repository.OrderRepository;
import com.sinomed.repository.PrescriptionItemRepository;
import com.sinomed.repository.PrescriptionRepository;
import com.sinomed.repository.RechargeRepository;
import com.sinomed.repository.ReviewCustomerRepository;
import com.sinomed.repository.ReviewRepository;
import com.sinomed.repository.ReviewStaffRepository;
import com.sinomed.repository.SignRepository;
import com.sinomed.repository.StaffRepository;
import com.sinomed.repository.TreatRepository;
import com.sinomed.util.DateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 演示种子数据（全部为虚构脱敏人名与内容，仅用于在线演示环境）。
 *
 * <p>由环境变量 DEMO_SEED=true 开关（compose 演示环境默认开启，开发环境默认关闭）。
 * 逐表幂等：以自然键（账号 / 手机号 / 卡项名 / 复盘日）判重，已有数据即跳过，
 * 重复启动不会产生重复数据；某一节失败只记录日志、不阻断服务启动，下次启动会补齐缺项。
 *
 * <p>说明：广告素材模块源码尚未实现，暂无对应种子数据（如实留空）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "demo.seed", havingValue = "true")
public class DemoSeedRunner implements ApplicationRunner {

    private final StaffRepository staffRepository;
    private final AppointmentRepository appointmentRepository;
    private final RechargeRepository rechargeRepository;
    private final OrderRepository orderRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final PrescriptionItemRepository prescriptionItemRepository;
    private final CustomerRepository customerRepository;
    private final TreatRepository treatRepository;
    private final ItemRepository itemRepository;
    private final HerbRepository herbRepository;
    private final ReviewRepository reviewRepository;
    private final ReviewCustomerRepository reviewCustomerRepository;
    private final ReviewStaffRepository reviewStaffRepository;
    private final SignRepository signRepository;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        try {
            seedStaff();
            seedItems();
            seedHerbs();
            Map<String, Long> customers = seedCustomers();
            seedTreats(customers);
            seedReviews();
            seedReviewCustomers(customers);
            seedReviewStaffs();
            seedSigns();
            seedAppointments(customers);
            seedBilling(customers);
            seedPrescriptions(customers);
            log.info("演示种子数据检查完成（逐表幂等，已有数据自动跳过）；"
                    + "广告素材模块源码未实现，无种子数据");
        } catch (Exception e) {
            // 播种失败不阻断启动：演示站优先可用，下次启动按自然键补齐缺项
            log.error("演示种子数据失败（服务继续启动）", e);
        }
    }

    /** 员工：馆长 + 中医师 + 前台，密码统一为演示账号 123（bcrypt） */
    private void seedStaff() {
        record StaffSeed(String name, String account, int role, int gender, int age,
                         String phone, String address, int birthYear, int birthMonth, int birthDay) {}
        List<StaffSeed> seeds = List.of(
                new StaffSeed("顾景明", "gu", 0, 1, 45, "13800000001", "南京市秦淮区", 1981, 4, 18),
                new StaffSeed("沈知远", "shen", 99, 1, 38, "13800000002", "南京市秦淮区", 1988, 9, 3),
                new StaffSeed("苏文若", "su", 99, 0, 26, "13800000003", "南京市秦淮区", 2000, 2, 11)
        );
        for (StaffSeed s : seeds) {
            if (staffExists(s.account())) {
                continue;
            }
            StaffEntity staff = StaffEntity.getInstanceWithDefault(s.account());
            staff.setName(s.name());
            staff.setRole(s.role());
            staff.setGender(s.gender());
            staff.setAge(s.age());
            staff.setPhone(s.phone());
            staff.setAddress(s.address());
            staff.setBirthday(Date.from(LocalDate.of(s.birthYear(), s.birthMonth(), s.birthDay())
                    .atStartOfDay(ZoneId.systemDefault()).toInstant()));
            staff.setPassword(passwordEncoder.encode("123"));
            staffRepository.save(staff);
        }
        log.info("种子·员工：检查完成");
    }

    /** 卡项（服务项目）：单次体验与次卡/季卡组合 */
    private void seedItems() {
        record ItemSeed(String name, int price, String description) {}
        List<ItemSeed> seeds = List.of(
                new ItemSeed("中医体质辨识（单次）", 198,
                        "四诊合参体质辨识一次，含辨识报告与调理建议。"),
                new ItemSeed("经络推拿（10 次卡）", 680,
                        "经络推拿十次卡，有效期一年，可指定技师。"),
                new ItemSeed("艾灸温阳调理（5 次卡）", 480,
                        "艾灸温阳调理五次卡，含灸后温阳茶饮。"),
                new ItemSeed("四季膏方调理（季卡）", 1280,
                        "四季膏方调理季度服务，含辨证开方与两次复诊调整。")
        );
        int sort = 0;
        for (ItemSeed s : seeds) {
            if (itemExists(s.name())) {
                continue;
            }
            ItemEntity item = new ItemEntity();
            item.setName(s.name());
            item.setPrice(s.price());
            item.setDescription(s.description());
            // enabled/sort 为 NOT NULL 列（ItemView 默认 enabled=1/sort=0），种子直插实体需显式带上
            item.setEnabled(1);
            item.setSort(++sort);
            itemRepository.save(item);
        }
        log.info("种子·卡项：检查完成");
    }

    /** 顾客建档：虚构脱敏人名，手机号取明显虚构号段 */
    private Map<String, Long> seedCustomers() {
        record CustomerSeed(String name, int gender, int age, int level,
                            String phone, String address, int birthYear, int birthMonth, int birthDay,
                            int registeredDaysAgo) {}
        List<CustomerSeed> seeds = List.of(
                new CustomerSeed("王慕清", 0, 34, 2, "13900000001", "南京市秦淮区", 1992, 3, 14, 30),
                new CustomerSeed("李景和", 1, 47, 3, "13900000002", "苏州市姑苏区", 1979, 8, 2, 14),
                new CustomerSeed("陈屿", 1, 29, 1, "13900000003", "杭州市上城区", 1997, 11, 26, 21),
                new CustomerSeed("赵蘅", 0, 52, 3, "13900000004", "泰州市海陵区", 1974, 5, 9, 10),
                new CustomerSeed("孙泽宇", 1, 41, 1, "13900000005", "无锡市梁溪区", 1985, 1, 20, 5),
                new CustomerSeed("吴清禾", 0, 38, 2, "13900000006", "常州市天宁区", 1988, 7, 7, 16)
        );
        Map<String, Long> byPhone = new java.util.HashMap<>();
        for (CustomerSeed s : seeds) {
            Long existing = customerByPhone(s.phone());
            if (existing != null) {
                byPhone.put(s.phone(), existing);
                continue;
            }
            CustomerEntity customer = new CustomerEntity();
            customer.setName(s.name());
            customer.setGender(s.gender());
            customer.setAge(s.age());
            customer.setLevel(s.level());
            customer.setPhone(s.phone());
            customer.setAddress(s.address());
            customer.setBirthday(Date.from(LocalDate.of(s.birthYear(), s.birthMonth(), s.birthDay())
                    .atStartOfDay(ZoneId.systemDefault()).toInstant()));
            customerRepository.save(customer);
            // 建档时间回写过去 N 天，顾客档案「最近到店」/每日报表「新客建档」才有真实时间线
            backdateCustomer(customer.getId(), Date.from(LocalDate.now().minusDays(s.registeredDaysAgo())
                    .atStartOfDay(ZoneId.systemDefault()).toInstant()));
            byPhone.put(s.phone(), customer.getId());
        }
        log.info("种子·顾客：检查完成");
        return byPhone;
    }

    /**
     * 中医诊疗记录：主诉 / 望闻问切 / 脉象 / 取穴 / 诊断与方案，按顾客各一条、可复诊调阅。
     * 五行、难经等结构化字段留空（演示数据不硬造术语），由医生实际接诊时录入。
     */
    private void seedTreats(Map<String, Long> customers) {
        Long wang = customers.get("13900000001");
        Long li = customers.get("13900000002");
        Long zhao = customers.get("13900000004");
        if (wang != null) {
            treatIfAbsent(wang, daysAgoAt(14),
                    "近一月夜寐不安，多梦易醒，晨起头昏乏力。",
                    "饮食尚可，二便调，经量偏少色淡，久坐伏案。",
                    "面色少华，舌淡苔薄白，唇色偏淡。",
                    "腹软无压痛，腰骶轻叩痛（-）。",
                    "细",
                    "左关脉弦细，尺脉沉",
                    "右寸脉弱，关脉平",
                    "百会、内关、神门",
                    "三阴交、足三里、太冲",
                    "不寐——心脾两虚",
                    "归脾汤加减内服；配合耳穴压豆（神门、心、脾），两周后复诊。",
                    "忌浓茶咖啡，戌时后少食，睡前热水泡脚 15 分钟。",
                    "首次针灸留针 25 分钟，灸后温阳茶饮一杯。");
        }
        if (li != null) {
            treatIfAbsent(li, daysAgoAt(7),
                    "腰部酸痛两月，久坐加重，俯仰不利，无下肢放射痛。",
                    "久坐司机，喜卧软床，二便调。",
                    "舌暗红苔薄白，腰肌紧张。",
                    "腰 4/5 棘突旁压痛（+），直腿抬高试验（-）。",
                    "沉紧",
                    "左尺脉沉",
                    "右尺脉沉，关脉弦",
                    "肾俞、腰阳关、委中",
                    "大肠俞、阿是穴、昆仑",
                    "腰痛——肾虚络瘀",
                    "独活寄生汤加减内服；配合温针灸与推拿，一周两次，共三次。",
                    "避免久坐寒湿，护腰佩戴，睡硬板床。",
                    "本次推拿松解腰背 30 分钟，温针灸 20 分钟。");
        }
        if (zhao != null) {
            treatIfAbsent(zhao, daysAgoAt(2),
                    "咽干微痛反复三月，晨起有痰，久语加重。",
                    "用嗓较多，喜辛辣，夜间口干，二便调。",
                    "咽部黏膜偏暗红，舌红少津苔薄。",
                    "咽部充血（+），甲状腺未及肿大。",
                    "细数",
                    "左寸脉细数",
                    "右寸脉细数，关脉滑",
                    "列缺、照海、天突",
                    "合谷、鱼际、廉泉",
                    "慢喉痹——肺阴不足",
                    "养阴清肺汤加减内服；忌辛辣煎炸，两周后复诊评估。",
                    "少食辛辣，室内保湿，罗汉果胖大海代茶饮。",
                    "本次穴位贴敷（天突、列缺），嘱声休。");
        }
        log.info("种子·诊疗记录：检查完成");
    }

    /** 每日复盘：昨日一条，沉淀到报表 */
    private void seedReviews() {
        Date yesterday = daysAgo(1);
        boolean exists = reviewRepository.findAll().stream()
                .anyMatch(r -> r.getDay() != null && DateUtil.isSomeDay(r.getDay(), yesterday));
        if (exists) {
            return;
        }
        ReviewEntity review = new ReviewEntity();
        review.setDay(yesterday);
        review.setGood("今日到店 6 位，三位老客复诊记录均已完善；次卡核销 3 次。");
        review.setImprovement("午间高峰前台排队偏长，下周把预约时段打散；顾客回访有 1 位未接通，改约明日再访。");
        reviewRepository.save(review);
        log.info("种子·每日复盘：已写入");
    }

    /** 顾客回访：一条待办（回访日=明日）+ 一条已回访 */
    private void seedReviewCustomers(Map<String, Long> customers) {
        Long wang = customers.get("13900000001");
        Long li = customers.get("13900000002");
        if (wang != null && !reviewCustomerExists(wang, daysAhead(1))) {
            ReviewCustomerEntity rc = new ReviewCustomerEntity();
            rc.setName("王慕清");
            rc.setCustomerId(wang);
            rc.setLast(daysAgo(3));
            rc.setDay(daysAhead(1)); // 明日回访：出现在每日报表待办
            rc.setAdvice("复诊后回访：询问睡眠改善情况，提醒调理卡月底到期可续。");
            reviewCustomerRepository.save(rc);
        }
        if (li != null && !reviewCustomerExists(li, daysAgo(1))) {
            ReviewCustomerEntity rc = new ReviewCustomerEntity();
            rc.setName("李景和");
            rc.setCustomerId(li);
            rc.setLast(daysAgo(9));
            rc.setDay(daysAgo(1)); // 昨日回访：已完成
            rc.setAdvice("已回访：腰痛明显缓解，嘱继续硬板床与腰背锻炼，下周复诊。");
            reviewCustomerRepository.save(rc);
        }
        log.info("种子·顾客回访：检查完成");
    }

    /** 员工日复盘：昨日一条（耗卡/已做/建议） */
    private void seedReviewStaffs() {
        Date yesterday = daysAgo(1);
        Long shen = staffByAccount("shen");
        if (shen == null || reviewStaffExists(shen, yesterday)) {
            return;
        }
        ReviewStaffEntity rs = new ReviewStaffEntity();
        rs.setStaffId(shen);
        rs.setName("沈知远");
        rs.setCost(3);
        rs.setDone("卡项已有：次卡 12 张、季卡 2 张；今日做：推拿 2、艾灸 1。");
        rs.setAdvice("王慕清调理卡月底到期，建议本周沟通续季卡；新客陈屿体质辨识后可引导办次卡。");
        rs.setDay(yesterday);
        reviewStaffRepository.save(rs);
        log.info("种子·员工复盘：已写入");
    }

    /** 考勤（诊所运营）：今日上班打卡各一条 */
    /** 预约：今日 3 条（1 条已到店）+ 明日 1 条 + 昨日取消 1 条，逐条按顾客+当日判重 */
    private void seedAppointments(Map<String, Long> customers) {
        record ApptSeed(String phone, String staffAccount, String itemName,
                        int dayOffset, int hour, int minute, int duration, int status, String remark) {}
        List<ApptSeed> seeds = List.of(
                new ApptSeed("13900000001", "shen", "艾灸温阳调理（5 次卡）", 0, 9, 30, 60, 1, "艾灸第 4 次复诊，已到店"),
                new ApptSeed("13900000002", null, null, 0, 11, 0, 60, 0, "肩颈不适初诊"),
                new ApptSeed("13900000004", "shen", "经络推拿（10 次卡）", 0, 15, 0, 60, 0, null),
                new ApptSeed("13900000003", "su", "中医体质辨识（单次）", 1, 10, 0, 30, 0, "首次到店体验"),
                new ApptSeed("13900000005", null, null, -1, 16, 0, 60, 9, "临时改期，改约下周")
        );
        for (ApptSeed a : seeds) {
            Long customerId = customers.get(a.phone());
            if (customerId == null || appointmentOnDayExists(customerId, a.dayOffset())) {
                continue;
            }
            AppointmentEntity appointment = new AppointmentEntity();
            appointment.setCustomerId(customerId);
            appointment.setStaffId(a.staffAccount() == null ? null : staffByAccount(a.staffAccount()));
            appointment.setItemId(a.itemName() == null ? null : itemByName(a.itemName()));
            appointment.setStartTime(at(a.dayOffset(), a.hour(), a.minute()));
            appointment.setDuration(a.duration());
            appointment.setStatus(a.status());
            appointment.setRemark(a.remark());
            appointmentRepository.save(appointment);
        }
        log.info("种子·预约：检查完成");
    }

    /** 收费结算演示数据：两笔储值充值 + 两条今日自助机待结算单（staffId 空 = 自助机来源） */
    private void seedBilling(Map<String, Long> customers) {
        record RechargeSeed(String phone, int money, int dayOffset) {}
        for (RechargeSeed r : List.of(
                new RechargeSeed("13900000005", 500, -2),
                new RechargeSeed("13900000001", 2000, -6)
        )) {
            Long customerId = customers.get(r.phone());
            if (customerId == null || rechargeOnDayExists(customerId, r.dayOffset())) {
                continue;
            }
            RechargeEntity recharge = new RechargeEntity();
            recharge.setUserId(customerId);
            recharge.setMoney(r.money());
            Date when = at(r.dayOffset(), 10, 0);
            recharge.setCreateTime(when);
            RechargeEntity saved = rechargeRepository.save(recharge);
            backdateRow("recharges", saved.getId(), when); // @CreatedDate 会覆盖显式时间，回写演示时间线
        }
        log.info("种子·储值：检查完成");

        record KioskOrderSeed(String phone, String itemName, int hour, int minute) {}
        for (KioskOrderSeed k : List.of(
                new KioskOrderSeed("13900000005", "艾灸温阳调理（5 次卡）", 10, 20),
                new KioskOrderSeed("13900000002", "经络推拿（10 次卡）", 11, 5)
        )) {
            Long customerId = customers.get(k.phone());
            Long itemId = k.itemName() == null ? null : itemByName(k.itemName());
            if (customerId == null || itemId == null || kioskOrderOnDayExists(customerId, itemId, 0)) {
                continue;
            }
            OrderEntity order = new OrderEntity();
            order.setUserId(customerId);
            order.setItemId(itemId);
            order.setStaffId(null); // 自助机单，无接待员工
            order.setStatus(0); // 已下单，待前台结算
            ItemEntity item = itemRepository.findById(itemId).orElse(null);
            order.setPrice(item == null ? null : item.getPrice()); // 下单时刻价格快照
            orderRepository.save(order); // create_time 由审计写为今天，幂等判重按当日即可
        }
        log.info("种子·待结算单：检查完成");
    }

    /** 中药处方演示数据：王慕清昨日一张逍遥散化裁（药材名为自由文本，虚构演示） */
    /** 药材字典：覆盖种子处方 7 味与常用饮片，价格（分/克）为虚构演示值 */
    private void seedHerbs() {
        record HerbPrice(String name, int pricePerGram) {}
        List<HerbPrice> herbs = List.of(
                new HerbPrice("柴胡", 8), new HerbPrice("白芍", 5), new HerbPrice("当归", 12),
                new HerbPrice("茯苓", 4), new HerbPrice("白术", 6), new HerbPrice("薄荷", 3),
                new HerbPrice("炙甘草", 2), new HerbPrice("甘草", 2), new HerbPrice("黄芪", 6),
                new HerbPrice("丹参", 8), new HerbPrice("半夏", 9), new HerbPrice("陈皮", 3),
                new HerbPrice("川芎", 7), new HerbPrice("地黄", 8), new HerbPrice("麦冬", 10));
        if (herbRepository.existsByName(herbs.get(0).name())) {
            return;
        }
        for (HerbPrice herb : herbs) {
            if (herbRepository.existsByName(herb.name())) {
                continue;
            }
            HerbEntity entity = new HerbEntity();
            entity.setName(herb.name());
            entity.setPrice(herb.pricePerGram());
            herbRepository.save(entity);
        }
        log.info("种子·药材字典：检查完成");
    }

    private void seedPrescriptions(Map<String, Long> customers) {
        record HerbSeed(String herb, double weight, String special) {}
        Long customerId = customers.get("13900000001");
        Long staffId = staffByAccount("shen");
        if (customerId == null || prescriptionOnDayExists(customerId, -1)) {
            return;
        }
        List<HerbSeed> herbs = List.of(
                new HerbSeed("柴胡", 12, null),
                new HerbSeed("白芍", 15, null),
                new HerbSeed("当归", 10, null),
                new HerbSeed("茯苓", 15, null),
                new HerbSeed("白术", 12, null),
                new HerbSeed("薄荷", 6, "后下"),
                new HerbSeed("炙甘草", 6, null)
        );
        PrescriptionEntity prescription = new PrescriptionEntity();
        prescription.setCustomerId(customerId);
        prescription.setStaffId(staffId);
        prescription.setDoses(7);
        prescription.setUsage("水煎服，日一剂，早晚温服；代煎 7 袋");
        prescription.setRemark("复诊请带近期睡眠记录");
        prescription = prescriptionRepository.save(prescription);
        for (int i = 0; i < herbs.size(); i++) {
            HerbSeed herb = herbs.get(i);
            PrescriptionItemEntity item = new PrescriptionItemEntity();
            item.setPrescriptionId(prescription.getId());
            item.setHerb(herb.herb());
            item.setWeight(herb.weight());
            item.setSpecial(herb.special());
            item.setSort(i);
            prescriptionItemRepository.save(item);
        }
        backdateRow("prescriptions", prescription.getId(), at(-1, 10, 30)); // 回写昨日开方时间线
        log.info("种子·处方：检查完成");
    }

    private void seedSigns() {
        if (signRepository.count() > 0) {
            return;
        }
        for (String account : List.of("gu", "shen", "su")) {
            Long staffId = staffByAccount(account);
            if (staffId == null) {
                continue;
            }
            SignEntity sign = new SignEntity();
            sign.setStaffId(staffId);
            sign.setType(1); // 1 上班
            signRepository.save(sign);
        }
        log.info("种子·考勤：已写入今日上班打卡");
    }

    // ---------- 幂等判重与工具 ----------

    private Long treatIfAbsent(Long customerId, Date createTime, String desc, String inquiry,
                               String observation, String palpation, String pulse,
                               String pulseLeft, String pulseRight,
                               String acupointLeft, String acupointRight,
                               String diagnose, String plan, String diet, String conditioning) {
        boolean exists = treatRepository.findAll().stream()
                .anyMatch(t -> customerId.equals(t.getCustomerId()));
        if (exists) {
            return null;
        }
        TreatEntity treat = new TreatEntity();
        treat.setCustomerId(customerId);
        treat.setDesc(desc);
        treat.setInquiry(inquiry);
        treat.setObservation(observation);
        treat.setPalpation(palpation);
        treat.setPulse(pulse);
        treat.setPulseLeft(pulseLeft);
        treat.setPulseRight(pulseRight);
        treat.setAcupointLeft(acupointLeft);
        treat.setAcupointRight(acupointRight);
        treat.setDiagnose(diagnose);
        treat.setPlan(plan);
        treat.setDiet(diet);
        treat.setConditioning(conditioning);
        treat.setCreateTime(createTime);
        TreatEntity saved = treatRepository.save(treat);
        backdate(saved.getId(), createTime); // @CreatedDate 会覆盖显式时间，落库后回写演示时间线
        return saved.getId();
    }

    /**
     * 把诊疗记录的创建时间回写到过去的接诊日，让复诊历史/按日查询有真实时间线。
     * 仅影响演示数据；失败只记日志，不影响其余种子。
     */
    private void backdate(Long treatId, Date createTime) {
        backdateRow("treats", treatId, createTime);
    }

    /** 演示时间线回写通用化：把某行 create_time/update_time 改写到过去；失败只记日志 */
    private void backdateRow(String table, Long id, Date createTime) {
        try {
            jdbcTemplate.update("UPDATE " + table + " SET create_time = ?, update_time = ? WHERE id = ?",
                    new Timestamp(createTime.getTime()), new Timestamp(createTime.getTime()), id);
        } catch (Exception e) {
            log.warn("种子·时间回写失败（表 " + table + "，仅影响演示时间线，不影响功能）", e);
        }
    }

    private void backdateCustomer(Long customerId, Date createTime) {
        try {
            jdbcTemplate.update("UPDATE customers SET create_time = ?, update_time = ? WHERE id = ?",
                    new Timestamp(createTime.getTime()), new Timestamp(createTime.getTime()), customerId);
        } catch (Exception e) {
            log.warn("种子·顾客建档时间回写失败（仅影响演示时间线，不影响功能）", e);
        }
    }

    private boolean staffExists(String account) {
        return staffRepository.findAll().stream().anyMatch(s -> account.equals(s.getAccount()));
    }

    private Long staffByAccount(String account) {
        return staffRepository.findAll().stream()
                .filter(s -> account.equals(s.getAccount()))
                .map(StaffEntity::getId)
                .findFirst().orElse(null);
    }

    private Long customerByPhone(String phone) {
        return customerRepository.findAll().stream()
                .filter(c -> phone.equals(c.getPhone()))
                .map(CustomerEntity::getId)
                .findFirst().orElse(null);
    }

    private boolean itemExists(String name) {
        return itemRepository.findAll().stream().anyMatch(i -> name.equals(i.getName()));
    }

    private Long itemByName(String name) {
        return itemRepository.findAll().stream()
                .filter(i -> name.equals(i.getName()))
                .map(ItemEntity::getId)
                .findFirst().orElse(null);
    }

    /** 该顾客在 offset 天（0=今天）当日是否已有预约（按自然日判重，时段不同也视同已约） */
    private boolean appointmentOnDayExists(Long customerId, int dayOffset) {
        return appointmentRepository.findAll().stream()
                .anyMatch(a -> customerId.equals(a.getCustomerId())
                        && a.getStartTime() != null
                        && DateUtil.isSomeDay(a.getStartTime(), at(dayOffset, 12, 0)));
    }

    /** 该顾客在 offset 天当日是否已有储值流水（按自然日判重） */
    private boolean rechargeOnDayExists(Long customerId, int dayOffset) {
        return rechargeRepository.findAll().stream()
                .anyMatch(r -> customerId.equals(r.getUserId())
                        && r.getCreateTime() != null
                        && DateUtil.isSomeDay(r.getCreateTime(), at(dayOffset, 12, 0)));
    }

    /** 该顾客该卡项当日是否已有自助机单（任意状态，含已结算，避免重启后重复补单） */
    private boolean kioskOrderOnDayExists(Long customerId, Long itemId, int dayOffset) {
        return orderRepository.findAll().stream()
                .anyMatch(o -> customerId.equals(o.getUserId())
                        && itemId.equals(o.getItemId())
                        && o.getCreateTime() != null
                        && DateUtil.isSomeDay(o.getCreateTime(), at(dayOffset, 12, 0)));
    }

    /** 该顾客在 offset 天当日是否已有处方（按自然日判重） */
    private boolean prescriptionOnDayExists(Long customerId, int dayOffset) {
        return prescriptionRepository.findAll().stream()
                .anyMatch(p -> customerId.equals(p.getCustomerId())
                        && p.getCreateTime() != null
                        && DateUtil.isSomeDay(p.getCreateTime(), at(dayOffset, 12, 0)));
    }

    /** 相对当天的某时刻（dayOffset 天后 hour:minute） */
    private Date at(int dayOffset, int hour, int minute) {
        return Date.from(LocalDate.now().plusDays(dayOffset).atTime(hour, minute)
                .atZone(ZoneId.systemDefault()).toInstant());
    }

    private boolean reviewCustomerExists(Long customerId, Date day) {
        return reviewCustomerRepository.findAll().stream()
                .anyMatch(rc -> customerId.equals(rc.getCustomerId())
                        && rc.getDay() != null && DateUtil.isSomeDay(rc.getDay(), day));
    }

    private boolean reviewStaffExists(Long staffId, Date day) {
        return reviewStaffRepository.findAll().stream()
                .anyMatch(rs -> staffId.equals(rs.getStaffId())
                        && rs.getDay() != null && DateUtil.isSomeDay(rs.getDay(), day));
    }

    /** n 天前 */
    private Date daysAgo(int n) {
        return day(LocalDate.now().minusDays((long) n));
    }

    /** n 天前的上午 10:30——诊疗落在营业时间；分页 endTime 为闭区间，00:00 整点会误入前一日的边界 */
    private Date daysAgoAt(int n) {
        return Date.from(LocalDate.now().minusDays((long) n).atTime(10, 30)
                .atZone(ZoneId.systemDefault()).toInstant());
    }

    /** n 天后 */
    private Date daysAhead(int n) {
        return day(LocalDate.now().plusDays((long) n));
    }

    private Date day(LocalDate date) {
        return Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }
}
