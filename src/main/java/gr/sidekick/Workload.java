// SPDX-License-Identifier: GPL-3.0-only
// Copyright (C) 2026 Dimitrios Diamantis
package gr.sidekick;
import java.time.*;
import static gr.sidekick.Model.*;

/** Per-employee, per-month targets. A reserve has a ceiling, never a quota to fill. */
public final class Workload {
    private Workload(){}
    public static WorkPlan plan(Team t,Employee e,YearMonth month){
        Model.Month m=t.months.get(month.toString());return m==null||m.workPlans==null?null:m.workPlans.get(e.id);
    }
    public static boolean reserve(Team t,Employee e,YearMonth m){WorkPlan p=plan(t,e,m);return p!=null&&"reserve".equals(p.mode);}
    public static boolean custom(Team t,Employee e,YearMonth m){WorkPlan p=plan(t,e,m);return p!=null&&("target".equals(p.mode)||"reserve".equals(p.mode));}
    public static int leaveCount(Team t,Employee e,YearMonth m){
        int n=0;for(int d=1;d<=m.lengthOfMonth();d++){Cell c=t.cell(e.id,m.atDay(d));if(c!=null&&t.post(c.value)==null&&!OFF.equals(c.value))n++;}return n;
    }
    public static int automatic(Team t,Employee e,YearMonth m){return Math.max(0,m.lengthOfMonth()-CalendarRules.offTarget(t,m)-leaveCount(t,e,m));}
    public static int target(Team t,Employee e,YearMonth m){return custom(t,e,m)?Math.max(0,Math.min(m.lengthOfMonth(),plan(t,e,m).shifts)):automatic(t,e,m);}
    public static int offTarget(Team t,Employee e,YearMonth m){return custom(t,e,m)?Math.max(0,m.lengthOfMonth()-leaveCount(t,e,m)-target(t,e,m)):CalendarRules.offTarget(t,m);}
    public static void set(Team t,Employee e,YearMonth m,String mode,int shifts){
        if(!java.util.Set.of("auto","target","reserve").contains(mode)||shifts<0||shifts>m.lengthOfMonth())throw new IllegalArgumentException("Invalid monthly workload");
        Model.Month schedule=t.month(m);if(schedule.workPlans==null)schedule.workPlans=new java.util.LinkedHashMap<>();
        if("auto".equals(mode))schedule.workPlans.remove(e.id);else schedule.workPlans.put(e.id,new WorkPlan(mode,shifts));
    }
}
