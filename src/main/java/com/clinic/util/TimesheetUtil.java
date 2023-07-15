package com.clinic.util;

import com.clinic.entity.SignEntity;

import java.text.SimpleDateFormat;
import java.util.*;

public class TimesheetUtil {

    public static class TimesheetFilter{
        private Map<Long,StaffFilter> data;

        private List<SignEntity> signEntities;
        public TimesheetFilter(){
            data = new HashMap<>();
        }

        public Map<Long,StaffFilter> getData(){
            return data;
        }

        public static TimesheetFilter of(List<SignEntity> signEntities){
            TimesheetFilter timesheetFilter = new TimesheetFilter();
            timesheetFilter.signEntities = signEntities;
            return timesheetFilter;
        }

        private TimesheetFilter filter(){
            for (SignEntity signEntity : signEntities) {
                Long staffId = signEntity.getStaffId();
                StaffFilter v = data.getOrDefault(staffId, StaffFilter.of());
                v.appendSignEntity(signEntity);
                data.put(staffId,v);
            }
            return this;
        }
        public TimesheetFilter statistic(Date current) {
            filter();
            data.values().stream().parallel().forEach(filter -> {
                filter.statistic(current);
            });
            return this;
        }

    }

    public static class StaffFilter{
        private Long totalMilliseconds; //毫秒
        private Map<Date,DayFilter> data;
        private List<SignEntity> signEntities;

        public StaffFilter(){
            data = new HashMap<>();
            signEntities = new ArrayList<>();
            totalMilliseconds = 0L;
        }

        public Map<Date,DayFilter> getData(){
            return data;
        }

        public Long getTotalMilliseconds() {
            return totalMilliseconds;
        }

        public Long getHours(){
            return totalMilliseconds/(60*60*1000);
        }
        public static StaffFilter of(){
            StaffFilter staffFilter = new StaffFilter();
            return  staffFilter;
        }
        public static StaffFilter of(List<SignEntity> signEntities){
            StaffFilter staffFilter = new StaffFilter();
            staffFilter.signEntities = signEntities;
            return  staffFilter;
        }
        public boolean appendSignEntity(SignEntity signEntity){
            return this.signEntities.add(signEntity);
        }
        private StaffFilter filter(){
            for (SignEntity signEntity : signEntities){
                Date key = signEntity.getDayZeroTime();
                DayFilter dayFilter = data.getOrDefault(key, DayFilter.of());
                dayFilter.appendSignEntity(signEntity);
                data.put(key,dayFilter);
            }
            return this;
        }
        public StaffFilter statistic(Date current) {
            filter();
            data.values().stream().parallel().forEach(dayFilter -> {
                dayFilter.statistic(current);
                this.totalMilliseconds += dayFilter.getTotalMilliseconds();
            });
            return this;
        }

    }
    public static class DayFilter {
        private static SimpleDateFormat shortTimeFormat = new SimpleDateFormat ("hh:mm:ss");
        private Date start;
        private Date end;

        private int day;

        private List<SignEntity> signEntities;

        private Long totalMilliseconds; //毫秒

        public DayFilter(){
            start = null;
            end = null;
            totalMilliseconds = 0L;
            this.signEntities = new ArrayList<>();
        }

        public DayFilter(List<SignEntity> signEntities){
            start = null;
            end = null;
            totalMilliseconds = 0L;
            this.signEntities = signEntities;
        }

        public static DayFilter of(List<SignEntity> signEntities){
            return new DayFilter(signEntities);
        }

        public static DayFilter of(){
            return new DayFilter();
        }

        public boolean appendSignEntity(SignEntity signEntity){
            return this.signEntities.add(signEntity);
        }

        public Date getStart(){
            return start;
        }

        public String getShortStart(){
            if (start == null){
                return "";
            }
            return  shortTimeFormat.format(start);
        }

        public Date getEnd() {
            return end;
        }

        public String getShortEnd(){
            if (end == null){
                return "";
            }
            return  shortTimeFormat.format(end);
        }

        public Long getTotalMilliseconds(){
            return totalMilliseconds;
        }

        public Long getHours(){
            return totalMilliseconds/(60*60*1000);
        }

        public int getDay(){
            return this.day;
        }

        public DayFilter statistic(Date current) {
            filter();
            // 计算当前的总时间
            if (start == null){
                return this;
            }
            if (end != null && end.after(start)){
                totalMilliseconds =  end.getTime() - start.getTime();
            }else{
                if (DateUtil.isSomeDay(current,start)){
                    totalMilliseconds = current.getTime() - start.getTime();
                }
            }
            return this;
        }

        private DayFilter filter(){
            for (SignEntity signEntity : signEntities) {
                if (signEntity.getType()  > 0) {
                    // 上班
                    if (start == null){
                        start = signEntity.getCreateTime();
                        day = DateUtil.getDayAtMonth(signEntity.getCreateTime());
                        continue;
                    }
                    if (start.after(signEntity.getCreateTime())){
                        start = signEntity.getCreateTime();
                    }
                }else{
                    // 下班
                    if (end == null){
                        end = signEntity.getCreateTime();
                        continue;
                    }
                    if (end.before(signEntity.getCreateTime())){
                        end = signEntity.getCreateTime();
                    }
                }
            }
            return this;
        }

    }
}
