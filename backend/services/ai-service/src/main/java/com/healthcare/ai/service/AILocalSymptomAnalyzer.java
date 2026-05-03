package com.healthcare.ai.service;

import com.healthcare.ai.dto.AICheckResponse;
import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

@Component
public class AILocalSymptomAnalyzer {
    private static final Pattern DIACRITICS = Pattern.compile("\\p{M}+");

    public AICheckResponse analyze(String text) {
        String normalized = normalize(text);
        Set<String> conditions = new LinkedHashSet<>();
        Set<String> symptoms = new LinkedHashSet<>();
        String specialty = "Noi tong quat";
        List<String> advice = new ArrayList<>();

        if (containsAny(normalized, "ho", "khan tieng", "dau hong")) {
            symptoms.add("Ho/dau hong");
            conditions.add("Viem duong ho hap tren");
            specialty = "Tai mui hong";
            advice.add("Uong du nuoc am va theo doi tinh trang ho, dau hong.");
        }

        if (containsAny(normalized, "sot", "nong", "lanh run")) {
            symptoms.add("Sot");
            conditions.add("Nhiem trung hoac cam/cum");
            advice.add("Theo doi nhiet do, nghi ngoi va bo sung nuoc.");
        }

        if (containsAny(normalized, "kho tho", "tho gap", "tim tai", "dau nguc")) {
            symptoms.add("Kho tho/dau nguc");
            conditions.add("Can loai tru tinh trang ho hap hoac tim mach cap");
            specialty = "Cap cuu hoac Ho hap";
            advice.add("Neu kho tho, dau nguc, tim tai hoac met la, can di cap cuu ngay.");
        }

        if (containsAny(normalized, "dau bung", "buon non", "non", "tieu chay")) {
            symptoms.add("Trieu chung tieu hoa");
            conditions.add("Roi loan tieu hoa hoac viem da day ruot");
            specialty = "Tieu hoa";
            advice.add("An nhe, uong oresol/nuoc neu tieu chay hoac non nhieu.");
        }

        if (containsAny(normalized, "dau dau", "chong mat", "choang")) {
            symptoms.add("Dau dau/chong mat");
            conditions.add("Cang thang, mat ngu, mat nuoc hoac van de than kinh can theo doi");
            specialty = "Noi than kinh";
            advice.add("Nghi ngoi, tranh lai xe neu chong mat va theo doi dau dau tang dan.");
        }

        if (containsAny(normalized, "phat ban", "ngua", "noi me day", "di ung")) {
            symptoms.add("Phat ban/ngua");
            conditions.add("Di ung hoac viem da");
            specialty = "Da lieu";
            advice.add("Tranh tac nhan nghi ngo gay di ung va kham som neu phu moi/mat hoac kho tho.");
        }

        if (conditions.isEmpty()) {
            conditions.add("Chua du thong tin de goi y cu the");
            symptoms.add(text == null || text.isBlank() ? "Chua ghi nhan" : text.trim());
            advice.add("Mo ta them thoi gian, muc do, nhiet do, benh nen va thuoc da dung de duoc goi y sat hon.");
        }

        advice.add("Ket qua nay chi mang tinh tham khao, khong thay the chan doan y khoa.");
        return new AICheckResponse(
                List.copyOf(conditions),
                List.copyOf(symptoms),
                specialty,
                String.join(" ", advice)
        );
    }

    private static boolean containsAny(String value, String... terms) {
        for (String term : terms) {
            if (value.contains(term)) {
                return true;
            }
        }
        return false;
    }

    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD);
        normalized = DIACRITICS.matcher(normalized).replaceAll("");
        return normalized.toLowerCase(Locale.ROOT).replace('đ', 'd').replaceAll("\\s+", " ").trim();
    }
}
