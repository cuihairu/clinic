package com.sinomed.service.impl;

import com.sinomed.entity.CustomerEntity;
import com.sinomed.entity.ItemEntity;
import com.sinomed.entity.OrderEntity;
import com.sinomed.entity.SettlementEntity;
import com.sinomed.entity.StaffEntity;
import com.sinomed.repository.CustomerRepository;
import com.sinomed.repository.ItemRepository;
import com.sinomed.repository.OrderRepository;
import com.sinomed.repository.SettlementRepository;
import com.sinomed.repository.StaffRepository;
import com.sinomed.service.PrintTemplateService;
import com.sinomed.service.ReceiptPrintService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Map;

/**
 * 结算小票套打实现：取 receipt 模板（可被 data/printtemplates/receipt.html 覆盖），
 * 按结算单联出订单/顾客/卡项/经手人后逐 token 填充；items 循环体一行（一单一卡项一次结算）。
 * 顾客与员工名等业务字段做 HTML 转义，避免模板被名字注入。
 */
@RequiredArgsConstructor
@Service
public class ReceiptPrintServiceImpl implements ReceiptPrintService {

    /** 支付方式文案（与 SettlementService 取值范围一致） */
    private static final Map<Integer, String> PAY_TEXT = Map.of(
            1, "储值卡", 2, "微信", 3, "支付宝", 4, "现金", 5, "次卡抵扣");

    private static final String BEGIN_ITEMS = "<!-- BEGIN items -->";
    private static final String END_ITEMS = "<!-- END items -->";

    private final PrintTemplateService printTemplateService;
    private final SettlementRepository settlementRepository;
    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ItemRepository itemRepository;
    private final StaffRepository staffRepository;

    /** 小票抬头机构名，可按部署覆盖 */
    @Value("${sinomed.receipt.clinic-name:示例医馆}")
    private String clinicName;

    @Override
    public String render(Long settlementId) {
        if (settlementId == null) {
            throw new IllegalArgumentException("结算单id不能为空");
        }
        SettlementEntity settlement = settlementRepository.findById(settlementId)
                .orElseThrow(() -> new IllegalArgumentException("结算单不存在：" + settlementId));
        OrderEntity order = orderRepository.findById(settlement.getOrderId()).orElse(null);
        String itemName = order == null ? "—" : itemRepository.findById(order.getItemId())
                .map(ItemEntity::getName).orElse("—");
        String customerName = customerRepository.findById(settlement.getUserId())
                .map(CustomerEntity::getName).orElse("—");
        String staff = order == null || order.getStaffId() == null ? "—"
                : staffRepository.findById(order.getStaffId()).map(StaffEntity::getName).orElse("—");
        String pay = PAY_TEXT.getOrDefault(settlement.getPayType(), String.valueOf(settlement.getPayType()));
        // 次卡抵扣实收 0，金额列写明抵扣口径，避免打印出 ¥0 像免单
        boolean cardPay = settlement.getPayType() != null && settlement.getPayType() == 5;
        String amount = cardPay && settlement.getMoney() != null && settlement.getMoney() == 0
                ? "次卡抵扣" : "¥" + settlement.getMoney();

        String html = printTemplateService.template("receipt")
                .replace("{{clinicName}}", esc(clinicName))
                .replace("{{orderNo}}", "S" + settlement.getId())
                .replace("{{customerName}}", esc(customerName))
                .replace("{{date}}", settlement.getCreateTime() == null
                        ? "" : new SimpleDateFormat("yyyy-MM-dd HH:mm").format(settlement.getCreateTime()))
                .replace("{{payType}}", esc(pay))
                .replace("{{total}}", "¥" + settlement.getMoney())
                .replace("{{staff}}", esc(staff));

        int begin = html.indexOf(BEGIN_ITEMS);
        int end = html.indexOf(END_ITEMS);
        if (begin >= 0 && end > begin) {
            String row = html.substring(begin + BEGIN_ITEMS.length(), end)
                    .replace("{{name}}", esc(itemName))
                    .replace("{{qty}}", "1")
                    .replace("{{amount}}", esc(amount));
            html = html.substring(0, begin) + row + html.substring(end + END_ITEMS.length());
        }
        return html;
    }

    /** 业务字段 HTML 转义：名字里带 <>& 等不破坏版式 */
    private String esc(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
